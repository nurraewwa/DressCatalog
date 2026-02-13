package com.example.dresscatalog.model;

import java.io.Serializable;
import java.util.List;

public class Dress implements Serializable {

    public int id;


    public String category; // wedding / evening


    public String title;
    public String sku;
    public int priceSom;
    public String currency;


    public String color;
    public String silhouette;

    public List<String> materials;
    public List<String> features;
    public List<String> style;


    public String imageUrl;
    public List<String> imageUrls;


    public String sourceUrl;
}
