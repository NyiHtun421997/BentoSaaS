import aws_cdk as cdk
from aws_cdk import (
    Stack,
    aws_ec2 as ec2,
    aws_msk as msk,
)
from constructs import Construct

class NetworkMskStack(Stack):
    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # 1. VPC (max_azs=2)
        self.vpc = ec2.Vpc(
            self,
            "BentoSaaSVpc",
            max_azs=2
        )

        # 3. msk_sg (for MSK brokers)
        self.msk_sg = ec2.SecurityGroup(
            self,
            "MskSG",
            vpc=self.vpc,
            allow_all_outbound=True,
            description="SG for MSK brokers",
            security_group_name="MskSG"
        )

        # 5. MSK cluster using aws_cdk.aws_msk.CfnCluster
        # kafka_version must be kraft (e.g. "4.1.x.kraft")
        # broker subnets: PRIVATE_WITH_EGRESS
        # Set MSK removal policy to RETAIN to avoid rollback delete stuck in CREATING state.
        self.msk_cluster = msk.CfnCluster(
            self,
            "BentoSaaSMskCluster",
            cluster_name="bento-saas-kafka-cluster",
            kafka_version="4.1.x.kraft",
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
        self.msk_cluster.apply_removal_policy(cdk.RemovalPolicy.RETAIN)

        # 6. Outputs
        cdk.CfnOutput(self, "VpcId", value=self.vpc.vpc_id)
        cdk.CfnOutput(self, "MskClusterArn", value=self.msk_cluster.attr_arn)
