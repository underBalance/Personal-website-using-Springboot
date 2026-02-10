package com.gob.small.marketprice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gob.small.marketprice.data.BrandIngester;
import com.gob.small.marketprice.model.WatchAnalyticsBrands;
import com.gob.small.marketprice.model.WatchAnalyticsSnapshot;
import com.gob.small.marketprice.scraper.WatchAnalyticsScraper;


@RestController
@RequestMapping("/api/v1/watch-analytics")
public class WatchAnalyticsController {
    private final WatchAnalyticsScraper scraper;
    private final BrandIngester brandIngester;

    public WatchAnalyticsController( WatchAnalyticsScraper scraper, BrandIngester brandIngester) {
        this.scraper = scraper;
        this.brandIngester = brandIngester;
    }

    @GetMapping("/{brand}/{model}/{ref}")
    public WatchAnalyticsSnapshot getSnapshot(
            @PathVariable String brand,
            @PathVariable String model,
            @PathVariable String ref
    ) {
        return scraper.fetchSnapshot(brand, model, ref);
    }

    @GetMapping("/brands")
    public WatchAnalyticsBrands getBrands() {
        return new WatchAnalyticsBrands(brandIngester.getBrands());
    }
}
