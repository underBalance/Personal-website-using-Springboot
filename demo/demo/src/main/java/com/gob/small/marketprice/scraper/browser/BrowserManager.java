package com.gob.small.marketprice.scraper.browser;

import org.springframework.stereotype.Component;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

@Component
public class BrowserManager {

    private final Playwright playwright;
    private final Browser browser;

    public BrowserManager() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
        );
    }

    public synchronized BrowserContext newContext() {
        return browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent(
                                 "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/121.0.0.0 Safari/537.36"
                        )
        );
    }
}