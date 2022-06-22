package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.example.model.UploadImageData;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostImage implements RequestHandler<Map<String, Object>, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");
        String newKey = UUID.randomUUID().toString();
        String imageName = !pathParams.get("imageName").isBlank() ? pathParams.get("imageName") : UUID.randomUUID().toString();

        //Generate an presigned URL for image upload
        S3Presigner presigner = S3Presigner.create();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(S3_BUCKET_NAME).key(newKey).build();
        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(60)).putObjectRequest(putObjectRequest).build();
        PresignedPutObjectRequest presignedPutObjectRequest = presigner.presignPutObject(putObjectPresignRequest);
        String uploadURL = presignedPutObjectRequest.url().toString();

        //Connect with DynamoDB and prepares the values to be inserted
        DynamoDbClient dbClient = DynamoDbClient.builder().region(Region.of(CLIENT_REGION)).build();
        HashMap<String,AttributeValue> imageValues = new HashMap<>();
        imageValues.put("Id", AttributeValue.builder().s(newKey).build());
        imageValues.put("name", AttributeValue.builder().s(imageName).build());
        imageValues.put("createdAt", AttributeValue.builder().s(ZonedDateTime.now().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(imageValues)
                .build();

        try {
            dbClient.putItem(request);
        }
        catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return new UploadImageData(uploadURL);

    }
}
