package com.kpelykh.docker.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class ImageCreateResponse {

    @JsonProperty("status")
    public String status;

    @Override
    public String toString() {
        return "ImageCreateResponse{" +
                "status=" + status +
                '}';
    }
}
