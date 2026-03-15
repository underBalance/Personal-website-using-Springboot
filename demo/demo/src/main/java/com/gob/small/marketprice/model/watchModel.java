package com.gob.small.marketprice.model;

public class watchModel {
    private final String brand;
    private final String slug;
    private final String fullSlug;

    public watchModel(String brand, String slug, String fullSlug) {
        this.brand = brand;
        this.slug = slug;
        this.fullSlug = fullSlug;
    }

    public String getBrand() {
        return brand;
    }

    public String getSlug() {
        return slug;
    }

    public String getFullSlug() {
        return fullSlug;
    }
}
