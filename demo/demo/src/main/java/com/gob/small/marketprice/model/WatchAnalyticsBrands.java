package com.gob.small.marketprice.model;

import java.util.List;

public class WatchAnalyticsBrands {
    private List<Brand> brands;

    public WatchAnalyticsBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public static class Brand {
        private String name;
        private String slug;

        public Brand(String name, String slug) {
            this.name = name;
            this.slug = slug;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }
}
