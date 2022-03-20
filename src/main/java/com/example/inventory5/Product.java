package com.example.inventory5;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Product {

    private String productName;
    private Double productUnitPrice;
    private String imagePath;

    public Product() {

    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName.toLowerCase();
    }

    public Double getProductUnitPrice() {
        return productUnitPrice;
    }

    public void setProductUnitPrice(Double productUnitPrice) {
        this.productUnitPrice = productUnitPrice;
    }

    public String getTableName() {
        String[] words = getProductName().toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word:words) {
            sb.append(word.substring(0,1).toUpperCase()).append(word.substring(1));
        }
        sb.append("Table");
        return sb.toString();
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public InputStream getImage(){
        try {
            return new FileInputStream(imagePath);
        } catch (IOException e) {
            System.out.println("Failed to read the image! " + e.getMessage());
            return null;
        }
    }

    public String getImagePath() {
        return imagePath;
    }

}
