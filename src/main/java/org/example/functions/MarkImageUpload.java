package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.google.gson.Gson;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.google.gson.JsonObject;
import org.example.model.ProcessedStatus;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import java.util.List;
import java.util.Map;

public class MarkImageUpload implements RequestHandler<SQSEvent, Object> {
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");
    final String VALIDATE_LABELS_ARN = System.getenv("FUNCTION_ARN");

    @Override
    public Object handleRequest(SQSEvent sqsMessage, Context context) {
        final DynamoDbClient dbClient = DynamoDbClient
                .builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryPolicy.builder().numRetries(1).build()).build())
                .region(Region.of(CLIENT_REGION)).build();

        sqsMessage.getRecords().forEach(sqsMessage1 -> {
            S3EventNotification s3EventNotification = S3EventNotification.parseJson(sqsMessage1.getBody());
            var s3event = s3EventNotification.getRecords().get(0);

            var key = s3event.getS3().getObject().getKey();

            var imageKey = Map.of("imageId", AttributeValue.builder().s(key).build());
            var updateItemRequest = UpdateItemRequest
                    .builder()
                    .key(imageKey)
                    .tableName(TABLE_NAME)
                    .updateExpression("SET #processingStatus = :value")
                    .expressionAttributeNames(Map.of("#processingStatus", "processingStatus"))
                    .expressionAttributeValues(Map.of(":value", AttributeValue.builder().s(ProcessedStatus.UPLOADED.name()).build()))
                    .build();

            try {
                dbClient.updateItem(updateItemRequest);

                var jsonPayload = new JsonObject();
                jsonPayload.addProperty("imageId", key);

                var jsonBytes = SdkBytes.fromUtf8String(jsonPayload.toString());

                InvokeRequest invokeRequest = InvokeRequest
                        .builder()
                        .invocationType(InvocationType.EVENT)
                        .functionName(VALIDATE_LABELS_ARN)
                        .payload(jsonBytes)
                        .build();
                LambdaClient.builder().build().invoke(invokeRequest);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
                System.exit(1);
            }
        });

        return null;
    }
}
