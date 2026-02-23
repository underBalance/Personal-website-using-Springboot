package com.gob.small.marketprice.data;

// Temporary solution to avoid scraping WatchAnalytics each time
import com.gob.small.marketprice.model.WatchAnalyticsBrands.Brand;
import java.util.List;

import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class BrandIngester {

    private final List<Brand> brands = List.of(
        new Brand("Rolex", "rolex"),
        new Brand("Audemars Piguet", "audemars-piguet"),
        new Brand("Patek Philippe", "patek-philippe"),
        new Brand("Omega", "omega"),
        new Brand("Cartier", "cartier"),
        new Brand("Tudor", "tudor"),
        new Brand("Vacheron Constantin", "vacheron-constantin"),
        new Brand("Hublot", "hublot"),
        new Brand("Panerai", "panerai"),
        new Brand("A. Lange & Söhne", "a-lange-sohne"),
        new Brand("IWC", "iwc"),
        new Brand("Zenith", "zenith"),
        new Brand("Richard Mille", "richard-mille"),
        new Brand("Breitling", "breitling"),
        new Brand("Jaeger-LeCoultre", "jaeger-lecoultre"),
        new Brand("Bulgari", "bulgari"),
        new Brand("Grand Seiko", "grand-seiko"),
        new Brand("TAG Heuer", "tag-heuer"),
        new Brand("Breguet", "breguet"),
        new Brand("Bell & Ross", "bell-ross"),
        new Brand("Girard-Perregaux", "girard-perregaux"),
        new Brand("Longines", "longines"),
        new Brand("Ulysse Nardin", "ulysse-nardin"),
        new Brand("Glashütte Original", "glashutte-original")
    );

    public List<Brand> getBrands() {
        return Collections.unmodifiableList(brands);
    }
}
