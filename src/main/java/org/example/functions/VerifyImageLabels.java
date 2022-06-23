package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

import java.util.Map;

public class VerifyImageLabels implements RequestHandler<Map<String, String>, Void> {

    final String S3_BUCKET_NAME = System.getenv("FILE_UPLOAD_BUCKET_NAME");
    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");

    @Override
    public Void handleRequest(Map<String, String> request, Context context) {
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
            System.out.println(labels);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
