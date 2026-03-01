#!/usr/bin/env python3
import os

import aws_cdk as cdk

from infrastructure.network_msk_stack import NetworkMskStack
from infrastructure.services_stack import ServicesStack

app = cdk.App()

# Shared environment
env = cdk.Environment(
    account=os.getenv('CDK_DEFAULT_ACCOUNT'), 
    region=os.getenv('CDK_DEFAULT_REGION')
)

# 1. Network and MSK Stack
NetworkMskStack(app, "NetworkMskStack", env=env)

# 2. Services Stack
# Requires vpc_id passed via context: -c vpc_id=vpc-xxxxxx
vpc_id = app.node.try_get_context("vpc_id")

if not vpc_id:
    # We only raise error if we are specifically trying to synthesize/deploy ServicesStack
    # In a real CI/CD, we might want to be more selective.
    # But for CLI usage as requested:
    import sys
    if "ServicesStack" in sys.argv:
        raise ValueError("Missing context variable 'vpc_id'. Use: -c vpc_id=<VpcId>")

ServicesStack(app, "ServicesStack", vpc_id=vpc_id, env=env)

app.synth()

# Commands to deploy:
# 1. cdk deploy NetworkMskStack
# 2. aws kafka get-bootstrap-brokers --cluster-arn <MskClusterArn from Output>
# 3. cdk deploy ServicesStack -c vpc_id=<VpcId from Output> \
#    --parameters KafkaBootstrapServers="<brokers>" \
#    --parameters JwtSecretArn="..." \
#    --parameters StripeSecretKeyArn="..." \
#    --parameters StripeWebhookSecretArn="..." \
#    --parameters FrontendAcmCertArn="..." \
#    --parameters FrontendDomainName="..."
