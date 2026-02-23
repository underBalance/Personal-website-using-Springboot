package com.gob.small.marketprice.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class brandesAndModels {
    public static Document fetchWatchAnalyticsBrandDocument() {
        String url = "https://watchanalytics.io/en-eur/brands/";
        return fetchDocument(url);
    }

    // Mantiene la función anterior: modelos por marca
    public static Document fetchWatchAnalyticsModelDocument(String brand) {
        String url = "https://watchanalytics.io/en-eur/brands/" + brand;
        return fetchDocument(url);
    }

    // Nueva sobrecarga: página de un modelo concreto (brand + model)
    public static Document fetchWatchAnalyticsModelDocument(String brand, String model) {
        String url = "https://watchanalytics.io/en-eur/models/" + brand + "-" + model;
        return fetchDocument(url);
    }

    private static Document fetchDocument(String url) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/121.0.0.0 Safari/537.36"
                    )
            );

            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            for (int i = 0; i < 10; i++) {
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(800);
            }

            String html = page.content();
            browser.close();
            return Jsoup.parse(html, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }

    // Mejor que parsear todo el HTML para refs: extrae hrefs directamente desde el DOM con Playwright
    public static List<String> fetchModelReferencesByBrandAndModel(String brand, String model) {
        String modelSlug = (brand + "-" + model).toLowerCase();
        String modelUrl = "https://watchanalytics.io/en-eur/models/" + modelSlug;
        String hrefPrefix = "/en-eur/watches/" + modelSlug + "-";
        Set<String> references = new LinkedHashSet<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/121.0.0.0 Safari/537.36"
                    )
            );

            Page page = context.newPage();
            page.navigate(modelUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            for (int i = 0; i < 10; i++) {
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(500);
            }

            @SuppressWarnings("unchecked")
            List<String> hrefs = (List<String>) page.evalOnSelectorAll(
                    "a[href]",
                    "els => els.map(e => e.getAttribute('href')).filter(Boolean)"
            );

            for (String href : hrefs) {
                String normalizedHref = href.trim()
                        .replace("https://watchanalytics.io", "")
                        .replace("http://watchanalytics.io", "");

                int queryStart = normalizedHref.indexOf("?");
                if (queryStart >= 0) {
                    normalizedHref = normalizedHref.substring(0, queryStart);
                }

                int hashStart = normalizedHref.indexOf("#");
                if (hashStart >= 0) {
                    normalizedHref = normalizedHref.substring(0, hashStart);
                }

                if (!normalizedHref.startsWith(hrefPrefix)) {
                    continue;
                }

                String reference = normalizedHref.substring(hrefPrefix.length()).trim();
                if (!reference.isEmpty()) {
                    references.add(reference);
                }
            }

            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch references from " + modelUrl, e);
        }

        return new ArrayList<>(references);
    }

    // Devuelve modelos a partir de una marca (modelo sin prefijo de marca).
    public static List<String> fetchModelsByBrand(String brand) {
        String normalizedBrand = brand.toLowerCase().trim();
        String brandUrl = "https://watchanalytics.io/en-eur/brands/" + normalizedBrand;
        String hrefPrefix = "/en-eur/models/" + normalizedBrand + "-";
        Set<String> models = new LinkedHashSet<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/121.0.0.0 Safari/537.36"
                    )
            );

            Page page = context.newPage();
            page.navigate(brandUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            for (int i = 0; i < 10; i++) {
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(500);
            }

            @SuppressWarnings("unchecked")
            List<String> hrefs = (List<String>) page.evalOnSelectorAll(
                    "a[href]",
                    "els => els.map(e => e.getAttribute('href')).filter(Boolean)"
            );

            for (String href : hrefs) {
                String normalizedHref = href.trim()
                        .replace("https://watchanalytics.io", "")
                        .replace("http://watchanalytics.io", "");

                int queryStart = normalizedHref.indexOf("?");
                if (queryStart >= 0) {
                    normalizedHref = normalizedHref.substring(0, queryStart);
                }

                int hashStart = normalizedHref.indexOf("#");
                if (hashStart >= 0) {
                    normalizedHref = normalizedHref.substring(0, hashStart);
                }

                if (!normalizedHref.startsWith(hrefPrefix)) {
                    continue;
                }

                String model = normalizedHref.substring(hrefPrefix.length()).trim();
                if (!model.isEmpty()) {
                    models.add(model);
                }
            }

            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch models from " + brandUrl, e);
        }

        // Fallback por si la página cambia el HTML visible pero mantiene el payload.
        if (models.isEmpty()) {
            for (String fullSlug : fetchModelSlugsFromBrandPayload(normalizedBrand)) {
                String prefix = normalizedBrand + "-";
                if (fullSlug.startsWith(prefix) && fullSlug.length() > prefix.length()) {
                    models.add(fullSlug.substring(prefix.length()));
                }
            }
        }

        return new ArrayList<>(models);
    }

    // Extrae slugs desde el payload incrustado (ej: ["slug","rolex-milgauss","d"]).
    public static List<String> fetchModelSlugsFromBrandPayload(String brand) {
        Document doc = fetchWatchAnalyticsModelDocument(brand);
        String html = doc.html();

        Pattern slugPattern = Pattern.compile("\\[\\\"slug\\\",\\\"([^\\\"]+)\\\",\\\"d\\\"\\]");
        Matcher matcher = slugPattern.matcher(html);

        Set<String> modelSlugs = new LinkedHashSet<>();
        while (matcher.find()) {
            modelSlugs.add(matcher.group(1));
        }

        return new ArrayList<>(modelSlugs);
    }

    public static void main(String[] args) {
        List<String> models = fetchModelsByBrand("rolex");
        for (String model : models) {
            System.out.println("Model -> " + model);
        }

        List<String> references = fetchModelReferencesByBrandAndModel("rolex", "oyster-perpetual");
        for (String ref : references) {
            System.out.println("Reference -> " + ref);
        }

        List<String> slugs = fetchModelSlugsFromBrandPayload("rolex");
        for (String slug : slugs) {
            System.out.println("Payload model slug -> " + slug);
        }
    }
}
