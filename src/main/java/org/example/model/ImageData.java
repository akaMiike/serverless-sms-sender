package org.example.model;

import java.util.List;

public class ImageData {
    String id;
    String name;
    String createdAt;
    String downloadUrl;

    String processingStatus;

    List<String> labels;

    public ImageData(String downloadUrl, String id, String name, String createdAt, String processingStatus, List<String> labels) {
        this.downloadUrl = downloadUrl;
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.processingStatus = processingStatus;
        this.labels = labels;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
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
