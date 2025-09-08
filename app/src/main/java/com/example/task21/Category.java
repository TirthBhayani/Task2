package com.example.task21;

import java.util.List;

public class Category {
    private String name;
    private List<String> imagesBase64;

    public Category() {} // Required by Firebase

    public Category(String name, List<String> imagesBase64) {
        this.name = name;
        this.imagesBase64 = imagesBase64;
    }

    public String getName() { return name; }
    public List<String> getImagesBase64() { return imagesBase64; }

    public void setName(String name) { this.name = name; }
    public void setImagesBase64(List<String> imagesBase64) { this.imagesBase64 = imagesBase64; }
}
