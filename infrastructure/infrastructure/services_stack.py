import aws_cdk as cdk
from aws_cdk import (
    Stack,
    aws_ec2 as ec2,
    aws_rds as rds,
    aws_ecs as ecs,
    aws_logs as logs,
    aws_certificatemanager as acm,
    aws_secretsmanager as secretsmanager,
    aws_iam as iam,
    aws_lambda as _lambda,
    aws_s3 as s3,
    aws_cloudfront as cloudfront,
    aws_cloudfront_origins as origins,
    custom_resources as cr,
    aws_elasticloadbalancingv2 as elbv2,
    aws_ecs_patterns as ecs_patterns,
)
from aws_cdk.aws_lambda_python_alpha import PythonFunction
from constructs import Construct


class ServicesStack(Stack):
    vpc: ec2.IVpc
    ecs_cluster: ecs.Cluster
    db_name = "bento_saas"
    db_user_name = "postgres"
    ecs_sg: ec2.SecurityGroup
    rds_sg: ec2.SecurityGroup
    kafka_bootstrap_servers: str
    jwt_secret: secretsmanager.ISecret
    stripe_secret_key: secretsmanager.ISecret
    stripe_webhook_secret: secretsmanager.ISecret
    media_bucket: s3.Bucket

    def __init__(self, scope: Construct, construct_id: str, vpc_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # 1. Import VPC
        self.vpc = ec2.Vpc.from_lookup(self, "BentoSaaSVpc", vpc_id=vpc_id)

        # 2. Security Groups
        self.ecs_sg = ec2.SecurityGroup(self, "EcsServicesSG", vpc=self.vpc, allow_all_outbound=True)
        # Allow ECS services to communicate with each other (HTTP ports)
        self.ecs_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp_range(4000, 4005),
            "Allow ECS service-to-service HTTP communication"
        )

        # Allow ECS services to communicate with each other (gRPC ports)
        self.ecs_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp_range(9000, 9002),
            "Allow ECS service-to-service gRPC communication"
        )
        msk_sg_id = cdk.CfnParameter(
            self,
            "MskSecurityGroupId",
            type="String",
            description="Security Group ID of MSK brokers"
        )

        msk_sg = ec2.SecurityGroup.from_security_group_id(
            self,
            "ImportedMskSG",
            msk_sg_id.value_as_string
        )

        msk_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp(9092),
            "Allow ECS services to connect to MSK"
        )

        self.rds_sg = ec2.SecurityGroup(self, "RdsSG", vpc=self.vpc, allow_all_outbound=True)
        self.rds_sg.add_ingress_rule(
            self.ecs_sg,
            ec2.Port.tcp(5432),
            "Allow ECS to Postgres"
        )

        # 3. Kafka Bootstrap Parameter
        kafka_bootstrap_param = cdk.CfnParameter(
            self,
            "KafkaBootstrapServers",
            type="String",
            description="MSK Bootstrap Broker string"
        )
        self.kafka_bootstrap_servers = kafka_bootstrap_param.value_as_string

        # 4. DB configs
        bento_saas_db = self.create_database_instance("BentoSaaSDB")
        bento_saas_db.connections.allow_default_port_from(self.ecs_sg)

        # 5. DB init lambda
        db_init_lambda = PythonFunction(
            self,
            "DBInitLambda",
            entry="infrastructure/lambda/db_init",
            index="handler.py",
            handler="handler",
            runtime=_lambda.Runtime.PYTHON_3_12,
            timeout=cdk.Duration.minutes(5),
            memory_size=1024,
            vpc=self.vpc,
            vpc_subnets=ec2.SubnetSelection(
                subnet_type=ec2.SubnetType.PRIVATE_WITH_EGRESS
            ),
            environment={
                "DB_SECRET_ARN": bento_saas_db.secret.secret_arn,
                "DB_HOST": bento_saas_db.db_instance_endpoint_address,
                "DB_PORT": bento_saas_db.db_instance_endpoint_port,
            },
        )
        bento_saas_db.secret.grant_read(db_init_lambda)
        bento_saas_db.connections.allow_default_port_from(db_init_lambda)
        self.rds_sg.add_ingress_rule(
            db_init_lambda.connections.security_groups[0],
            ec2.Port.tcp(5432),
            "Allow DBInitLambda to Postgres"
        )

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
        db_init_resource.node.add_dependency(bento_saas_db)
        db_init_resource.node.add_dependency(bento_saas_db.secret)

        # 6. Secrets
        jwt_secret_arn = cdk.CfnParameter(
            self,
            "JwtSecretArn",
            type="String",
            description="Secrets Manager ARN for shared JWT secret"
        )
        stripe_secret_key_arn = cdk.CfnParameter(
            self,
            "StripeSecretKeyArn",
            type="String",
            description="Secrets Manager ARN for Stripe secret key"
        )
        stripe_webhook_secret_arn = cdk.CfnParameter(
            self,
            "StripeWebhookSecretArn",
            type="String",
            description="Secrets Manager ARN for Stripe webhook secret"
        )

        self.jwt_secret = secretsmanager.Secret.from_secret_complete_arn(
            self, "JwtSecret", jwt_secret_arn.value_as_string
        )
        self.stripe_secret_key = secretsmanager.Secret.from_secret_complete_arn(
            self, "StripeSecretKey", stripe_secret_key_arn.value_as_string
        )
        self.stripe_webhook_secret = secretsmanager.Secret.from_secret_complete_arn(
            self, "StripeWebhookSecret", stripe_webhook_secret_arn.value_as_string
        )

        # 7. S3 & CloudFront
        plan_images_prefix = cdk.CfnParameter(self, "PlanImagesPrefix", type="String", default="bento_images/plan/")
        meal_images_prefix = cdk.CfnParameter(self, "MealImagesPrefix", type="String", default="bento_images/meal/")
        user_images_prefix = cdk.CfnParameter(self, "UserImagesPrefix", type="String", default="bento_images/user/")

        self.media_bucket = self.create_private_bucket("MediaBucket")
        cdk.CfnOutput(self, "MediaBucketName", value=self.media_bucket.bucket_name)

        # 8. ECS Cluster
        self.ecs_cluster = self.create_ecs_cluster()

        # 9. ECS Services
        # UserService
        user_service = self.create_fargate_service(
            "UserService",
            "user-service",
            ecs.ContainerImage.from_asset("../backend/spring-boot", file="user-service/Dockerfile"),
            [4004],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "JWT_EXPIRATION_TIME": "7200000",
                "AWS_REGION": "ap-northeast-1",
                "AWS_EXPIRATION_TIME_MIN": 60,
                "AWS_BUCKET_NAME": self.media_bucket.bucket_name
            },
            "SPRING_DATASOURCE_PASSWORD",
            {"JWT_SECRET_KEY": ecs.Secret.from_secrets_manager(self.jwt_secret)},
            "user"
        )
        user_service.node.add_dependency(bento_saas_db)
        user_service.node.add_dependency(db_init_resource)
        user_service.task_definition.task_role.add_to_principal_policy(
            iam.PolicyStatement(
                actions=["s3:GetObject", "s3:PutObject"],
                resources=[self.media_bucket.arn_for_objects(f"{user_images_prefix.value_as_string}*")],
            )
        )

        # PlanManagementService
        plan_management_service = self.create_fargate_service(
            "PlanManagementService",
            "plan-management-service",
            ecs.ContainerImage.from_asset("../backend/spring-boot", file="plan-management-service/Dockerfile"),
            [4000, 9000],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "JWT_EXPIRATION_TIME": "7200000",
                "AWS_REGION": "ap-northeast-1",
                "AWS_EXPIRATION_TIME_MIN": 60,
                "AWS_BUCKET_NAME": self.media_bucket.bucket_name
            },
            "SPRING_DATASOURCE_PASSWORD",
            {"JWT_SECRET_KEY": ecs.Secret.from_secrets_manager(self.jwt_secret)},
            "planmanagement"
        )
        plan_management_service.node.add_dependency(bento_saas_db)
        plan_management_service.node.add_dependency(db_init_resource)
        plan_management_service.node.add_dependency(user_service)
        plan_management_service.task_definition.task_role.add_to_principal_policy(
            iam.PolicyStatement(
                actions=["s3:GetObject", "s3:PutObject"],
                resources=[
                    self.media_bucket.arn_for_objects(f"{plan_images_prefix.value_as_string}*"),
                    self.media_bucket.arn_for_objects(f"{meal_images_prefix.value_as_string}*"),
                ],
            )
        )

        # SubscriptionService
        subscription_service = self.create_fargate_service(
            "SubscriptionService",
            "subscription-service",
            ecs.ContainerImage.from_asset("../backend/spring-boot", file="subscription-service/Dockerfile"),
            [4001, 9001],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "PLAN_MANAGEMENT_SERVICE_URL": "http://plan-management-service.bento-saas.local:4000/plan-management",
                "JWT_EXPIRATION_TIME": "7200000"
            },
            "SPRING_DATASOURCE_PASSWORD",
            {"JWT_SECRET_KEY": ecs.Secret.from_secrets_manager(self.jwt_secret)},
            "subscription"
        )
        subscription_service.node.add_dependency(bento_saas_db)
        subscription_service.node.add_dependency(db_init_resource)
        subscription_service.node.add_dependency(user_service)

        # InvoiceService
        invoice_service = self.create_fargate_service(
            "InvoiceService",
            "invoice-service",
            ecs.ContainerImage.from_asset("../backend/spring-boot", file="invoice-service/Dockerfile"),
            [4002, 9002],
            bento_saas_db,
            {
                "SPRING_PROFILES_ACTIVE": "prod",
                "PLAN_MANAGEMENT_SERVICE_ADDRESS": "plan-management-service.bento-saas.local",
                "PLAN_MANAGEMENT_SERVICE_PORT": "9000",
                "SUBSCRIPTION_SERVICE_ADDRESS": "subscription-service.bento-saas.local",
                "SUBSCRIPTION_SERVICE_PORT": "9001",
                "JWT_EXPIRATION_TIME": "7200000"
            },
            "SPRING_DATASOURCE_PASSWORD",
            {
                "JWT_SECRET_KEY": ecs.Secret.from_secrets_manager(self.jwt_secret),
                "STRIPE_SECRET_KEY": ecs.Secret.from_secrets_manager(self.stripe_secret_key),
                "STRIPE_WEBHOOK_SECRET": ecs.Secret.from_secrets_manager(self.stripe_webhook_secret),
            },
            "invoice"
        )
        invoice_service.node.add_dependency(bento_saas_db)
        invoice_service.node.add_dependency(db_init_resource)
        invoice_service.node.add_dependency(user_service)
        invoice_service.node.add_dependency(plan_management_service)
        invoice_service.node.add_dependency(subscription_service)

        # NotificationService
        notification_service = self.create_fargate_service(
            "NotificationService",
            "notification-service",
            ecs.ContainerImage.from_asset("../backend/go/notification-service", file="Dockerfile"),
            [4005],
            bento_saas_db,
            {
                "NOTI_DB_PARAMS_ADDRESS": bento_saas_db.db_instance_endpoint_address,
                "NOTI_DB_PARAMS_PORT": bento_saas_db.db_instance_endpoint_port,
                "NOTI_DB_PARAMS_DBNAME": "notification-service",
                'NOTI_DB_PARAMS_USER': self.db_user_name,
                "NOTI_KAFKA_PARAMS_BOOTSTRAP_SERVERS": self.kafka_bootstrap_servers,
                "NOTI_SERVER_ADDRESS": "0.0.0.0",
                "NOTI_DB_PARAMS_SSLMODE": "require"
            },
            "NOTI_DB_PARAMS_PASSWORD",
            {"NOTI_JWT_SECRET_KEY": ecs.Secret.from_secrets_manager(self.jwt_secret)},
            "notification"
        )
        notification_service.node.add_dependency(bento_saas_db)
        notification_service.node.add_dependency(db_init_resource)
        notification_service.node.add_dependency(user_service)

        # ApiGateway ECS
        self.create_api_gateway_service(
            ecs.ContainerImage.from_asset("../backend/spring-boot", file="api-gateway/Dockerfile")
        )
        self.create_frontend_infra()

    def create_database_instance(self, construct_id: str) -> rds.DatabaseInstance:
        return rds.DatabaseInstance(
            self,
            construct_id,
            engine=rds.DatabaseInstanceEngine.postgres(version=rds.PostgresEngineVersion.VER_17_2),
            vpc=self.vpc,
            instance_type=ec2.InstanceType.of(ec2.InstanceClass.BURSTABLE3, ec2.InstanceSize.MEDIUM),
            allocated_storage=20,
            credentials=rds.Credentials.from_generated_secret(self.db_user_name),
            removal_policy=cdk.RemovalPolicy.DESTROY,
            security_groups=[self.rds_sg]
        )

    def create_ecs_cluster(self) -> ecs.Cluster:
        return ecs.Cluster(
            self,
            "BentoSaaSEcsCluster",
            vpc=self.vpc,
            default_cloud_map_namespace=ecs.CloudMapNamespaceOptions(name="bento-saas.local")
        )

    def create_fargate_service(self, construct_id: str, image_name: str, image: ecs.ContainerImage, ports: list[int],
                               db: rds.DatabaseInstance, additional_env_vars: dict[str, str], db_pswd_prop_key: str,
                               additional_secrets: dict[str, ecs.Secret] | None,
                               schema_name: str) -> ecs.FargateService:
        task_definition = ecs.FargateTaskDefinition(
            self,
            construct_id + "Task",
            cpu=256,
            memory_limit_mib=512,
            runtime_platform=ecs.RuntimePlatform(
                operating_system_family=ecs.OperatingSystemFamily.LINUX,
                cpu_architecture=ecs.CpuArchitecture.ARM64,
            ),
        )

        env_vars = {
            "SPRING_KAFKA_BOOTSTRAP_SERVERS": getattr(self, "kafka_bootstrap_servers", ""),
        }

        if additional_env_vars is not None:
            env_vars.update(additional_env_vars)

        if db is not None and image_name != "notification-service":
            env_vars[
                "SPRING_DATASOURCE_URL"] = f"jdbc:postgresql://{db.db_instance_endpoint_address}:{db.db_instance_endpoint_port}/{image_name}?currentSchema={schema_name}"

        if image_name != "notification-service":
            env_vars["SPRING_DATASOURCE_USERNAME"] = self.db_user_name
            env_vars["SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"] = "org.hibernate.dialect.PostgreSQLDialect"
            env_vars["SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT"] = "60000"

        task_definition.add_container(
            f"{image_name}Container",
            image=image,
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

    def create_api_gateway_service(self, image: ecs.ContainerImage):
        construct_id = "ApiGateway"
        image_name = "api-gateway"

        task_definition = ecs.FargateTaskDefinition(
            self,
            f"{construct_id}Task",
            cpu=256,
            memory_limit_mib=512,
            runtime_platform=ecs.RuntimePlatform(
                operating_system_family=ecs.OperatingSystemFamily.LINUX,
                cpu_architecture=ecs.CpuArchitecture.ARM64,
            ),
        )

        task_definition.add_container(
            f"{construct_id}Container",
            image=image,
            environment={"SPRING_PROFILES_ACTIVE": "prod",
                         "USER_SERVICE_URL": "http://user-service.bento-saas.local:4004/user/v1",
                         "JWT_EXPIRATION_TIME": "7200000"},
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

        api_gateway_with_alb.target_group.configure_health_check(
            path="/actuator/health",
            healthy_http_codes="200"
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
            "Allow Api Gateway HTTP To User Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4000,
            "Allow Api Gateway HTTP To Plan Management Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4001,
            "Allow Api Gateway HTTP To Subscription Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4002,
            "Allow Api Gateway HTTP To Invoice Service"
        )

        self.add_api_gateway_sg_to_ecs_service(
            api_gateway_sg,
            4005,
            "Allow Api Gateway HTTP To Notification Service"
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
