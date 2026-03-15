package com.gob.small.marketprice.model.ranking;

public class FinancialRankingItem {
    private final String brand;
    private final String model;
    private final double roiPercent;
    private final double volatility;
    private final double sharpeRatio;
    private final double momentumPercent;
    private final double drawdown;
    private final double maxDrawdown;
    private final String momentumWindow;
    private final double score;

    public FinancialRankingItem(
            String brand,
            String model,
            double roiPercent,
            double volatility,
            double sharpeRatio,
            double momentumPercent,
            double drawdown,
            double maxDrawdown,
            String momentumWindow,
            double score
    ) {
        this.brand = brand;
        this.model = model;
        this.roiPercent = roiPercent;
        this.volatility = volatility;
        this.sharpeRatio = sharpeRatio;
        this.momentumPercent = momentumPercent;
        this.drawdown = drawdown;
        this.maxDrawdown = maxDrawdown;
        this.momentumWindow = momentumWindow;
        this.score = score;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getRoiPercent() {
        return roiPercent;
    }

    public double getVolatility() {
        return volatility;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public double getSharpeLike() {
        return sharpeRatio;
    }

    public double getMomentumPercent() {
        return momentumPercent;
    }

    public double getDrawdown() {
        return drawdown;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public String getMomentumWindow() {
        return momentumWindow;
    }

    public double getScore() {
        return score;
    }
}
