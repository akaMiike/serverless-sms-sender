package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.example.model.ImageResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetImages implements RequestHandler<Object, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");

    @Override
    public List<ImageResponse> handleRequest(Object o, Context context) {
        List<ImageResponse> results = new ArrayList<>();

        var s3Client = S3Client.builder().region(Region.of(CLIENT_REGION)).build();
        var clientResult = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(S3_BUCKET_NAME).build());
        List<String> keys  = clientResult.contents().stream().map(S3Object::key).collect(Collectors.toList());

        //Generate an URL to show/download the image
        for(String key : keys){
            S3Presigner presigner = S3Presigner.create();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(S3_BUCKET_NAME).key(key).build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(10)).getObjectRequest(getObjectRequest).build();
            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

            ImageResponse result = new ImageResponse(presignedGetObjectRequest.url().toString(), key);
            results.add(result);
        }

        return results;
    }
}
