package com.gob.small.marketprice.scraper;

import org.jsoup.Jsoup;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class brandesAndModels {
     public static Document fetchWatchAnalyticsBrandDocument() {

        String url = "https://watchanalytics.io/en-eur/brands/";

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

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));


            for (int i = 0; i < 10; i++) { // número conservador
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(800); // tiempo para que React renderice
            }

            String html = page.content();

            browser.close();

            return Jsoup.parse(html, url);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }
    

    public static Document fetchWatchAnalyticsModelDocument(String brand) {

        String url = "https://watchanalytics.io/en-eur/brands/" + brand;

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

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));


            for (int i = 0; i < 10; i++) { // número conservador
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(800); // tiempo para que React renderice
            }

            String html = page.content();

            browser.close();

            return Jsoup.parse(html, url);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + url, e);
        }
    }



       public static void main(String[] args) {
        Document doc = fetchWatchAnalyticsBrandDocument();

        Elements brandLinks = doc.select("a[href^=/en-eur/brands/]");
        for (Element link : brandLinks) { 
            String name = link.text().trim(); 
            String href = link.attr("href"); 
            String slug = href.substring(href.lastIndexOf("/") + 1);  // convierte el href relativo en absoluto 
            System.out.println(name + " -> " + slug); 
        }
    }
}

