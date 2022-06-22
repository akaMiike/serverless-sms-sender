package org.example.model;

public class UploadImageData {
    String uploadUrl;

    public UploadImageData(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
