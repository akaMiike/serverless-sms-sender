package org.example.model;

public class ImageData {
    String id;
    String name;
    String createdAt;
    String downloadUrl;

    public ImageData(String downloadUrl, String id, String name, String createdAt) {
        this.downloadUrl = downloadUrl;
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
