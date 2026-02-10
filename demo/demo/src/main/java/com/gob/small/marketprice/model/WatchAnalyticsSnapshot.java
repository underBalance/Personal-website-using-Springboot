package com.gob.small.marketprice.model;

import java.math.BigDecimal;

public class WatchAnalyticsSnapshot {
    private String description;
    private String twitterTitle;
    private Details details;
    private Summary summary;

    public WatchAnalyticsSnapshot(
            String description,
            String twitterTitle,
            Details details,
            Summary summary
    ) {
        this.description = description;
        this.twitterTitle = twitterTitle;
        this.details = details;
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public String getTwitterTitle() {
        return twitterTitle;
    }

    public Details getDetails() {
        return details;
    }

    public Summary getSummary() {
        return summary;
    }

    public static class Details {
        private String brand;
        private String model;
        private BigDecimal retailPrice;
        private BigDecimal retailPriceChangePercent;
        private String caliber;
        private String complications;
        private String crystal;
        private String dial;
        private String diameter;
        private String material;
        private String movement;
        private String powerReserve;
        private String waterResistance;

        public Details(
                String brand,
                String model,
                BigDecimal retailPrice,
                BigDecimal retailPriceChangePercent,
                String caliber,
                String complications,
                String crystal,
                String dial,
                String diameter,
                String material,
                String movement,
                String powerReserve,
                String waterResistance
        ) {
            this.brand = brand;
            this.model = model;
            this.retailPrice = retailPrice;
            this.retailPriceChangePercent = retailPriceChangePercent;
            this.caliber = caliber;
            this.complications = complications;
            this.crystal = crystal;
            this.dial = dial;
            this.diameter = diameter;
            this.material = material;
            this.movement = movement;
            this.powerReserve = powerReserve;
            this.waterResistance = waterResistance;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public BigDecimal getRetailPrice() {
            return retailPrice;
        }

        public BigDecimal getRetailPriceChangePercent() {
            return retailPriceChangePercent;
        }

        public String getCaliber() {
            return caliber;
        }

        public String getComplications() {
            return complications;
        }

        public String getCrystal() {
            return crystal;
        }

        public String getDial() {
            return dial;
        }

        public String getDiameter() {
            return diameter;
        }

        public String getMaterial() {
            return material;
        }

        public String getMovement() {
            return movement;
        }

        public String getPowerReserve() {
            return powerReserve;
        }

        public String getWaterResistance() {
            return waterResistance;
        }
    }

    public static class Summary {
        private BigDecimal closingPrice;
        private BigDecimal openingPrice;
        private BigDecimal minimumPrice;
        private BigDecimal maximumPrice;
        private BigDecimal relativePerformancePercent;
        private BigDecimal absolutePerformance;

        public Summary(
                BigDecimal closingPrice,
                BigDecimal openingPrice,
                BigDecimal minimumPrice,
                BigDecimal maximumPrice,
                BigDecimal relativePerformancePercent,
                BigDecimal absolutePerformance
        ) {
            this.closingPrice = closingPrice;
            this.openingPrice = openingPrice;
            this.minimumPrice = minimumPrice;
            this.maximumPrice = maximumPrice;
            this.relativePerformancePercent = relativePerformancePercent;
            this.absolutePerformance = absolutePerformance;
        }

        public BigDecimal getClosingPrice() {
            return closingPrice;
        }

        public BigDecimal getOpeningPrice() {
            return openingPrice;
        }

        public BigDecimal getMinimumPrice() {
            return minimumPrice;
        }

        public BigDecimal getMaximumPrice() {
            return maximumPrice;
        }

        public BigDecimal getRelativePerformancePercent() {
            return relativePerformancePercent;
        }

        public BigDecimal getAbsolutePerformance() {
            return absolutePerformance;
        }
    }
}
