import aws_cdk as cdk
from aws_cdk import (
    Stack,
    aws_ec2 as ec2,
    aws_msk as msk,
    aws_iam as iam,
)
from constructs import Construct


class NetworkMskStack(Stack):
    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # 1. VPC (max_azs=2)
        self.vpc = ec2.Vpc(
            self,
            "BentoSaaSVpc",
            max_azs=2,
            nat_gateways=1
        )

        # 3. msk_sg (for MSK brokers)
        # self.msk_sg = ec2.SecurityGroup(
        #     self,
        #     "MskSG",
        #     vpc=self.vpc,
        #     allow_all_outbound=True,
        #     description="SG for MSK brokers",
        #     security_group_name="MskSG"
        # )

        # 5. MSK cluster using aws_cdk.aws_msk.CfnCluster
        # kafka_version must be kraft (e.g. "4.1.x.kraft")
        # broker subnets: PRIVATE_WITH_EGRESS
        # Set MSK removal policy to RETAIN to avoid rollback delete stuck in CREATING state.
        # self.msk_cluster = msk.CfnCluster(
        #     self,
        #     "BentoSaaSMskCluster",
        #     cluster_name="bento-saas-kafka-cluster",
        #     kafka_version="4.1.x.kraft",
        #     number_of_broker_nodes=1,
        #     broker_node_group_info=msk.CfnCluster.BrokerNodeGroupInfoProperty(
        #         instance_type="kafka.m5.large",
        #         client_subnets=self.vpc.select_subnets(subnet_type=ec2.SubnetType.PRIVATE_WITH_EGRESS).subnet_ids,
        #         broker_az_distribution="DEFAULT",
        #         security_groups=[self.msk_sg.security_group_id]
        #     ),
        #     encryption_info=msk.CfnCluster.EncryptionInfoProperty(
        #         encryption_in_transit=msk.CfnCluster.EncryptionInTransitProperty(
        #             client_broker="PLAINTEXT",
        #             in_cluster=True,
        #         )
        #     ),
        # )
        # self.msk_cluster.apply_removal_policy(cdk.RemovalPolicy.RETAIN)
        #
        # # 6. Outputs
        # cdk.CfnOutput(self, "VpcId", value=self.vpc.vpc_id)
        # cdk.CfnOutput(self, "MskClusterArn", value=self.msk_cluster.attr_arn)

        # 2. Security Group for Kafka EC2
        self.msk_sg = ec2.SecurityGroup(
            self,
            "KafkaEc2SG",
            vpc=self.vpc,
            allow_all_outbound=True,
            description="SG for Kafka EC2 broker",
            security_group_name="KafkaEc2SG"
        )

        # Allow PLAINTEXT Kafka
        self.msk_sg.add_ingress_rule(
            ec2.Peer.any_ipv4(),
            ec2.Port.tcp(9092),
            "Allow Kafka PLAINTEXT"
        )

        # IAM role for Kafka EC2 (SSM access)
        kafka_role = iam.Role(
            self,
            "KafkaEc2Role",
            assumed_by=iam.ServicePrincipal("ec2.amazonaws.com"),
        )
        kafka_role.add_managed_policy(
            iam.ManagedPolicy.from_aws_managed_policy_name("AmazonSSMManagedInstanceCore")
        )

        # 3. Kafka EC2 Instance (cheap single-node broker)
        kafka_instance = ec2.Instance(
            self,
            "KafkaBrokerInstance",
            vpc=self.vpc,
            instance_type=ec2.InstanceType("t4g.small"),
            machine_image=ec2.MachineImage.latest_amazon_linux2023(
                cpu_type=ec2.AmazonLinuxCpuType.ARM_64
            ),
            vpc_subnets=ec2.SubnetSelection(
                subnet_type=ec2.SubnetType.PRIVATE_WITH_EGRESS
            ),
            security_group=self.msk_sg,
            key_name=None,
            role=kafka_role,
        )

        # Install Kafka automatically via user data
        kafka_instance.add_user_data(
            "#!/bin/bash",
            "set -euxo pipefail",
            "exec > >(tee /var/log/kafka-bootstrap.log | logger -t kafka-bootstrap -s 2>/dev/console) 2>&1",

            "dnf update -y",
            "dnf install -y java-17-amazon-corretto-headless curl tar",

            "KAFKA_VERSION=3.7.0",
            "KAFKA_TGZ=kafka_2.13-${KAFKA_VERSION}.tgz",
            "cd /opt",

            # Use Apache archive + -L to follow redirects; downloads.apache.org can return HTML/redirects
            "curl -fL -o ${KAFKA_TGZ} https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_TGZ}",
            "file ${KAFKA_TGZ}",
            "tar -xzf ${KAFKA_TGZ}",
            "rm -f ${KAFKA_TGZ}",
            "mv kafka_2.13-${KAFKA_VERSION} kafka",

            "mkdir -p /var/lib/kafka/data",
            "chown -R ec2-user:ec2-user /var/lib/kafka",

            # Kafka KRaft settings
            "CFG=/opt/kafka/config/server.properties",
            "echo 'process.roles=broker,controller' >> ${CFG}",
            "echo 'node.id=1' >> ${CFG}",
            "echo 'controller.quorum.voters=1@localhost:9093' >> ${CFG}",
            "echo 'listeners=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093' >> ${CFG}",
            "echo \"advertised.listeners=PLAINTEXT://$(curl -sf http://169.254.169.254/latest/meta-data/local-ipv4):9092\" >> ${CFG}",
            "echo 'listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT' >> ${CFG}",
            "echo 'controller.listener.names=CONTROLLER' >> ${CFG}",
            "echo 'inter.broker.listener.name=PLAINTEXT' >> ${CFG}",
            "echo 'log.dirs=/var/lib/kafka/data' >> ${CFG}",
            "echo 'offsets.topic.replication.factor=1' >> ${CFG}",
            "echo 'transaction.state.log.replication.factor=1' >> ${CFG}",
            "echo 'transaction.state.log.min.isr=1' >> ${CFG}",
            "echo 'group.initial.rebalance.delay.ms=0' >> ${CFG}",
            "echo 'default.replication.factor=1' >> ${CFG}",
            "echo 'num.partitions=1' >> ${CFG}",

            # Format KRaft storage (idempotent via marker file)
            "if [ ! -f /var/lib/kafka/.kraft_formatted ]; then \n"
            "  CLUSTER_ID=$(/opt/kafka/bin/kafka-storage.sh random-uuid); \n"
            "  /opt/kafka/bin/kafka-storage.sh format -t ${CLUSTER_ID} -c ${CFG}; \n"
            "  touch /var/lib/kafka/.kraft_formatted; \n"
            "fi",

            # systemd service
            "cat > /etc/systemd/system/kafka.service <<'EOF'\n"
            "[Unit]\n"
            "Description=Apache Kafka (KRaft)\n"
            "After=network.target\n\n"
            "[Service]\n"
            "Type=simple\n"
            "User=root\n"
            "ExecStart=/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties\n"
            "Restart=on-failure\n"
            "RestartSec=5\n"
            "LimitNOFILE=100000\n\n"
            "[Install]\n"
            "WantedBy=multi-user.target\n"
            "EOF",

            "systemctl daemon-reload",
            "systemctl enable kafka",
            "systemctl start kafka",
            "systemctl status kafka --no-pager -l || true",
            "ss -lntp | egrep ':9092|:9093' || true",
        )

        # 4. Outputs (same pattern as MSK before)
        cdk.CfnOutput(self, "VpcId", value=self.vpc.vpc_id)
        cdk.CfnOutput(
            self,
            "KafkaBootstrapServers",
            value=f"{kafka_instance.instance_private_dns_name}:9092"
        )
        cdk.CfnOutput(
            self,
            "MskSecurityGroupId",
            value=self.msk_sg.security_group_id
        )
