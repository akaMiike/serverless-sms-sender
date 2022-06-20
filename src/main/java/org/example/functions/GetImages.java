package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetImages implements RequestHandler<Object, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");

    @Override
    public Object handleRequest(Object o, Context context) {
        var s3Client = S3Client.builder().region(Region.of(CLIENT_REGION)).build();

        var result = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(S3_BUCKET_NAME).build());
        List<String> keys  = result.contents().stream().map(S3Object::key).collect(Collectors.toList());

        return keys;
    }
}
