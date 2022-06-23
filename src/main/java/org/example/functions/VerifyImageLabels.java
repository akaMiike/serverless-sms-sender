package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonObject;
import org.example.model.ProcessedStatus;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.S3Object;

import java.util.Map;
import java.util.stream.Collectors;

public class VerifyImageLabels implements RequestHandler<Map<String, String>, Void> {

    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");

    final String NOTIFY_PERSON_IN_PICTURE_ARN = System.getenv("NOTIFY_PERSON_IN_PICTURE_ARN");

    @Override
    public Void handleRequest(Map<String, String> request, Context context) {
        final DynamoDbClient dbClient = DynamoDbClient
                .builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryPolicy.builder().numRetries(1).build()).build())
                .region(Region.of(CLIENT_REGION)).build();

        var imageId = request.get("imageId");
        var rekognitionClient = RekognitionClient.builder().region(Region.US_EAST_1).build();
        var imageS3Object = S3Object.builder().bucket(S3_BUCKET_NAME).name(imageId).build();
        var detectLabelsRequest = DetectLabelsRequest
                .builder()
                .maxLabels(10)
                .image(Image.builder().s3Object(imageS3Object).build())
                .build();

        try {
            var result = rekognitionClient.detectLabels(detectLabelsRequest);
            var labels = result.labels();
            var labelNames = labels.stream().map(Label::name).collect(Collectors.toList());

            var imageKey = Map.of("imageId", AttributeValue.builder().s(imageId).build());
            var updateItemRequest = UpdateItemRequest
                    .builder()
                    .key(imageKey)
                    .tableName(TABLE_NAME)
                    .updateExpression("SET #processingStatus = :value, #labels = :labelsValue")
                    .expressionAttributeNames(Map.of("#processingStatus", "processingStatus", "#labels", "labels"))
                    .expressionAttributeValues(Map
                            .of(":value", AttributeValue.builder().s(ProcessedStatus.PROCESSED.name()).build(),
                                    ":labelsValue", AttributeValue.builder().ss(labelNames).build()))
                    .build();

            dbClient.updateItem(updateItemRequest);

            var hasPersonInImage = labelNames.contains("Person");

            if (hasPersonInImage) {
                var jsonPayload = new JsonObject();
                jsonPayload.addProperty("imageId", imageId);

                var jsonBytes = SdkBytes.fromUtf8String(jsonPayload.toString());

                InvokeRequest invokeRequest = InvokeRequest
                        .builder()
                        .invocationType(InvocationType.EVENT)
                        .functionName(NOTIFY_PERSON_IN_PICTURE_ARN)
                        .payload(jsonBytes)
                        .build();
                LambdaClient.builder().build().invoke(invokeRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
