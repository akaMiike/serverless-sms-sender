package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.example.model.ImageData;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetImages implements RequestHandler<Object, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public List<ImageData> handleRequest(Object o, Context context) {
        List<ImageData> results = new ArrayList<>();

        DynamoDbClient dbClient = DynamoDbClient.builder().region(Region.of(CLIENT_REGION)).build();
        ScanRequest scanRequest = ScanRequest.builder().tableName(TABLE_NAME).build();
        ScanResponse scanResponse = dbClient.scan(scanRequest);

        //Generate an URL to show/download the image
        for(Map<String, AttributeValue> item : scanResponse.items()){
            S3Presigner presigner = S3Presigner.create();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(S3_BUCKET_NAME).key(item.get("imageId").s()).build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(10)).getObjectRequest(getObjectRequest).build();
            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

            ImageData result = new ImageData(
                    presignedGetObjectRequest.url().toString(),
                    item.get("imageId").s(),
                    item.get("name").s(),
                    item.get("createdAt").s(),
                    item.get("processingStatus").s(),
                    item.getOrDefault("labels", AttributeValue.builder().ss().build()).ss()
                    );

            results.add(result);
        }

        return results;
    }
}
