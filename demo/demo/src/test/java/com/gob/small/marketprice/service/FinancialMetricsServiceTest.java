package com.gob.small.marketprice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.model.ranking.FinancialMetrics;

class FinancialMetricsServiceTest {

    private final FinancialMetricsService financialMetricsService = new FinancialMetricsService();

    @Test
    void computesMetricsFromPriceHistory() {
        WatchAnalyticsSnapshot snapshot = snapshot(
                "Rolex",
                "Submariner",
                new BigDecimal("100"),
                new BigDecimal("118"),
                List.of(
                        pricePoint("2024-01-01", "100"),
                        pricePoint("2024-02-01", "110"),
                        pricePoint("2024-03-01", "121"),
                        pricePoint("2024-04-01", "118")
                )
        );

        FinancialMetrics metrics = financialMetricsService.computeMetrics(snapshot);

        assertEquals(18.0, metrics.getRoiPercent());
        assertEquals("3M", metrics.getMomentumWindow());
        assertEquals(18.0, metrics.getMomentumPercent());
        assertEquals(2.48, metrics.getDrawdownPercent());
        assertEquals(2.48, metrics.getMaxDrawdownPercent());
        assertTrue(metrics.getVolatility() > 0);
    }

    private WatchAnalyticsSnapshot snapshot(
            String brand,
            String model,
            BigDecimal retailPrice,
            BigDecimal closingPrice,
            List<WatchAnalyticsSnapshot.PricePoint> priceHistory
    ) {
        return new WatchAnalyticsSnapshot(
                "description",
                "title",
                new WatchAnalyticsSnapshot.Details(
                        brand,
                        model,
                        retailPrice,
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new WatchAnalyticsSnapshot.Summary(
                        closingPrice,
                        closingPrice,
                        closingPrice,
                        closingPrice,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                priceHistory
        );
    }

    private WatchAnalyticsSnapshot.PricePoint pricePoint(String time, String value) {
        return new WatchAnalyticsSnapshot.PricePoint(time, new BigDecimal(value));
    }
}
