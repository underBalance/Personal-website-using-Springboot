package com.gob.small.marketprice.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gob.small.marketprice.data.BrandIngester;
import com.gob.small.marketprice.model.WatchAnalyticsBrands;
import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.model.ranking.FinancialRankingItem;
import com.gob.small.marketprice.scraper.WatchAnalyticsScraper;
import com.gob.small.marketprice.scraper.brandesAndModels;
import com.gob.small.marketprice.service.FinancialRankingService;


@RestController
@RequestMapping("/api/v1/watch-analytics")
public class WatchAnalyticsController {
    private final WatchAnalyticsScraper scraper;
    private final BrandIngester brandIngester;
    private final FinancialRankingService financialRankingService;

    public WatchAnalyticsController(
            WatchAnalyticsScraper scraper,
            BrandIngester brandIngester,
            FinancialRankingService financialRankingService
    ) {
        this.scraper = scraper;
        this.brandIngester = brandIngester;
        this.financialRankingService = financialRankingService;
    }

    @GetMapping("/{brand}/{model}/{ref}")
    public WatchAnalyticsSnapshot getSnapshot(
            @PathVariable String brand,
            @PathVariable String model,
            @PathVariable String ref
    ) {
        return scraper.fetchSnapshot(brand, model, ref);
    }

    @PostMapping("/bundle")
    public List<WatchAnalyticsSnapshot> postBundle(@RequestBody List<Map<String, String>> payload) {
        // 10 threads is more than enough
        ExecutorService executor = Executors.newFixedThreadPool(5);
        if(payload.size() < 100 ){
        List<CompletableFuture<WatchAnalyticsSnapshot>> futures =
            payload.stream()
                        .map(item -> CompletableFuture.supplyAsync(() -> {
                            String brand = item.get("brand");
                            String model = item.get("model");
                            String ref = item.get("ref");
                            return scraper.fetchSnapshot(brand, model, ref);
                        }, executor))
                        .toList();

                List<WatchAnalyticsSnapshot> elements =
                    futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                executor.shutdown();

        return elements;
        } else{
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Too many elements in bundle. Maximum allowed is 100."
            );
        }
    }

    @PostMapping("/bundle/ranking")
    public List<FinancialRankingItem> postBundleRanking(@RequestBody List<WatchAnalyticsSnapshot> bundleResult) {
        return financialRankingService.computeRanking(bundleResult);
    }

    @GetMapping("/brands")
    public WatchAnalyticsBrands getBrands() {
        return new WatchAnalyticsBrands(brandIngester.getBrands());
    }

    @GetMapping("/brands/{brand}/models")
    public List<String> getModelsByBrand(@PathVariable String brand) {
        return brandesAndModels.fetchModelsByBrand(brand);
    }

    @GetMapping("/brands/{brand}/models/{model}/references")
    public List<String> getReferencesByBrandAndModel(
            @PathVariable String brand,
            @PathVariable String model
    ) {
        return brandesAndModels.fetchModelReferencesByBrandAndModel(brand, model);
    }
}
