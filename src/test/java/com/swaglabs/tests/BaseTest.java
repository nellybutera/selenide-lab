package com.swaglabs.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.junit5.ScreenShooterExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * Shared Selenide configuration for every test class:
 * - browser + headless mode, overridable via -Dselenide.browser / -Dselenide.headless,
 *   and forced headless when running in CI (the CI=true env var).
 * - Allure reporting (screenshots, page source, browser logs) attached to every step.
 * - screenshot-on-failure via Selenide's own JUnit 5 extension.
 * - a fresh browser per test method for isolation.
 */
@ExtendWith(ScreenShooterExtension.class)
public abstract class BaseTest {

    @BeforeAll
    static void globalSetup() {
        Configuration.baseUrl = "https://www.saucedemo.com";
        Configuration.browser = System.getProperty("selenide.browser", "chrome");
        Configuration.timeout = 8000;
        Configuration.screenshots = true;
        Configuration.savePageSource = true;
        Configuration.reportsFolder = "target/screenshots";

        // "eager" moves on once the DOM is interactive instead of waiting for every
        // subresource (analytics, ads, etc.) to finish - a slow/blocked third-party
        // request should not be able to hang a page load for minutes.
        Configuration.pageLoadStrategy = "eager";
        Configuration.pageLoadTimeout = 30000;

        boolean runningInCi = Boolean.parseBoolean(System.getenv("CI"));
        boolean headlessRequested = Boolean.parseBoolean(System.getProperty("selenide.headless", "false"));
        Configuration.headless = runningInCi || headlessRequested;

        // Chrome refuses to launch as root (the user Docker/CI containers run as) unless
        // sandboxing is explicitly disabled.
        if ("chrome".equalsIgnoreCase(Configuration.browser)) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            Configuration.browserCapabilities = options;
        }

        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(true));
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }
}
