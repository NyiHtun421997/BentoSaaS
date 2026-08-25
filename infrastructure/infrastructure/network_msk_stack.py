import aws_cdk as cdk
from aws_cdk import Stack, aws_ec2 as ec2
from constructs import Construct


class NetworkMskStack(Stack):
    """Low-cost network stack.

    The historical name is retained so app.py does not need to change. Kafka is
    no longer provisioned here; it runs in Docker Compose on the application EC2
    instance created by ServicesStack.
    """

    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        # The ALB needs subnets in at least two AZs. The application EC2 instance
        # also uses a public subnet so it can reach ECR/the internet without a
        # continuously billed NAT Gateway. Its SG still accepts traffic only
        # from the ALB.
        self.vpc = ec2.Vpc(
            self,
            "BentoSaaSVpc",
            max_azs=2,
            nat_gateways=0,
            subnet_configuration=[
                ec2.SubnetConfiguration(
                    name="Public",
                    subnet_type=ec2.SubnetType.PUBLIC,
                    cidr_mask=24,
                )
            ],
        )

        cdk.CfnOutput(self, "VpcId", value=self.vpc.vpc_id)
