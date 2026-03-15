package com.gob.small.marketprice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.model.ranking.FinancialMetrics;

@Service
public class FinancialMetricsService {
    private static final double EPS = 1e-9;
    private static final double ANNUAL_RISK_FREE_RATE = 0.02;
    private static final double DEFAULT_PERIODS_PER_YEAR = 12.0;

    public FinancialMetrics computeMetrics(WatchAnalyticsSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        List<HistoricalPoint> points = normalizePriceHistory(snapshot.getPriceHistory());
        BigDecimal retail = snapshot.getDetails() == null ? null : snapshot.getDetails().getRetailPrice();
        BigDecimal closing = snapshot.getSummary() == null ? null : snapshot.getSummary().getClosingPrice();

        Double latestPrice = positiveValue(closing);
        if (latestPrice == null && !points.isEmpty()) {
            latestPrice = points.get(points.size() - 1).value();
        }

        if (latestPrice == null) {
            return null;
        }

        double roi = computeRoi(retail, points, latestPrice);
        List<Double> returns = computeReturns(points);
        double volatility = standardDeviation(returns);
        double sharpeRatio = computeSharpeRatio(returns, volatility, points);
        MomentumResult momentum = computeMomentum(points);
        double drawdown = computeCurrentDrawdown(points, latestPrice);
        double maxDrawdown = computeMaxDrawdown(points);

        return new FinancialMetrics(
                round(roi, 6),
                round(roi * 100.0, 2),
                round(volatility, 6),
                round(volatility * 100.0, 2),
                round(sharpeRatio, 4),
                round(momentum.value(), 6),
                round(momentum.value() * 100.0, 2),
                momentum.windowLabel(),
                round(drawdown, 6),
                round(drawdown * 100.0, 2),
                round(maxDrawdown, 6),
                round(maxDrawdown * 100.0, 2)
        );
    }

    private double computeRoi(BigDecimal retail, List<HistoricalPoint> points, double latestPrice) {
        Double retailValue = positiveValue(retail);
        if (retailValue != null) {
            return (latestPrice - retailValue) / retailValue;
        }

        if (points.isEmpty()) {
            return 0;
        }

        double firstPrice = points.get(0).value();
        return (latestPrice - firstPrice) / firstPrice;
    }

    private List<Double> computeReturns(List<HistoricalPoint> points) {
        List<Double> returns = new ArrayList<>();
        for (int index = 1; index < points.size(); index++) {
            double previous = points.get(index - 1).value();
            if (previous <= 0) {
                continue;
            }

            double current = points.get(index).value();
            returns.add((current - previous) / previous);
        }
        return returns;
    }

    private double computeSharpeRatio(List<Double> returns, double volatility, List<HistoricalPoint> points) {
        if (returns.isEmpty() || volatility <= EPS) {
            return 0;
        }

        double averageReturn = average(returns);
        double periodsPerYear = estimatePeriodsPerYear(points);
        double periodRiskFreeRate = Math.pow(1 + ANNUAL_RISK_FREE_RATE, 1.0 / periodsPerYear) - 1.0;
        return (averageReturn - periodRiskFreeRate) / volatility;
    }

    private MomentumResult computeMomentum(List<HistoricalPoint> points) {
        if (points.size() < 2) {
            return new MomentumResult(0, "N/A");
        }

        HistoricalPoint latest = points.get(points.size() - 1);
        List<MomentumWindow> windows = List.of(
                new MomentumWindow("12M", latest.date().minusYears(1)),
                new MomentumWindow("6M", latest.date().minusMonths(6)),
                new MomentumWindow("3M", latest.date().minusMonths(3))
        );

        for (MomentumWindow window : windows) {
            HistoricalPoint start = findPointForPeriod(points, window.startDate());
            if (start != null && start.value() > 0) {
                return new MomentumResult((latest.value() - start.value()) / start.value(), window.label());
            }
        }

        HistoricalPoint first = points.get(0);
        return new MomentumResult((latest.value() - first.value()) / first.value(), "ALL");
    }

    private double computeCurrentDrawdown(List<HistoricalPoint> points, double latestPrice) {
        if (points.isEmpty()) {
            return 0;
        }

        double ath = points.stream()
                .mapToDouble(HistoricalPoint::value)
                .max()
                .orElse(latestPrice);

        if (ath <= 0) {
            return 0;
        }

        return Math.max(0, (ath - latestPrice) / ath);
    }

    private double computeMaxDrawdown(List<HistoricalPoint> points) {
        if (points.isEmpty()) {
            return 0;
        }

        double runningPeak = points.get(0).value();
        double maxDrawdown = 0;

        for (HistoricalPoint point : points) {
            runningPeak = Math.max(runningPeak, point.value());
            if (runningPeak <= 0) {
                continue;
            }

            double currentDrawdown = (runningPeak - point.value()) / runningPeak;
            maxDrawdown = Math.max(maxDrawdown, currentDrawdown);
        }

        return maxDrawdown;
    }

    private List<HistoricalPoint> normalizePriceHistory(List<WatchAnalyticsSnapshot.PricePoint> priceHistory) {
        if (priceHistory == null || priceHistory.isEmpty()) {
            return List.of();
        }

        return priceHistory.stream()
                .filter(point -> point != null && point.getTime() != null && positiveValue(point.getValue()) != null)
                .map(point -> new HistoricalPoint(LocalDate.parse(point.getTime()), point.getValue().doubleValue()))
                .sorted(Comparator.comparing(HistoricalPoint::date))
                .toList();
    }

    private HistoricalPoint findPointForPeriod(List<HistoricalPoint> points, LocalDate targetDate) {
        HistoricalPoint candidate = null;
        for (HistoricalPoint point : points) {
            if (!point.date().isAfter(targetDate)) {
                candidate = point;
            } else {
                break;
            }
        }
        return candidate;
    }

    private double estimatePeriodsPerYear(List<HistoricalPoint> points) {
        if (points.size() < 2) {
            return DEFAULT_PERIODS_PER_YEAR;
        }

        long totalDays = 0;
        int gaps = 0;
        for (int index = 1; index < points.size(); index++) {
            long days = points.get(index).date().toEpochDay() - points.get(index - 1).date().toEpochDay();
            if (days > 0) {
                totalDays += days;
                gaps++;
            }
        }

        if (gaps == 0) {
            return DEFAULT_PERIODS_PER_YEAR;
        }

        double averageDays = (double) totalDays / gaps;
        if (averageDays <= 0) {
            return DEFAULT_PERIODS_PER_YEAR;
        }

        return Math.max(1.0, 365.25 / averageDays);
    }

    private double average(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }

        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    private double standardDeviation(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }

        double mean = average(values);
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private Double positiveValue(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) >= 0) {
            return null;
        }
        return value.doubleValue();
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private record HistoricalPoint(LocalDate date, double value) {}

    private record MomentumWindow(String label, LocalDate startDate) {}

    private record MomentumResult(double value, String windowLabel) {}
}
