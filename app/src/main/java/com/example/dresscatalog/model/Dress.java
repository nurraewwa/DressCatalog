package com.example.dresscatalog.model;

import java.io.Serializable;
import java.util.List;

public class Dress implements Serializable {

    public int id;

    // Для фильтрации (НЕ показываем в UI)
    public String category; // wedding / evening

    // Основное
    public String title;
    public String sku;          // Артикул
    public int priceSom;        // Цена в сомах
    public String currency;     // KGS

    // Описание
    public String color;        // строка (например: "красный, синий")
    public String silhouette;

    public List<String> materials;
    public List<String> features;
    public List<String> style;

    // Изображения
    public String imageUrl;         // может быть null
    public List<String> imageUrls;  // для свайпа фото

    // Источник (необязательно использовать)
    public String sourceUrl;
}
