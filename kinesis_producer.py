import random
import boto3
import uuid

region = 'ap-southeast-2'
kinesisStreamName = 'kinesis_twitter_20'
kinesis = boto3.client('kinesis',region_name=region)
count=0

while True:

        datetimeid = random.randint(0, 400)
        customerid = random.randint(0, 8000)
        grosssolar = random.uniform(1,10)
        netsolar = random.uniform(5,15)
        load = random.random()
        controlledload = random.random()
        patitionKey = str(uuid.uuid4())

        putString = str(datetimeid)+","+str(customerid)+","+str(grosssolar)+","+str(netsolar)+","+str(load)+","+str(controlledload)

        if count == 0:
                putRecordsList = [{'Data': putString, 'PartitionKey': patitionKey }]
        else:
                putRecordsList.append({'Data': putString, 'PartitionKey': patitionKey })

        if count == 499:
                result = kinesis.put_records(Records=putRecordsList,StreamName=kinesisStreamName)
                count=0
        else:
                count += 1
