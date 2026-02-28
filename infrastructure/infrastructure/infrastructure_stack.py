import aws_cdk as cdk
from aws_cdk import (
    # Duration,
    Stack,
    # aws_sqs as sqs,
    aws_ec2 as ec2,
    aws_rds as rds,
    aws_route53 as route53,
    aws_msk as msk,
    aws_ecr as ecr,
    aws_ecs as ecs,
    aws_logs as logs,
    aws_ecs_patterns as ecs_patterns,
    aws_certificatemanager as acm,
    aws_elasticloadbalancingv2 as elbv2,
    aws_secretsmanager as secretsmanager,
    aws_iam as iam,
    aws_lambda as _lambda,
    aws_s3 as s3,
    aws_s3_deployment as s3_deployment,
    aws_cloudfront as cloudfront,
    aws_cloudfront_origins as origins,
    custom_resources as cr
)
from aws_cdk.aws_lambda_python_alpha import PythonFunction

from constructs import Construct


class InfrastructureStack(Stack):
    vpc: ec2.Vpc
    ecs_cluster: ecs.Cluster
    db_name = "bento_saas"
    db_user_name = "postgres"
    ecs_sg: ec2.SecurityGroup
    rds_sg: ec2.SecurityGroup
    msk_sg: ec2.SecurityGroup
    kafka_bootstrap_servers: str
    jwt_secret: secretsmanager.ISecret
    stripe_secret_key: secretsmanager.ISecret
    stripe_webhook_secret: secretsmanager.ISecret
    media_bucket: s3.Bucket

    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # VPC configs
        self.vpc = self.create_vpc()

        # Security Group configs
        self.ecs_sg = ec2.SecurityGroup(self, "EcsServicesSG", vpc=self.vpc, allow_all_outbound=True)

        self.rds_sg = ec2.SecurityGroup(self, "RdsSG", vpc=self.vpc, allow_all_outbound=True)
        self.rds_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp(5432),
            "Allow ECS to Postgres"
        )

        # DB configs
        bento_saas_db = self.create_database_instance("BentoSaaSDB")
        bento_saas_db_health_check = self.cfn_health_check(bento_saas_db, "BentoSaaSDBHealthCheck")
        bento_saas_db.connections.allow_default_port_from(self.ecs_sg)

        # DB init lambda
        db_init_lambda = PythonFunction(
            self,
            "DBInitLambda",
            entry="infrastructure/lambda/db_init",
            index="handler.py",
            handler="handler",
            runtime=_lambda.Runtime.PYTHON_3_12,
            vpc=self.vpc,
            environment={
                "DB_SECRET_ARN": bento_saas_db.secret.secret_arn,
                "DB_HOST": bento_saas_db.db_instance_endpoint_address,
                "DB_PORT": bento_saas_db.db_instance_endpoint_port,
            },
        )
        bento_saas_db.secret.grant_read(db_init_lambda)
        bento_saas_db.connections.allow_default_port_from(db_init_lambda)

        provider = cr.Provider(
            self,
            "DbInitProvider",
            on_event_handler=db_init_lambda,
        )

        db_init_resource = cdk.CustomResource(
            self,
            "DbInitCustomResource",
            service_token=provider.service_token,
        )

        # Secrets (import existing Secrets Manager secrets by ARN)
        jwt_secret_arn = cdk.CfnParameter(
            self,
            "JwtSecretArn",
            type="String",
            description="Secrets Manager ARN for shared JWT secret (used by Spring services)"
        )
        stripe_secret_key_arn = cdk.CfnParameter(
            self,
            "StripeSecretKeyArn",
            type="String",
            description="Secrets Manager ARN for Stripe secret key (invoice-service)"
        )
        stripe_webhook_secret_arn = cdk.CfnParameter(
            self,
            "StripeWebhookSecretArn",
            type="String",
            description="Secrets Manager ARN for Stripe webhook secret (invoice-service)"
        )

        self.jwt_secret = secretsmanager.Secret.from_secret_complete_arn(
            self,
            "JwtSecret",
            jwt_secret_arn.value_as_string,
        )
        self.stripe_secret_key = secretsmanager.Secret.from_secret_complete_arn(
            self,
            "StripeSecretKey",
            stripe_secret_key_arn.value_as_string,
        )
        self.stripe_webhook_secret = secretsmanager.Secret.from_secret_complete_arn(
            self,
            "StripeWebhookSecret",
            stripe_webhook_secret_arn.value_as_string,
        )

        # S3 (shared bucket for presigned URL generation)
        plan_images_prefix = cdk.CfnParameter(
            self,
            "PlanImagesPrefix",
            type="String",
            default="plan/",
            description="Key prefix for plan images"
        )
        meal_images_prefix = cdk.CfnParameter(
            self,
            "MealImagesPrefix",
            type="String",
            default="meal/",
            description="Key prefix for meal images"
        )
        user_images_prefix = cdk.CfnParameter(
            self,
            "UserImagesPrefix",
            type="String",
            default="user/",
            description="Key prefix for user images"
        )

        # Create the shared media bucket (plan/meal/user images)
        self.media_bucket = self.create_private_bucket("MediaBucket")

        cdk.CfnOutput(
            self,
            "MediaBucketName",
            value=self.media_bucket.bucket_name,
        )

        # Kafka configs
        self.msk_sg = ec2.SecurityGroup(
            self,
            "MskSG",
            vpc=self.vpc,
            allow_all_outbound=True,
            description="SG for MSK brokers"
        )
        # Allow ECS tasks to reach MSK brokers (PLAINTEXT 9092 inside VPC)
        self.msk_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp(9092),
            "Allow ECS to MSK brokers (9092)"
        )
        msk_cluster = self.create_msk_cluster()
        # Export bootstrap brokers so services can publish/consume
        self.kafka_bootstrap_servers = msk_cluster.get_att("BootstrapBrokers").to_string()

        # ECS configs
        # Cluster
        self.ecs_cluster = self.create_ecs_cluster()

        # UserService ECS
        user_repo = self.create_ecr_repository(
            "UserRepository",
            "user-repo"
        )
        user_service = self.create_fargate_service(
            "UserService",
            "user-service",
            user_repo,
            [4004],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "USER_SERVICE_URL": "http://user-service.bento-saas.local:4004/user/v1",
            },
            "SPRING_DATASOURCE_PASSWORD",
            {
                "JWT_SECRET": ecs.Secret.from_secrets_manager(self.jwt_secret),
            },
            "user"
        )
        user_service.node.add_dependency(bento_saas_db_health_check)
        user_service.node.add_dependency(bento_saas_db)

        # Allow user-service to generate presigned URLs for user images
        user_service.task_definition.task_role.add_to_principal_policy(
            iam.PolicyStatement(
                actions=["s3:GetObject", "s3:PutObject"],
                resources=[
                    self.media_bucket.arn_for_objects(f"{user_images_prefix.value_as_string}*")
                ],
            )
        )

        # PlanManagementService ECS
        plan_management_repo = self.create_ecr_repository(
            "PlanManagementRepository",
            "plan-management-repo"
        )
        plan_management_service = self.create_fargate_service(
            "PlanManagementService",
            "plan-management-service",
            plan_management_repo,
            [4000, 9000],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod"
            },
            "SPRING_DATASOURCE_PASSWORD",
            {
                "JWT_SECRET": ecs.Secret.from_secrets_manager(self.jwt_secret),
            },
            "planmanagement"
        )
        plan_management_service.node.add_dependency(bento_saas_db_health_check)
        plan_management_service.node.add_dependency(bento_saas_db)
        plan_management_service.node.add_dependency(msk_cluster)
        plan_management_service.node.add_dependency(user_service)

        # Allow plan-management-service to generate presigned URLs for plan and meal images
        plan_management_service.task_definition.task_role.add_to_principal_policy(
            iam.PolicyStatement(
                actions=["s3:GetObject", "s3:PutObject"],
                resources=[
                    self.media_bucket.arn_for_objects(f"{plan_images_prefix.value_as_string}*"),
                    self.media_bucket.arn_for_objects(f"{meal_images_prefix.value_as_string}*"),
                ],
            )
        )

        # SubscriptionService ECS
        subscription_repo = self.create_ecr_repository(
            "SubscriptionRepository",
            "subscription-repo"
        )
        subscription_service = self.create_fargate_service(
            "SubscriptionService",
            "subscription-service",
            subscription_repo,
            [4001, 9001],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "PLAN_MANAGEMENT_SERVICE_URL": "http://plan-management-service.bento-saas.local:4000/plan-management",
            },
            "SPRING_DATASOURCE_PASSWORD",
            {
                "JWT_SECRET": ecs.Secret.from_secrets_manager(self.jwt_secret),
            },
            "subscription"
        )
        subscription_service.node.add_dependency(bento_saas_db_health_check)
        subscription_service.node.add_dependency(bento_saas_db)
        subscription_service.node.add_dependency(msk_cluster)
        subscription_service.node.add_dependency(user_service)

        # InvoiceService ECS
        invoice_repo = self.create_ecr_repository(
            "InvoiceRepository",
            "invoice-repo"
        )
        invoice_service = self.create_fargate_service(
            "InvoiceService",
            "invoice-service",
            invoice_repo,
            [4002, 9002],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "PLAN_MANAGEMENT_SERVICE_ADDRESS": "plan-management-service.bento-saas.local",
                "PLAN_MANAGEMENT_SERVICE_PORT": "9000",
                "SUBSCRIPTION_SERVICE_ADDRESS": "subscription-service.bento-saas.local",
                "SUBSCRIPTION_SERVICE_PORT": "9001",
            },
            "SPRING_DATASOURCE_PASSWORD",
            {
                "JWT_SECRET": ecs.Secret.from_secrets_manager(self.jwt_secret),
                "STRIPE_SECRET_KEY": ecs.Secret.from_secrets_manager(self.stripe_secret_key),
                "STRIPE_WEBHOOK_SECRET": ecs.Secret.from_secrets_manager(self.stripe_webhook_secret),
            },
            "invoice"
        )
        invoice_service.node.add_dependency(bento_saas_db_health_check)
        invoice_service.node.add_dependency(bento_saas_db)
        invoice_service.node.add_dependency(msk_cluster)
        invoice_service.node.add_dependency(user_service)
        invoice_service.node.add_dependency(plan_management_service)
        invoice_service.node.add_dependency(subscription_service)

        # NotificationService ECS
        notification_repo = self.create_ecr_repository(
            "NotificationRepository",
            "notification-repo"
        )
        notification_service = self.create_fargate_service(
            "NotificationService",
            "notification-service",
            notification_repo,
            [4005],
            bento_saas_db,
            {
                "NOTI_DB_PARAMS_ADDRESS": bento_saas_db.db_instance_endpoint_address,
                "NOTI_DB_PARAMS_PORT": bento_saas_db.db_instance_endpoint_port,
                "NOTI_DB_PARAMS_DBNAME": "notification-service-db",
                'NOTI_DB_PARAMS_USER': self.db_user_name,
                "NOTI_KAFKA_PARAMS_BOOTSTRAP_SERVERS": self.kafka_bootstrap_servers,
                "NOTI_SERVER_ADDRESS": "0.0.0.0"
            },
            "NOTI_DB_PARAMS_PASSWORD",
            None,
            "notification"
        )
        notification_service.node.add_dependency(bento_saas_db_health_check)
        notification_service.node.add_dependency(bento_saas_db)
        notification_service.node.add_dependency(msk_cluster)
        notification_service.node.add_dependency(user_service)

        # ApiGateway ECS
        api_gateway_repo = self.create_ecr_repository(
            "ApiGatewayRepository",
            "api_gateway_repo"
        )
        self.create_api_gateway_service(api_gateway_repo)
        self.create_frontend_infra()

        user_service.node.add_dependency(db_init_resource)
        plan_management_service.node.add_dependency(db_init_resource)
        subscription_service.node.add_dependency(db_init_resource)
        invoice_service.node.add_dependency(db_init_resource)
        notification_service.node.add_dependency(db_init_resource)

    def create_vpc(self) -> ec2.Vpc:
        return ec2.Vpc(
            self,
            "BentoSaaSVpc",
            max_azs=2)

    def create_database_instance(self, construct_id: str) -> rds.DatabaseInstance:
        return rds.DatabaseInstance(
            self,
            construct_id,
            engine=rds.DatabaseInstanceEngine.postgres(version=rds.PostgresEngineVersion.VER_17_2),
            vpc=self.vpc,
            instance_type=ec2.InstanceType.of(
                ec2.InstanceClass.BURSTABLE2, ec2.InstanceSize.MEDIUM
            ),
            allocated_storage=20,
            credentials=rds.Credentials.from_generated_secret(self.db_user_name),
            removal_policy=cdk.RemovalPolicy.DESTROY,
            security_groups=[self.rds_sg]
        )

    def cfn_health_check(self, database_instance: rds.DatabaseInstance, construct_id: str) -> route53.CfnHealthCheck:
        return route53.CfnHealthCheck(
            self,
            construct_id,
            health_check_config=route53.CfnHealthCheck.HealthCheckConfigProperty(
                type="TCP",
                port=cdk.Token.as_number(database_instance.db_instance_endpoint_port),
                ip_address=database_instance.db_instance_endpoint_address,
                request_interval=30,
                failure_threshold=3
            )
        )

    def create_msk_cluster(self) -> msk.CfnCluster:
        return msk.CfnCluster(
            self,
            "BentoSaaSMskCluster",
            cluster_name="bento-saas-kafka-cluster",
            kafka_version="4.2.0",
            number_of_broker_nodes=2,
            broker_node_group_info=msk.CfnCluster.BrokerNodeGroupInfoProperty(
                instance_type="kafka.m5.large",
                client_subnets=self.vpc.select_subnets(subnet_type=ec2.SubnetType.PRIVATE_WITH_EGRESS).subnet_ids,
                broker_az_distribution="DEFAULT",
                security_groups=[self.msk_sg.security_group_id]
            ),
            encryption_info=msk.CfnCluster.EncryptionInfoProperty(
                encryption_in_transit=msk.CfnCluster.EncryptionInTransitProperty(
                    client_broker="PLAINTEXT",
                    in_cluster=True,
                )
            ),
        )

    def create_ecs_cluster(self) -> ecs.Cluster:
        return ecs.Cluster(
            self,
            "BentoSaaSEcsCluster",
            vpc=self.vpc,
            default_cloud_map_namespace=ecs.CloudMapNamespaceOptions(
                name="bento-saas.local",
            )
        )

    def create_ecr_repository(self, construct_id: str, repo_name: str) -> ecr.Repository:
        return ecr.Repository(
            self,
            construct_id,
            repository_name=repo_name,
        )

    def create_fargate_service(self, construct_id: str,
                               image_name: str,
                               repo: ecr.Repository,
                               ports: list[int],
                               db: rds.DatabaseInstance,
                               additional_env_vars: dict[str, str],
                               db_pswd_prop_key: str,
                               additional_secrets: dict[str, ecs.Secret] | None,
                               schema_name: str) -> ecs.FargateService:
        task_definition = ecs.FargateTaskDefinition(
            self,
            construct_id + "Task",
            cpu=256,
            memory_limit_mib=512
        )

        env_vars = {
            "SPRING_KAFKA_BOOTSTRAP_SERVERS": getattr(self, "kafka_bootstrap_servers", ""),
        }

        if additional_env_vars is not None:
            env_vars.update(additional_env_vars)

        if db is not None:
            env_vars[
                "SPRING_DATASOURCE_URL"] = f"jdbc:postgresql://{db.db_instance_endpoint_address}:{db.db_instance_endpoint_port}/{image_name}?currentSchema={schema_name}"

        env_vars["SPRING_DATASOURCE_USERNAME"] = self.db_user_name
        env_vars["SPRING_JPA_HIBERNATE_DDL_AUTO"] = "update"
        env_vars["SPRING_SQL_INIT_MODE"] = "always"
        env_vars["SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"] = "org.hibernate.dialect.PostgreSQLDialect"
        env_vars["SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT"] = "60000"

        task_definition.add_container(
            f"{image_name}Container",
            image=ecs.ContainerImage.from_ecr_repository(repo),
            environment=env_vars,
            secrets=(
                    {
                        db_pswd_prop_key: ecs.Secret.from_secrets_manager(
                            db.secret, "password"
                        )
                    }
                    | (additional_secrets or {})
            ),
            port_mappings=[
                ecs.PortMapping(container_port=port)
                for port in ports
            ],
            logging=ecs.LogDrivers.aws_logs(
                log_group=logs.LogGroup(self, construct_id + "LogGroup",
                                        log_group_name=f"/ecs/{image_name}",
                                        removal_policy=cdk.RemovalPolicy.DESTROY,
                                        retention=logs.RetentionDays.ONE_DAY
                                        ),
                stream_prefix=construct_id
            )
        )

        return ecs.FargateService(
            self,
            construct_id,
            cluster=self.ecs_cluster,
            task_definition=task_definition,
            assign_public_ip=False,
            service_name=image_name,
            security_groups=[self.ecs_sg],
            cloud_map_options=ecs.CloudMapOptions(
                name=image_name
            )
        )

    def create_api_gateway_service(self, repo: ecr.Repository):
        construct_id = "ApiGateway"
        image_name = "api-gateway"

        task_definition = ecs.FargateTaskDefinition(
            self,
            f"{construct_id}Task",
            cpu=256,
            memory_limit_mib=512
        )

        task_definition.add_container(
            f"{construct_id}Container",
            image=ecs.ContainerImage.from_ecr_repository(repo),
            environment={"SPRING_PROFILES_ACTIVE": "prod"},
            port_mappings=[ecs.PortMapping(container_port=4003)],
            logging=ecs.LogDrivers.aws_logs(
                log_group=logs.LogGroup(self, construct_id + "LogGroup",
                                        log_group_name=f"/ecs/{image_name}",
                                        removal_policy=cdk.RemovalPolicy.DESTROY,
                                        retention=logs.RetentionDays.ONE_DAY
                                        ),
                stream_prefix=construct_id
            )
        )

        api_gateway_sg = ec2.SecurityGroup(
            self,
            "ApiGatewaySG",
            vpc=self.vpc,
            allow_all_outbound=True,
            description="SG for ApiGateway (SCG) tasks"
        )

        # ACM certificate for HTTPS on the public ALB
        api_gateway_cert_arn = cdk.CfnParameter(
            self,
            "ApiGatewayAcmCertArn",
            type="String",
            description="ACM certificate ARN for the ApiGateway public ALB (HTTPS 443)"
        )
        api_gateway_cert = acm.Certificate.from_certificate_arn(
            self,
            "ApiGatewayAcmCert",
            api_gateway_cert_arn.value_as_string
        )

        api_gateway_with_alb = ecs_patterns.ApplicationLoadBalancedFargateService(
            self,
            f"{construct_id}Service",
            cluster=self.ecs_cluster,
            service_name=image_name,
            task_definition=task_definition,
            desired_count=1,
            health_check_grace_period=cdk.Duration.seconds(60),
            open_listener=False,
            security_groups=[api_gateway_sg],
        )

        # Add HTTPS listener (443) with ACM cert and forward to the same target group
        https_listener = api_gateway_with_alb.load_balancer.add_listener(
            "ApiGatewayHttpsListener",
            port=443,
            protocol=elbv2.ApplicationProtocol.HTTPS,
            certificates=[api_gateway_cert]
        )
        https_listener.add_target_groups(
            "ApiGatewayHttpsTargets",
            target_groups=[api_gateway_with_alb.target_group]
        )

        # Make HTTP (80) always redirect to HTTPS (443)
        api_gateway_with_alb.listener.add_action(
            "RedirectHttpToHttps",
            action=elbv2.ListenerAction.redirect(
                protocol="HTTPS",
                port="443",
                permanent=True
            )
        )

        # Outputs (pattern auto-outputs http://...; add our own https://...)
        cdk.CfnOutput(
            self,
            "ApiGatewayHttpsURL",
            value=f"https://{api_gateway_with_alb.load_balancer.load_balancer_dns_name}",
        )

        cdk.CfnOutput(
            self,
            "ApiGatewayHttpURL",
            value=f"http://{api_gateway_with_alb.load_balancer.load_balancer_dns_name}",
        )

        # Public ingress to the ALB: allow 80 (for redirect) and 443 (HTTPS)
        lb_sg = api_gateway_with_alb.load_balancer.connections.security_groups[0]
        lb_sg.add_ingress_rule(
            ec2.Peer.ipv4("0.0.0.0/0"),
            ec2.Port.tcp(80),
            "Allow HTTP (redirect to HTTPS)"
        )
        lb_sg.add_ingress_rule(
            ec2.Peer.ipv4("0.0.0.0/0"),
            ec2.Port.tcp(443),
            "Allow HTTPS"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4004,
            "Allow Api Gateway's HTTP To User Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4000,
            "Allow Api Gateway's HTTP To Plan Management Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4001,
            "Allow Api Gateway's HTTP To Subscription Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4002,
            "Allow Api Gateway's HTTP To Invoice Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4005,
            "Allow Api Gateway's HTTP To Notification Service"
        )

    def add_api_gateway_sg_to_ecs_service(self, api_gateway_sg: ec2.SecurityGroup, port: int, description: str):
        self.ecs_sg.add_ingress_rule(
            api_gateway_sg,
            ec2.Port.tcp(port),
            description
        )

    def create_frontend_infra(self):
        # Frontend (React SPA) hosting: S3 (private) + CloudFront

        frontend_bucket = self.create_private_bucket("FrontendBucket")

        # CloudFront Origin Access Identity (simple + reliable for S3 origins)
        oai = cloudfront.OriginAccessIdentity(self, "FrontendOAI")
        frontend_bucket.grant_read(oai)

        # ACM certificate (must be in us-east-1 for CloudFront)
        frontend_cert_arn = cdk.CfnParameter(
            self,
            "FrontendAcmCertArn",
            type="String",
            description="ACM certificate ARN (us-east-1) for CloudFront custom domain"
        )

        frontend_domain_name = cdk.CfnParameter(
            self,
            "FrontendDomainName",
            type="String",
            description="Custom domain name for frontend (e.g. app.bento.nyihtuun.com)"
        )

        frontend_cert = acm.Certificate.from_certificate_arn(
            self,
            "FrontendAcmCert",
            frontend_cert_arn.value_as_string
        )

        distribution = cloudfront.Distribution(
            self,
            "FrontendDistribution",
            default_behavior=cloudfront.BehaviorOptions(
                origin=origins.S3Origin(frontend_bucket, origin_access_identity=oai),
                viewer_protocol_policy=cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
                cache_policy=cloudfront.CachePolicy.CACHING_OPTIMIZED,
            ),
            default_root_object="index.html",
            domain_names=[frontend_domain_name.value_as_string],
            certificate=frontend_cert,
            error_responses=[
                cloudfront.ErrorResponse(
                    http_status=403,
                    response_http_status=200,
                    response_page_path="/index.html",
                    ttl=cdk.Duration.seconds(0),
                ),
                cloudfront.ErrorResponse(
                    http_status=404,
                    response_http_status=200,
                    response_page_path="/index.html",
                    ttl=cdk.Duration.seconds(0),
                ),
            ],
        )

        cdk.CfnOutput(
            self,
            "FrontendBucketName",
            value=frontend_bucket.bucket_name,
        )

        cdk.CfnOutput(
            self,
            "FrontendCloudFrontURL",
            value=f"https://{distribution.distribution_domain_name}",
        )

    def create_private_bucket(self, construct_id: str) -> s3.Bucket:
        return s3.Bucket(
            self,
            construct_id,
            block_public_access=s3.BlockPublicAccess.BLOCK_ALL,
            encryption=s3.BucketEncryption.S3_MANAGED,
            enforce_ssl=True,
            removal_policy=cdk.RemovalPolicy.DESTROY,
            auto_delete_objects=True,
        )
