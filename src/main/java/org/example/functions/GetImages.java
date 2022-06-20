package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;

public class GetImages implements RequestHandler<Object, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");

    @Override
    public Object handleRequest(Object o, Context context) {
        AmazonS3 S3 = AmazonS3ClientBuilder.standard().withRegion(CLIENT_REGION).build();
        List<S3ObjectSummary> allObjects = S3.listObjectsV2(S3_BUCKET_NAME).getObjectSummaries();
        List<String> keys = new ArrayList<>();

        for(S3ObjectSummary object : allObjects){
            keys.add(object.getKey());
        }

        return keys;
    }
}
