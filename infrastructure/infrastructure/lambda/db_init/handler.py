import json
import boto3
import psycopg2
import os

def handler(event, context):
    secret_arn = os.environ["DB_SECRET_ARN"]
    region = os.environ["AWS_REGION"]

    sm = boto3.client("secretsmanager", region_name=region)
    secret = json.loads(
        sm.get_secret_value(SecretId=secret_arn)["SecretString"]
    )

    conn = psycopg2.connect(
        host=os.environ["DB_HOST"],
        port=os.environ["DB_PORT"],
        user=secret["username"],
        password=secret["password"],
        dbname="postgres",
    )
    conn.autocommit = True
    cur = conn.cursor()

    databases = [
        "user-service",
        "plan-management-service",
        "subscription-service",
        "invoice-service",
        "notification-service",
    ]

    for db in databases:
        cur.execute(f"SELECT 1 FROM pg_database WHERE datname='{db}'")
        if not cur.fetchone():
            cur.execute(f'CREATE DATABASE "{db}"')

    cur.close()
    conn.close()

    return { "status": "ok" }