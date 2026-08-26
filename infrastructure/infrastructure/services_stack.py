from textwrap import dedent

import aws_cdk as cdk
from aws_cdk import (
    Stack,
    aws_certificatemanager as acm,
    aws_cloudfront as cloudfront,
    aws_cloudfront_origins as origins,
    aws_ec2 as ec2,
    aws_ecr as ecr,
    aws_elasticloadbalancingv2 as elbv2,
    aws_elasticloadbalancingv2_targets as elbv2_targets,
    aws_iam as iam,
    aws_s3 as s3,
    aws_secretsmanager as secretsmanager,
)
from constructs import Construct


class ServicesStack(Stack):
    """Cost-optimized deployment: all backend containers run on one EC2 host."""

    def __init__(
        self,
        scope: Construct,
        construct_id: str,
        vpc_id: str,
        **kwargs,
    ) -> None:
        super().__init__(scope, construct_id, **kwargs)

        vpc = ec2.Vpc.from_lookup(self, "BentoSaaSVpc", vpc_id=vpc_id)

        # Existing application secrets.
        jwt_secret_arn = cdk.CfnParameter(self, "JwtSecretArn", type="String")
        stripe_key_arn = cdk.CfnParameter(self, "StripeSecretKeyArn", type="String")
        stripe_webhook_arn = cdk.CfnParameter(
            self, "StripeWebhookSecretArn", type="String"
        )
        jwt_secret = secretsmanager.Secret.from_secret_complete_arn(
            self, "JwtSecret", jwt_secret_arn.value_as_string
        )
        stripe_key = secretsmanager.Secret.from_secret_complete_arn(
            self, "StripeSecretKey", stripe_key_arn.value_as_string
        )
        stripe_webhook = secretsmanager.Secret.from_secret_complete_arn(
            self, "StripeWebhookSecret", stripe_webhook_arn.value_as_string
        )

        # Replaces the password formerly generated with the RDS instance.
        db_password = secretsmanager.Secret(
            self,
            "PostgresPassword",
            description="PostgreSQL password for the EC2 Docker deployment",
            generate_secret_string=secretsmanager.SecretStringGenerator(
                exclude_punctuation=True,
                password_length=32,
            ),
            removal_policy=cdk.RemovalPolicy.DESTROY,
        )

        repo_parameter_names = {
            "user": "UserServiceEcrRepoName",
            "plan": "PlanServiceEcrRepoName",
            "subscription": "SubscriptionServiceEcrRepoName",
            "invoice": "InvoiceServiceEcrRepoName",
            "notification": "NotificationServiceEcrRepoName",
            "gateway": "SpringCloudGatewayEcrRepoName",
        }
        repositories: dict[str, ecr.IRepository] = {}
        for key, parameter_name in repo_parameter_names.items():
            repo_name = cdk.CfnParameter(self, parameter_name, type="String")
            repositories[key] = ecr.Repository.from_repository_name(
                self, f"{key.title()}Repo", repo_name.value_as_string
            )

        plan_prefix = cdk.CfnParameter(
            self, "PlanImagesPrefix", type="String", default="bento_images/plan/"
        )
        meal_prefix = cdk.CfnParameter(
            self, "MealImagesPrefix", type="String", default="bento_images/meal/"
        )
        user_prefix = cdk.CfnParameter(
            self, "UserImagesPrefix", type="String", default="bento_images/user/"
        )

        media_bucket = self._private_bucket("MediaBucket")
        frontend_bucket = self._private_bucket("FrontendBucket")

        role = iam.Role(
            self,
            "ApplicationEc2Role",
            assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"),
            managed_policies=[
                iam.ManagedPolicy.from_aws_managed_policy_name(
                    "AmazonSSMManagedInstanceCore"
                )
            ],
        )
        for repository in repositories.values():
            repository.grant_pull(role)
        for secret in (jwt_secret, stripe_key, stripe_webhook, db_password):
            secret.grant_read(role)
        media_bucket.grant_read_write(role)

        alb_sg = ec2.SecurityGroup(self, "AlbSG", vpc=vpc, allow_all_outbound=True)
        alb_sg.add_ingress_rule(ec2.Peer.any_ipv4(), ec2.Port.tcp(80), "HTTP")
        alb_sg.add_ingress_rule(ec2.Peer.any_ipv4(), ec2.Port.tcp(443), "HTTPS")

        app_sg = ec2.SecurityGroup(
            self, "ApplicationEc2SG", vpc=vpc, allow_all_outbound=True
        )
        app_sg.add_ingress_rule(
            alb_sg,
            ec2.Port.tcp(4003),
            "Allow ALB to Spring Cloud Gateway only",
        )

        instance = ec2.Instance(
            self,
            "BentoSaaSApplicationHost",
            vpc=vpc,
            vpc_subnets=ec2.SubnetSelection(subnet_type=ec2.SubnetType.PUBLIC),
            instance_type=ec2.InstanceType("t4g.large"),
            machine_image=ec2.MachineImage.latest_amazon_linux2023(
                cpu_type=ec2.AmazonLinuxCpuType.ARM_64
            ),
            security_group=app_sg,
            role=role,
            associate_public_ip_address=True,
            require_imdsv2=True,
            block_devices=[
                ec2.BlockDevice(
                    device_name="/dev/xvda",
                    volume=ec2.BlockDeviceVolume.ebs(
                        40,
                        volume_type=ec2.EbsDeviceVolumeType.GP3,
                        encrypted=True,
                        delete_on_termination=True,
                    ),
                )
            ],
        )

        instance.add_user_data(
            self._bootstrap_script(
                repositories=repositories,
                jwt_secret_arn=jwt_secret.secret_arn,
                stripe_key_arn=stripe_key.secret_arn,
                stripe_webhook_arn=stripe_webhook.secret_arn,
                db_password_arn=db_password.secret_arn,
                media_bucket_name=media_bucket.bucket_name,
                plan_prefix=plan_prefix.value_as_string,
                meal_prefix=meal_prefix.value_as_string,
                user_prefix=user_prefix.value_as_string,
            )
        )

        alb = elbv2.ApplicationLoadBalancer(
            self,
            "ApiAlb",
            vpc=vpc,
            internet_facing=True,
            security_group=alb_sg,
            vpc_subnets=ec2.SubnetSelection(subnet_type=ec2.SubnetType.PUBLIC),
        )
        http_listener = alb.add_listener("HttpListener", port=80, open=False)
        http_listener.add_action(
            "RedirectToHttps",
            action=elbv2.ListenerAction.redirect(
                protocol="HTTPS", port="443", permanent=True
            ),
        )

        api_cert_arn = cdk.CfnParameter(
            self,
            "ApiGatewayAcmCertArn",
            type="String",
            description="ACM certificate ARN in this stack's region",
        )
        api_cert = acm.Certificate.from_certificate_arn(
            self, "ApiCertificate", api_cert_arn.value_as_string
        )
        https_listener = alb.add_listener(
            "HttpsListener",
            port=443,
            certificates=[api_cert],
            open=False,
        )
        https_listener.add_targets(
            "SpringCloudGatewayTarget",
            port=4003,
            protocol=elbv2.ApplicationProtocol.HTTP,
            targets=[elbv2_targets.InstanceTarget(instance, port=4003)],
            health_check=elbv2.HealthCheck(
                path="/actuator/health",
                healthy_http_codes="200",
                interval=cdk.Duration.seconds(30),
            ),
        )

        self._create_frontend(frontend_bucket)

        cdk.CfnOutput(self, "ApplicationInstanceId", value=instance.instance_id)
        cdk.CfnOutput(
            self, "ApiGatewayHttpsURL", value=f"https://{alb.load_balancer_dns_name}"
        )
        cdk.CfnOutput(self, "MediaBucketName", value=media_bucket.bucket_name)
        cdk.CfnOutput(self, "FrontendBucketName", value=frontend_bucket.bucket_name)

    def _bootstrap_script(
        self,
        repositories: dict[str, ecr.IRepository],
        jwt_secret_arn: str,
        stripe_key_arn: str,
        stripe_webhook_arn: str,
        db_password_arn: str,
        media_bucket_name: str,
        plan_prefix: str,
        meal_prefix: str,
        user_prefix: str,
    ) -> str:
        region = Stack.of(self).region
        account = Stack.of(self).account

        # Quoted heredocs prevent the shell from expanding Compose variables
        # while files are being written. Runtime values live in /opt/bento/.env.
        return dedent(
            f"""\
            #!/bin/bash
            set -euxo pipefail
            exec > >(tee /var/log/bento-bootstrap.log | logger -t bento-bootstrap -s 2>/dev/console) 2>&1

            dnf update -y
            dnf install -y docker awscli
            systemctl enable --now docker

            mkdir -p \
            /usr/local/lib/docker/cli-plugins \
            /opt/bento/postgres-init

            curl -fL \
            https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64 \
            -o /usr/local/lib/docker/cli-plugins/docker-compose

            chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

            aws ecr get-login-password --region {region} | \
            docker login \
                --username AWS \
                --password-stdin \
                {account}.dkr.ecr.{region}.amazonaws.com

            get_secret() {{
            aws secretsmanager get-secret-value \
                --region {region} \
                --secret-id "$1" \
                --query SecretString \
                --output text
            }}

            umask 077

            cat > /opt/bento/.env <<EOF
            POSTGRES_PASSWORD=$(get_secret '{db_password_arn}')
            JWT_SECRET_KEY=$(get_secret '{jwt_secret_arn}')
            STRIPE_SECRET_KEY=$(get_secret '{stripe_key_arn}')
            STRIPE_WEBHOOK_SECRET=$(get_secret '{stripe_webhook_arn}')
            AWS_BUCKET_NAME={media_bucket_name}
            EOF

            chmod 600 /opt/bento/.env
            umask 022

            cat > /opt/bento/postgres-init/01-create-databases.sql <<'SQL'
            CREATE DATABASE "user-service";
            CREATE DATABASE "plan-management-service";
            CREATE DATABASE "subscription-service";
            CREATE DATABASE "invoice-service";
            CREATE DATABASE "notification-service";

            \\connect "user-service"
            CREATE SCHEMA IF NOT EXISTS userinfo;

            \\connect "plan-management-service"
            CREATE SCHEMA IF NOT EXISTS planmanagement;

            \\connect "subscription-service"
            CREATE SCHEMA IF NOT EXISTS subscription;

            \\connect "invoice-service"
            CREATE SCHEMA IF NOT EXISTS invoice;

            \\connect "notification-service"
            CREATE SCHEMA IF NOT EXISTS notification;
            SQL

            chmod 644 /opt/bento/postgres-init/01-create-databases.sql

            cat > /opt/bento/compose.yaml <<'YAML'
            name: bento-saas

            services:
            postgres:
                image: imresamu/postgis:17-3.6-bookworm
                restart: unless-stopped
                environment:
                POSTGRES_USER: postgres
                POSTGRES_PASSWORD: ${{POSTGRES_PASSWORD}}
                POSTGRES_DB: postgres
                volumes:
                - postgres-data:/var/lib/postgresql/data
                - ./postgres-init:/docker-entrypoint-initdb.d:ro
                healthcheck:
                test: ["CMD-SHELL", "pg_isready -U postgres -d postgres"]
                interval: 10s
                timeout: 5s
                retries: 12

            kafka:
                image: apache/kafka:3.7.0
                restart: unless-stopped
                environment:
                KAFKA_NODE_ID: 1
                KAFKA_PROCESS_ROLES: broker,controller
                KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
                KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
                KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
                KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
                KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
                KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
                KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
                KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
                KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
                KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
                KAFKA_NUM_PARTITIONS: 1
                KAFKA_HEAP_OPTS: -Xms256m -Xmx512m
                volumes:
                - kafka-data:/var/lib/kafka/data

            user-service:
                image: {repositories['user'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                postgres:
                    condition: service_healthy
                environment:
                SPRING_PROFILES_ACTIVE: prod
                SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/user-service?currentSchema=userinfo
                SPRING_DATASOURCE_USERNAME: postgres
                SPRING_DATASOURCE_PASSWORD: ${{POSTGRES_PASSWORD}}
                SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.PostgreSQLDialect
                SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
                JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                JWT_EXPIRATION_TIME: "7200000"
                AWS_REGION: {region}
                AWS_EXPIRATION_TIME_MIN: "60"
                AWS_BUCKET_NAME: ${{AWS_BUCKET_NAME}}
                JAVA_TOOL_OPTIONS: -Xms128m -Xmx512m
                networks:
                default:
                    aliases:
                    - user-service.bento-saas.local

            plan-management-service:
                image: {repositories['plan'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                postgres:
                    condition: service_healthy
                kafka:
                    condition: service_started
                environment:
                SPRING_PROFILES_ACTIVE: prod
                SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/plan-management-service?currentSchema=planmanagement
                SPRING_DATASOURCE_USERNAME: postgres
                SPRING_DATASOURCE_PASSWORD: ${{POSTGRES_PASSWORD}}
                SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.PostgreSQLDialect
                SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
                JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                JWT_EXPIRATION_TIME: "7200000"
                AWS_REGION: {region}
                AWS_EXPIRATION_TIME_MIN: "60"
                AWS_BUCKET_NAME: ${{AWS_BUCKET_NAME}}
                JAVA_TOOL_OPTIONS: -Xms128m -Xmx512m
                networks:
                default:
                    aliases:
                    - plan-management-service.bento-saas.local

            subscription-service:
                image: {repositories['subscription'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                postgres:
                    condition: service_healthy
                environment:
                SPRING_PROFILES_ACTIVE: prod
                SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/subscription-service?currentSchema=subscription
                SPRING_DATASOURCE_USERNAME: postgres
                SPRING_DATASOURCE_PASSWORD: ${{POSTGRES_PASSWORD}}
                SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.PostgreSQLDialect
                SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
                PLAN_MANAGEMENT_SERVICE_URL: http://plan-management-service.bento-saas.local:4000/plan-management
                JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                JWT_EXPIRATION_TIME: "7200000"
                JAVA_TOOL_OPTIONS: -Xms128m -Xmx512m
                networks:
                default:
                    aliases:
                    - subscription-service.bento-saas.local

            invoice-service:
                image: {repositories['invoice'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                postgres:
                    condition: service_healthy
                environment:
                SPRING_PROFILES_ACTIVE: prod
                SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/invoice-service?currentSchema=invoice
                SPRING_DATASOURCE_USERNAME: postgres
                SPRING_DATASOURCE_PASSWORD: ${{POSTGRES_PASSWORD}}
                SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT: org.hibernate.dialect.PostgreSQLDialect
                SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
                PLAN_MANAGEMENT_SERVICE_ADDRESS: plan-management-service.bento-saas.local
                PLAN_MANAGEMENT_SERVICE_PORT: "9000"
                SUBSCRIPTION_SERVICE_ADDRESS: subscription-service.bento-saas.local
                SUBSCRIPTION_SERVICE_PORT: "9001"
                JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                JWT_EXPIRATION_TIME: "7200000"
                STRIPE_SECRET_KEY: ${{STRIPE_SECRET_KEY}}
                STRIPE_WEBHOOK_SECRET: ${{STRIPE_WEBHOOK_SECRET}}
                JAVA_TOOL_OPTIONS: -Xms128m -Xmx512m
                networks:
                default:
                    aliases:
                    - invoice-service.bento-saas.local

            notification-service:
                image: {repositories['notification'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                postgres:
                    condition: service_healthy
                kafka:
                    condition: service_started
                environment:
                NOTI_DB_PARAMS_ADDRESS: postgres
                NOTI_DB_PARAMS_PORT: "5432"
                NOTI_DB_PARAMS_DBNAME: notification-service
                NOTI_DB_PARAMS_USER: postgres
                NOTI_DB_PARAMS_PASSWORD: ${{POSTGRES_PASSWORD}}
                NOTI_DB_PARAMS_SSLMODE: disable
                NOTI_KAFKA_PARAMS_BOOTSTRAP_SERVERS: kafka:9092
                NOTI_SERVER_ADDRESS: 0.0.0.0
                NOTI_JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                networks:
                default:
                    aliases:
                    - notification-service.bento-saas.local

            api-gateway:
                image: {repositories['gateway'].repository_uri}:latest
                restart: unless-stopped
                depends_on:
                - user-service
                - plan-management-service
                - subscription-service
                - invoice-service
                - notification-service
                ports:
                - "4003:4003"
                environment:
                SPRING_PROFILES_ACTIVE: prod
                USER_SERVICE_URL: http://user-service.bento-saas.local:4004/user/v1
                JWT_SECRET_KEY: ${{JWT_SECRET_KEY}}
                JWT_EXPIRATION_TIME: "7200000"
                JAVA_TOOL_OPTIONS: -Xms128m -Xmx512m

            volumes:
            postgres-data:
            kafka-data:
            YAML

            cd /opt/bento
            docker compose pull
            docker compose up -d
            docker compose ps
            """
        )

    def _create_frontend(self, bucket: s3.Bucket) -> None:
        oai = cloudfront.OriginAccessIdentity(self, "FrontendOAI")
        bucket.grant_read(oai)

        cert_arn = cdk.CfnParameter(
            self,
            "FrontendAcmCertArn",
            type="String",
            description="ACM certificate ARN in us-east-1 for CloudFront",
        )
        domain_name = cdk.CfnParameter(
            self,
            "FrontendDomainName",
            type="String",
            description="Frontend custom domain",
        )
        certificate = acm.Certificate.from_certificate_arn(
            self, "FrontendCertificate", cert_arn.value_as_string
        )
        distribution = cloudfront.Distribution(
            self,
            "FrontendDistribution",
            default_behavior=cloudfront.BehaviorOptions(
                origin=origins.S3Origin(bucket, origin_access_identity=oai),
                viewer_protocol_policy=cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
                cache_policy=cloudfront.CachePolicy.CACHING_OPTIMIZED,
            ),
            default_root_object="index.html",
            domain_names=[domain_name.value_as_string],
            certificate=certificate,
            error_responses=[
                cloudfront.ErrorResponse(
                    http_status=status,
                    response_http_status=200,
                    response_page_path="/index.html",
                    ttl=cdk.Duration.seconds(0),
                )
                for status in (403, 404)
            ],
        )
        cdk.CfnOutput(
            self,
            "FrontendCloudFrontURL",
            value=f"https://{distribution.distribution_domain_name}",
        )

    def _private_bucket(self, construct_id: str) -> s3.Bucket:
        return s3.Bucket(
            self,
            construct_id,
            block_public_access=s3.BlockPublicAccess.BLOCK_ALL,
            encryption=s3.BucketEncryption.S3_MANAGED,
            enforce_ssl=True,
            removal_policy=cdk.RemovalPolicy.DESTROY,
            auto_delete_objects=True,
        )