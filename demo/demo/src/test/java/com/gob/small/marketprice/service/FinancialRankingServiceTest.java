package com.gob.small.marketprice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.model.ranking.FinancialRankingItem;

class FinancialRankingServiceTest {

    private final FinancialRankingService financialRankingService =
            new FinancialRankingService(new FinancialMetricsService());

    @Test
    void sortsByComputedScoreDescending() {
        WatchAnalyticsSnapshot stronger = snapshot(
                "Omega",
                "Speedmaster",
                "100",
                "140",
                List.of(
                        pricePoint("2024-01-01", "100"),
                        pricePoint("2024-02-01", "115"),
                        pricePoint("2024-03-01", "130"),
                        pricePoint("2024-04-01", "140")
                )
        );

        WatchAnalyticsSnapshot weaker = snapshot(
                "Tag Heuer",
                "Carrera",
                "100",
                "102",
                List.of(
                        pricePoint("2024-01-01", "100"),
                        pricePoint("2024-02-01", "110"),
                        pricePoint("2024-03-01", "90"),
                        pricePoint("2024-04-01", "102")
                )
        );

        List<FinancialRankingItem> ranking = financialRankingService.computeRanking(List.of(weaker, stronger));

        assertEquals(2, ranking.size());
        assertEquals("Omega", ranking.get(0).getBrand());
        assertEquals("Tag Heuer", ranking.get(1).getBrand());
        assertEquals("3M", ranking.get(0).getMomentumWindow());
    }

    private WatchAnalyticsSnapshot snapshot(
            String brand,
            String model,
            String retailPrice,
            String closingPrice,
            List<WatchAnalyticsSnapshot.PricePoint> priceHistory
    ) {
        BigDecimal closing = new BigDecimal(closingPrice);
        return new WatchAnalyticsSnapshot(
                "description",
                "title",
                new WatchAnalyticsSnapshot.Details(
                        brand,
                        model,
                        new BigDecimal(retailPrice),
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
                        closing,
                        closing,
                        closing,
                        closing,
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
