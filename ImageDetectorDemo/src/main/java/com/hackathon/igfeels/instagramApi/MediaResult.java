package com.hackathon.igfeels.instagramApi;

import com.google.gson.annotations.SerializedName;

public class MediaResult {
    public class ImageContainer {
        private String url;
        private int width;
        private int height;

        public ImageContainer(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public class ImageElement {
        @SerializedName("low_resolution")
        private ImageContainer lowResolution;

        @SerializedName("thumbnail")
        private ImageContainer thumbnail;

        @SerializedName("standard_resolution")
        private ImageContainer standardResolution;

        public ImageElement(ImageContainer lowResolution, ImageContainer thumbnail, ImageContainer standardResolution) {
            this.lowResolution = lowResolution;
            this.thumbnail = thumbnail;
            this.standardResolution = standardResolution;
        }

        public ImageContainer getLowResolution() {
            return lowResolution;
        }

        public ImageContainer getThumbnail() {
            return thumbnail;
        }

        public ImageContainer getStandardResolution() {
            return standardResolution;
        }
    }

    public class MediaElement {
        public final static String TYPE_IMAGE = "image";

        @SerializedName("created_time")
        private String createdTime;

        private String type;

        private ImageElement images;

        public MediaElement(String createdTime, String type, ImageElement images) {
            this.createdTime = createdTime;
            this.type = type;
            this.images = images;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public String getType() {
            return type;
        }

        public ImageElement getImages() {
            return images;
        }
    }

    public MediaElement[] data;

    public MediaResult(MediaElement[] data) {
        this.data = data;
    }

    public MediaElement[] getData() {
        return data;
    }
}