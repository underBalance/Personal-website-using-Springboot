package com.gob.small.marketprice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.model.ranking.FinancialMetrics;
import com.gob.small.marketprice.model.ranking.FinancialRankingItem;

@Service
public class FinancialRankingService {
    private static final double SHARPE_WEIGHT = 0.4;
    private static final double ROI_WEIGHT = 0.3;
    private static final double MOMENTUM_WEIGHT = 0.2;
    private static final double DRAWDOWN_WEIGHT = 0.1;

    private final FinancialMetricsService financialMetricsService;

    public FinancialRankingService(FinancialMetricsService financialMetricsService) {
        this.financialMetricsService = financialMetricsService;
    }

    public List<FinancialRankingItem> computeRanking(List<WatchAnalyticsSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return List.of();
        }

        return snapshots.stream()
                .map(this::computeScore)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(FinancialRankingItem::getScore).reversed())
                .toList();
    }

    private FinancialRankingItem computeScore(WatchAnalyticsSnapshot item) {
        WatchAnalyticsSnapshot.Details details = item.getDetails();
        if (details == null) {
            return null;
        }

        FinancialMetrics metrics = financialMetricsService.computeMetrics(item);
        if (metrics == null) {
            return null;
        }

        double score = SHARPE_WEIGHT * metrics.getSharpeRatio()
                + ROI_WEIGHT * metrics.getRoi()
                + MOMENTUM_WEIGHT * metrics.getMomentum()
                - DRAWDOWN_WEIGHT * metrics.getMaxDrawdown();

        return new FinancialRankingItem(
                details.getBrand(),
                details.getModel(),
                metrics.getRoiPercent(),
                metrics.getVolatilityPercent(),
                metrics.getSharpeRatio(),
                metrics.getMomentumPercent(),
                metrics.getDrawdownPercent(),
                metrics.getMaxDrawdownPercent(),
                metrics.getMomentumWindow(),
                round(score, 4)
        );
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
