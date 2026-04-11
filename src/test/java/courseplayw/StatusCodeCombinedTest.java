package courseplayw;

import com.example.config.EnvironmentConfig;
import com.microsoft.playwright.*;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StatusCodeCombinedTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;
    private static EnvironmentConfig config;

    @BeforeAll
    static void loadConfig() {
        config = ConfigFactory.create(EnvironmentConfig.class, System.getenv());

        String baseUrl = config.baseUrl();
        assertNotNull(baseUrl, "baseUrl не может быть null");
        assertTrue(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"),
                "baseUrl должен начинаться с http:// или https://");
        assertFalse(baseUrl.contains(" "), "baseUrl не должен содержать пробелов");
    }

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(config.baseUrl())
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(100)
        );

        page = browser.newPage();
        page.setDefaultTimeout(40000);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 404})
    void testStatusCodeCombined(int statusCode) {
        int apiStatusCode = getApiStatusCode(statusCode);
        int uiStatusCode = getUiStatusCode(statusCode);

        assertEquals(apiStatusCode, uiStatusCode,
                "Расхождение в статус-кодах для " + statusCode +
                        ": API=" + apiStatusCode + ", UI=" + uiStatusCode);
    }

    private int getApiStatusCode(int code) {
        try {
            APIResponse response = apiRequest.get("/status_codes/" + code);
            assertEquals(code, response.status(),
                    "API: Неверный статус код для " + code +
                            ". Ожидался " + code + ", получен " + response.status());
            return response.status();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка API‑запроса для кода " + code +
                    ". Базовый URL: " + config.baseUrl(), e);
        }
    }

    private int getUiStatusCode(int code) {
        try {
            String url = config.baseUrl() + "/status_codes";
            System.out.println("UI: Навигация на " + url);
            page.navigate(url);
            page.waitForSelector("div.example");

            Locator link = page.locator(
                    String.format("a[href*='status_codes/%d']", code)
            ).first();

            Response response = page.waitForResponse(
                    res -> res.url().endsWith("/status_codes/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(10000))
            );

            return response.status();
        } catch (Exception e) {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("screenshot-error-" + code + ".png")));
            throw new RuntimeException("UI проверка упала для кода " + code, e);
        }
    }

    @AfterEach
    void teardown() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (apiRequest != null) apiRequest.dispose();
        if (playwright != null) playwright.close();
    }
}
