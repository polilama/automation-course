package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

    @Execution(ExecutionMode.CONCURRENT) // Для параллельного выполнения тестов
    public class ParallelNavigationTest {

        // Метод для получения браузера по имени (chromium или firefox)
        private Browser launchBrowser(String browserName, Playwright playwright) {
            switch (browserName.toLowerCase()) {
                case "firefox":
                    return playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
                case "chromium":
                default:
                    return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"chromium", "firefox"})
        public void testLoginPage(String browserType) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = launchBrowser(browserType, playwright);
                try (BrowserContext context = browser.newContext()) {
                    Page page = context.newPage();
                    page.navigate("https://the-internet.herokuapp.com/");
                    assertEquals("The Internet", page.title());
                }
                browser.close();
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"chromium", "firefox"})
        public void testJavaScript(String browserType) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = launchBrowser(browserType, playwright);
                try (BrowserContext context = browser.newContext()) {
                    Page page = context.newPage();
                    page.navigate("https://the-internet.herokuapp.com/status_codes");
                    assertEquals("The Internet", page.title());
                }
                browser.close();
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"chromium", "firefox"})
        public void testDropDown(String browserType) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = launchBrowser(browserType, playwright);
                try (BrowserContext context = browser.newContext()) {
                    Page page = context.newPage();
                    page.navigate("https://the-internet.herokuapp.com/dropdown");
                    assertEquals("The Internet", page.title());
                }
                browser.close();
            }
        }
    }