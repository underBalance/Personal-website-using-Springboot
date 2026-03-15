package com.gob.small.marketprice.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
    private static final String CAPTURED_SERIES_SCRIPT_ID = "wa-captured-series-json";

        public Document fetchWatchAnalyticsDocument(String brand, String model, String ref) {

        String url = "https://watchanalytics.io/en-eur/watches/" + brand + "-" + model + "-" + ref;

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
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
            page.addInitScript("""
            () => {

                window.__CAPTURED_SERIES__ = [];

                const tryHook = () => {
                    if (!window.LightweightCharts) return false;

                    const proto = window.LightweightCharts.Series?.prototype;
                    if (!proto || proto.__patched) return false;

                    const original = proto.setData;

                    proto.setData = function(data){
                        try{
                            window.__CAPTURED_SERIES__.push(data);
                            console.log("CAPTURED_SERIES", data);
                        }catch(e){}
                        return original.apply(this, arguments);
                    };

                    proto.__patched = true;
                    return true;
                };

                const interval = setInterval(() => {
                    if (tryHook()) clearInterval(interval);
                }, 50);

            }
            """);

            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60000));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(3000);

            String capturedSeriesJson = capturePriceSeriesJson(page);
            log.debug("Captured raw price series JSON length={}", capturedSeriesJson == null ? 0 : capturedSeriesJson.length());
            log.debug("Captured raw price series JSON preview={}", abbreviate(capturedSeriesJson, 500));

            String html = page.content();
            browser.close();

            Document doc = Jsoup.parse(html, url);
            doc.body()
                    .appendElement("script")
                    .attr("id", CAPTURED_SERIES_SCRIPT_ID)
                    .attr("type", "application/json")
                    .text(capturedSeriesJson);
            log.debug("Embedded captured series script present={} chars={}",
                    doc.selectFirst("script#" + CAPTURED_SERIES_SCRIPT_ID) != null,
                    doc.selectFirst("script#" + CAPTURED_SERIES_SCRIPT_ID) == null
                            ? 0
                            : doc.selectFirst("script#" + CAPTURED_SERIES_SCRIPT_ID).data().length());

            List<WatchPricePoint> debugPoints = extractCapturedPriceSeries(doc);
            log.debug("Extracted {} historical price points from embedded script", debugPoints.size());
            if (!debugPoints.isEmpty()) {
                log.debug("First historical point={} last historical point={}",
                        debugPoints.get(0),
                        debugPoints.get(debugPoints.size() - 1));
            }

            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }

    private String capturePriceSeriesJson(Page page) {
        String capturedSeriesJson = (String) page.evaluate("""
                () => {
                    const series = window.__CAPTURED_SERIES__ || [];
                    return JSON.stringify(series);
                }
                """);

        if (hasPriceSeries(capturedSeriesJson)) {
            return capturedSeriesJson;
        }

        String fallbackSeriesJson = (String) page.evaluate("""
                () => {
                    const isPoint = (value) =>
                        value &&
                        typeof value === 'object' &&
                        !Array.isArray(value) &&
                        Object.prototype.hasOwnProperty.call(value, 'time') &&
                        Object.prototype.hasOwnProperty.call(value, 'value');

                    const looksLikeSeries = (value) =>
                        Array.isArray(value) &&
                        value.length > 1 &&
                        value.every(isPoint);

                    const seen = new WeakSet();
                    let best = [];

                    const visit = (value, depth) => {
                        if (!value || depth > 4) return;

                        if (looksLikeSeries(value) && value.length > best.length) {
                            best = value;
                        }

                        if (typeof value !== 'object') return;
                        if (seen.has(value)) return;
                        seen.add(value);

                        if (Array.isArray(value)) {
                            for (const item of value) {
                                visit(item, depth + 1);
                            }
                            return;
                        }

                        for (const key of Object.keys(value)) {
                            if (key === 'window' || key === 'document') continue;

                            let next;
                            try {
                                next = value[key];
                            } catch (e) {
                                continue;
                            }
                            visit(next, depth + 1);
                        }
                    };

                    for (const key of Object.getOwnPropertyNames(window)) {
                        let candidate;
                        try {
                            candidate = window[key];
                        } catch (e) {
                            continue;
                        }
                        visit(candidate, 0);
                    }

                    return JSON.stringify(best);
                }
                """);

        if (hasPriceSeries(fallbackSeriesJson)) {
            return fallbackSeriesJson;
        }

        String memorySeriesJson = (String) page.evaluate("""
                () => {
                    const seen = new WeakSet();
                    let best = [];

                    const looksLikeDatePriceSeries = (value) =>
                        Array.isArray(value) &&
                        value.length > 1 &&
                        value.every(item =>
                            item &&
                            typeof item === 'object' &&
                            !Array.isArray(item) &&
                            Object.prototype.hasOwnProperty.call(item, 'date') &&
                            Object.prototype.hasOwnProperty.call(item, 'price')
                        );

                    const normalize = (series) =>
                        series
                            .map(item => {
                                try {
                                    return {
                                        time: new Date(item.date).toISOString().split("T")[0],
                                        value: item.price
                                    };
                                } catch (e) {
                                    return null;
                                }
                            })
                            .filter(Boolean)
                            .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());

                    const visit = (value, depth) => {
                        if (!value || depth > 6) return;
                        if (typeof value !== 'object') return;
                        if (seen.has(value)) return;
                        seen.add(value);

                        if (looksLikeDatePriceSeries(value) && value.length > best.length) {
                            best = normalize(value);
                        }

                        if (Array.isArray(value)) {
                            for (const item of value) {
                                visit(item, depth + 1);
                            }
                            return;
                        }

                        for (const key of Object.keys(value)) {
                            if (key === 'window' || key === 'document') continue;

                            let next;
                            try {
                                next = value[key];
                            } catch (e) {
                                continue;
                            }
                            visit(next, depth + 1);
                        }
                    };

                    for (const key of Object.getOwnPropertyNames(window)) {
                        let candidate;
                        try {
                            candidate = window[key];
                        } catch (e) {
                            continue;
                        }
                        visit(candidate, 0);
                    }

                    return JSON.stringify(best);
                }
                """);

        if (hasPriceSeries(memorySeriesJson)) {
            log.debug("Captured price series from in-memory date/price array");
            return memorySeriesJson;
        }

        String reactSeriesJson = (String) page.evaluate("""
                () => {
                    const seen = new WeakSet();
                    let best = [];

                    const looksLikeDatePriceSeries = (value) =>
                        Array.isArray(value) &&
                        value.length > 1 &&
                        value.every(item =>
                            item &&
                            typeof item === 'object' &&
                            !Array.isArray(item) &&
                            Object.prototype.hasOwnProperty.call(item, 'date') &&
                            Object.prototype.hasOwnProperty.call(item, 'price')
                        );

                    const normalize = (series) =>
                        series
                            .map(item => {
                                try {
                                    return {
                                        time: new Date(item.date).toISOString().split("T")[0],
                                        value: item.price
                                    };
                                } catch (e) {
                                    return null;
                                }
                            })
                            .filter(Boolean)
                            .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());

                    const visit = (value, depth) => {
                        if (!value || depth > 8) return;
                        if (typeof value !== 'object') return;
                        if (seen.has(value)) return;
                        seen.add(value);

                        if (looksLikeDatePriceSeries(value) && value.length > best.length) {
                            best = normalize(value);
                        }

                        if (Array.isArray(value)) {
                            for (const item of value) {
                                visit(item, depth + 1);
                            }
                            return;
                        }

                        for (const key of Object.keys(value)) {
                            let next;
                            try {
                                next = value[key];
                            } catch (e) {
                                continue;
                            }
                            visit(next, depth + 1);
                        }
                    };

                    const isGraphNode = (node) => {
                        if (!(node instanceof HTMLElement)) return false;
                        const text = (node.textContent || "").toLowerCase();
                        const cls = (node.className || "").toString().toLowerCase();
                        return text.includes("closing price")
                            || text.includes("opening price")
                            || cls.includes("chart")
                            || cls.includes("graph");
                    };

                    const candidateNodes = Array.from(document.querySelectorAll("div, section, article"))
                        .filter(isGraphNode)
                        .slice(0, 20);

                    for (const node of candidateNodes) {
                        for (const key of Object.getOwnPropertyNames(node)) {
                            if (!key.startsWith("__reactFiber$") && !key.startsWith("__reactProps$")) {
                                continue;
                            }

                            let reactValue;
                            try {
                                reactValue = node[key];
                            } catch (e) {
                                continue;
                            }

                            visit(reactValue, 0);

                            if (reactValue && typeof reactValue === 'object') {
                                visit(reactValue.memoizedProps, 0);
                                visit(reactValue.memoizedState, 0);
                                visit(reactValue.pendingProps, 0);
                                visit(reactValue.stateNode, 0);
                                visit(reactValue.return, 1);
                                visit(reactValue.child, 1);
                            }
                        }
                    }

                    return JSON.stringify(best);
                }
                """);

        if (hasPriceSeries(reactSeriesJson)) {
            log.debug("Captured price series from React fiber/props state");
            return reactSeriesJson;
        }

        return "[]";
    }

    private boolean hasPriceSeries(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return false;
        }

        try {
            JSONArray root = new JSONArray(json);
            return pickBestPriceSeries(root) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static Document brandModels(String brand){
        String url = "https://watchanalytics.io/en-eur/watches/brands/" + brand;

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

            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60000));

            // Function that scans the hole page
            humanScroll(page);
            // HTML COMPLETO ya renderizado (con JS)
            String html = page.content();

            browser.close();

            // 👉 Si quieres tratarlo con Jsoup después
            return Jsoup.parse(html, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }

        private static void humanScroll(Page page) {
        int maxSteps = 15;

        for (int i = 0; i < maxSteps; i++) {

            System.out.println("Human scroll step " + i);

            // scroll pequeño
            page.evaluate("window.scrollBy(0, 400)");

            // mover el ratón (MUY IMPORTANTE)
            page.mouse().move(300, 500);

            // esperar a que cargue contenido
            page.waitForTimeout(2000);

            // debug: cuántos links hay
            int linkCount = page.locator("a[href]").count();
            System.out.println("Links found: " + linkCount);
        }
    }
    @SuppressWarnings("unused")
    private static void scrollToBottom(Page page) {
        int previousHeight = 0;
        int maxScrolls = 5, i=0;

        while ( i < maxScrolls) {
            int currentHeight = ((Number) page.evaluate("document.body.scrollHeight")).intValue();
            if (currentHeight == previousHeight) {
                break; // ya no se carga nada nuevo
            }

            previousHeight = currentHeight;

            // scroll hasta abajo
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            // espera a que cargue contenido nuevo
            page.waitForTimeout(1200);

            System.out.println(page.content());
            // Next page
            i++;
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

            if (startsWithSection(text, "Details")) {
                detailsMap.putAll(parseSectionByLabels(text, "Details", DETAILS_LABELS));
                log.debug("detailsMap updated={}", detailsMap);
            }

            if (startsWithSection(text, "Summary")) {
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
        List<WatchAnalyticsSnapshot.PricePoint> priceHistory = extractCapturedPriceSeries(doc).stream()
                .map(point -> new WatchAnalyticsSnapshot.PricePoint(point.time(), point.value()))
                .toList();

        log.debug("detailsMap={}", detailsMap);
        log.debug("summaryMap={}", summaryMap);
        log.debug("snapshot details={} summary={}", details, summary);

        return new WatchAnalyticsSnapshot(
                description,
                twitterTitle,
                details,
                summary,
                priceHistory
        );

    }

    public List<WatchPricePoint> extractCapturedPriceSeries(Document doc) {
        Element dataScript = doc.selectFirst("script#" + CAPTURED_SERIES_SCRIPT_ID);
        if (dataScript == null) {
            return List.of();
        }

        String json = dataScript.data();
        if (json == null || json.isBlank()) {
            json = dataScript.html();
        }
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JSONArray root = new JSONArray(json);
            JSONArray bestSeries = pickBestPriceSeries(root);
            if (bestSeries == null) {
                return List.of();
            }

            List<WatchPricePoint> rows = new ArrayList<>();
            for (int i = 0; i < bestSeries.length(); i++) {
                Object entry = bestSeries.get(i);
                if (!(entry instanceof JSONObject point)) {
                    continue;
                }
                if (!point.has("time") || !point.has("value")) {
                    continue;
                }

                String time = point.optString("time", null);
                if (time == null || time.isBlank()) {
                    continue;
                }

                BigDecimal value;
                try {
                    value = new BigDecimal(String.valueOf(point.get("value")));
                } catch (Exception ignored) {
                    continue;
                }

                rows.add(new WatchPricePoint(time, value));
            }

            rows.sort(Comparator.comparing(WatchPricePoint::time));
            return rows;
        } catch (Exception e) {
            log.warn("Could not parse captured watch series JSON", e);
            return List.of();
        }
    }

    private JSONArray pickBestPriceSeries(JSONArray root) {
        if (root == null) {
            return null;
        }

        if (looksLikePriceSeries(root)) {
            return root;
        }

        JSONArray best = null;
        for (int i = 0; i < root.length(); i++) {
            Object candidate = root.get(i);
            if (!(candidate instanceof JSONArray series)) {
                continue;
            }
            if (!looksLikePriceSeries(series)) {
                continue;
            }
            if (best == null || series.length() > best.length()) {
                best = series;
            }
        }
        return best;
    }

    private boolean looksLikePriceSeries(JSONArray node) {
        if (node == null || node.isEmpty()) {
            return false;
        }

        Object firstObj = node.get(0);
        if (!(firstObj instanceof JSONObject first)) {
            return false;
        }

        return first.has("time") && first.has("value");
    }

    public record WatchPricePoint(String time, BigDecimal value) {}

    private String abbreviate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

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
        String body = stripSectionPrefix(text, sectionPrefix);
        if (body == null || body.isBlank()) {
            return result;
        }

        List<LabelIndex> indices = new ArrayList<>();
        for (String label : labels) {
            Matcher matcher = Pattern.compile("(?i)" + Pattern.quote(label) + "\\s*:?[\\s\\u00A0]*").matcher(body);
            if (matcher.find()) {
                indices.add(new LabelIndex(label, matcher.start(), matcher.end()));
            }
        }
        indices.sort(Comparator.comparingInt(li -> li.index));

        for (int i = 0; i < indices.size(); i++) {
            LabelIndex current = indices.get(i);
            int valueStart = current.valueStart;
            int valueEnd = (i + 1 < indices.size()) ? indices.get(i + 1).index : body.length();
            String value = body.substring(valueStart, valueEnd).trim();
            if (!value.isEmpty()) {
                result.put(current.label, value);
            }
        }
        return result;
    }

    private boolean startsWithSection(String text, String sectionPrefix) {
        String normalized = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        String section = sectionPrefix.toLowerCase(Locale.ROOT);
        return normalized.startsWith(section + " ") || normalized.startsWith(section + ":") || normalized.equals(section);
    }

    private String stripSectionPrefix(String text, String sectionPrefix) {
        if (text == null) {
            return null;
        }
        String pattern = "(?i)^\\s*" + Pattern.quote(sectionPrefix) + "\\s*:?[\\s\\u00A0]*";
        return text.replaceFirst(pattern, "").trim();
    }

    private static class LabelIndex {
        private final String label;
        private final int index;
        private final int valueStart;

        private LabelIndex(String label, int index, int valueStart) {
            this.label = label;
            this.index = index;
            this.valueStart = valueStart;
        }
    }

    public static void main(String[] args) {
       Document elem = brandModels("rolex");
       for (Element a : elem.select("a[href]")) {
            String href = a.attr("href");
            System.out.println(href);
        }
    
    }

}
