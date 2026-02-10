package com.gob.small.marketprice.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gob.small.marketprice.model.*;


@Service
public class WatchAnalyticsScraper {
    private static final Logger log = LoggerFactory.getLogger(WatchAnalyticsScraper.class);
    private static final String[] DETAILS_LABELS = new String[] {
            "Brand",
            "Model",
            "Retail price",
            "Caliber",
            "Complications",
            "Crystal",
            "Dial",
            "Diameter",
            "Material",
            "Movement",
            "Power reserve",
            "Water resistance"
    };
    private static final String[] SUMMARY_LABELS = new String[] {
            "Closing price",
            "Opening price",
            "Minimum price",
            "Maximum price",
            "Relative performance",
            "Absolute performance"
    };

    public Document fetchWatchAnalyticsDocument(String brand, String model, String ref) {

        String url = "https://watchanalytics.io/en-eur/watches/" + brand+ "-" + model+ "-"+ ref;

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true) // pon false si quieres VER el navegador
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/121.0.0.0 Safari/537.36"
                            )
            );

            Page page = context.newPage();

            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            // HTML COMPLETO ya renderizado (con JS)
            String html = page.content();

            browser.close();

            // 👉 Si quieres tratarlo con Jsoup después
            return Jsoup.parse(html, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }

    public WatchAnalyticsSnapshot fetchSnapshot(String brand, String model, String ref) {
        // Retrieve data
        Document doc = fetchWatchAnalyticsDocument(brand, model, ref); 
        log.debug("Fetched document. title='{}' url='{}'", doc.title(), doc.location());
        String description = doc.selectFirst("meta[name=description]") != null
                ? doc.selectFirst("meta[name=description]").attr("content")
                : null;
        

        String twitterTitle = doc.selectFirst("meta[name=twitter:title]") != null
                ? doc.selectFirst("meta[name=twitter:title]").attr("content")
                : null;

        Map<String, String> detailsMap = new HashMap<>();
        Map<String, String> summaryMap = new HashMap<>();

        for (Element element : doc.select(".mt-2")) {
            String text = element.text().trim();
            log.debug("element text='{}'", text);

            if (text.startsWith("Details ")) {
                detailsMap.putAll(parseSectionByLabels(text, "Details", DETAILS_LABELS));
                log.debug("detailsMap updated={}", detailsMap);
            }

            if (text.startsWith("Summary ")) {
                summaryMap.putAll(parseSectionByLabels(text, "Summary", SUMMARY_LABELS));
                log.debug("summaryMap updated={}", summaryMap);
            }
        }

        WatchAnalyticsSnapshot.Details details =
            new WatchAnalyticsSnapshot.Details(
                detailsMap.get("Brand"),
                detailsMap.get("Model"),
                parseRetailPrice(detailsMap.get("Retail price")),
                parseRetailChange(detailsMap.get("Retail price")), 
                detailsMap.get("Caliber"),
                detailsMap.get("Complications"),
                detailsMap.get("Crystal"),
                detailsMap.get("Dial"),
                detailsMap.get("Diameter"),
                detailsMap.get("Material"),
                detailsMap.get("Movement"),
                detailsMap.get("Power reserve"),
                detailsMap.get("Water resistance")
            );
        
        WatchAnalyticsSnapshot.Summary summary =
        new WatchAnalyticsSnapshot.Summary(
            parseMoney(summaryMap.get("Closing price")),
            parseMoney(summaryMap.get("Opening price")),
            parseMoney(summaryMap.get("Minimum price")),
            parseMoney(summaryMap.get("Maximum price")),
            parsePercent(summaryMap.get("Relative performance")),
            parseMoney(summaryMap.get("Absolute performance"))
        );

        log.debug("detailsMap={}", detailsMap);
        log.debug("summaryMap={}", summaryMap);
        log.debug("snapshot details={} summary={}", details, summary);

        return new WatchAnalyticsSnapshot(
                description,
                twitterTitle,
                details,
                summary
        );

    }

    // public WatchAnalyticsBrands fetchBrands() {
       

    //     return new WatchAnalyticsBrands(new ArrayList<>(bySlug.values()));
    // }

    private BigDecimal parseMoney(String text) {
        if (text == null) return null;
        String cleaned = text
                .replace("€", "")
                .replace("$", "")
                .replace(",", "")
                .replace("+", "")
                .trim();
        return new BigDecimal(cleaned.split(" ")[0]);
    }


    private BigDecimal parsePercent(String text) {
        if (text == null) return null;
        return new BigDecimal(
            text.replace("%", "").replace("+", "").trim()
        );
    }

    private BigDecimal parseRetailPrice(String text) {
        if (text == null || !text.contains("€")) return null;
        String money = text.substring(text.indexOf("€") + 1, text.indexOf("(")).trim();
        return parseMoney(money);
    }

    private BigDecimal parseRetailChange(String text) {
        if (text == null || !text.contains("(")) return null;
        String percent = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
        return parsePercent(percent);
    }

    private Map<String, String> parseSectionByLabels(String text, String sectionPrefix, String[] labels) {
        Map<String, String> result = new HashMap<>();
        String prefix = sectionPrefix + " ";
        if (!text.startsWith(prefix)) {
            return result;
        }
        String body = text.substring(prefix.length()).trim();
        if (body.isEmpty()) {
            return result;
        }

        List<LabelIndex> indices = new ArrayList<>();
        for (String label : labels) {
            int idx = body.indexOf(label + " ");
            if (idx >= 0) {
                indices.add(new LabelIndex(label, idx));
            }
        }
        indices.sort(Comparator.comparingInt(li -> li.index));

        for (int i = 0; i < indices.size(); i++) {
            LabelIndex current = indices.get(i);
            int valueStart = current.index + current.label.length() + 1;
            int valueEnd = (i + 1 < indices.size()) ? indices.get(i + 1).index : body.length();
            String value = body.substring(valueStart, valueEnd).trim();
            if (!value.isEmpty()) {
                result.put(current.label, value);
            }
        }
        return result;
    }

    private static class LabelIndex {
        private final String label;
        private final int index;

        private LabelIndex(String label, int index) {
            this.label = label;
            this.index = index;
        }
    }

}
