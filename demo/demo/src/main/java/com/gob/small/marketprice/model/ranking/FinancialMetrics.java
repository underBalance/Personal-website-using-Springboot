package com.gob.small.marketprice.model.ranking;

public class FinancialMetrics {
    private final double roi;
    private final double roiPercent;
    private final double volatility;
    private final double volatilityPercent;
    private final double sharpeRatio;
    private final double momentum;
    private final double momentumPercent;
    private final String momentumWindow;
    private final double drawdown;
    private final double drawdownPercent;
    private final double maxDrawdown;
    private final double maxDrawdownPercent;

    public FinancialMetrics(
            double roi,
            double roiPercent,
            double volatility,
            double volatilityPercent,
            double sharpeRatio,
            double momentum,
            double momentumPercent,
            String momentumWindow,
            double drawdown,
            double drawdownPercent,
            double maxDrawdown,
            double maxDrawdownPercent
    ) {
        this.roi = roi;
        this.roiPercent = roiPercent;
        this.volatility = volatility;
        this.volatilityPercent = volatilityPercent;
        this.sharpeRatio = sharpeRatio;
        this.momentum = momentum;
        this.momentumPercent = momentumPercent;
        this.momentumWindow = momentumWindow;
        this.drawdown = drawdown;
        this.drawdownPercent = drawdownPercent;
        this.maxDrawdown = maxDrawdown;
        this.maxDrawdownPercent = maxDrawdownPercent;
    }

    public double getRoi() {
        return roi;
    }

    public double getRoiPercent() {
        return roiPercent;
    }

    public double getVolatility() {
        return volatility;
    }

    public double getVolatilityPercent() {
        return volatilityPercent;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public double getMomentum() {
        return momentum;
    }

    public double getMomentumPercent() {
        return momentumPercent;
    }

    public String getMomentumWindow() {
        return momentumWindow;
    }

    public double getDrawdown() {
        return drawdown;
    }

    public double getDrawdownPercent() {
        return drawdownPercent;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getMaxDrawdownPercent() {
        return maxDrawdownPercent;
    }
}
