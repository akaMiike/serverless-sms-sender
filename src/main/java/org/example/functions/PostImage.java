package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.File;

public class PostImage implements RequestHandler<Object, Object> {
    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");

    @Override
    public Object handleRequest(Object input, Context context) {
        //TODO: Receive file with multipart/form-data format

        /*String file_name = "";
        AmazonS3 S3 = AmazonS3ClientBuilder.standard().withRegion(CLIENT_REGION).build();
        try{
            S3.putObject(S3_BUCKET_NAME, file_name, new File("/"));
        } catch (AmazonServiceException e){
            //TODO: Return error code
        }*/

        return null;

    }
}
