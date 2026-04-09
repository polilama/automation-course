package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatusCodeApiUiTest {
    private Playwright playwright;
    private APIRequestContext apiRequest;
    private Browser browser;
    private Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();

        // Настройка API контекста
        apiRequest = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://the-internet.herokuapp.com")
        );

        // Настройка браузера
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(500)
        );

        page = browser.newPage();
    }

    @Test
    void testStatusCodesCombined() {
        int[] statusCodes = {200, 404};

        for (int code : statusCodes) {
            // Перед каждым UI‑тестом возвращаемся на главную страницу статус‑кодов
            page.navigate("https://the-internet.herokuapp.com/status_codes");
            page.waitForSelector("div.example");

            // Получаем статус‑код через API
            int apiStatusCode = getApiStatusCode(code);

            // Получаем статус‑код через UI
            int uiStatusCode = getUiStatusCode(code);

            // Сравниваем результаты
            assertEquals(apiStatusCode, uiStatusCode,
                    "Статус‑коды для " + code + " не совпадают: API=" + apiStatusCode + ", UI=" + uiStatusCode);
            System.out.println("Статус‑код " + code + ": API=" + apiStatusCode + ", UI=" + uiStatusCode + " — совпадение!");
        }
    }

    private int getApiStatusCode(int code) {
        try {
            APIResponse response = apiRequest.get("/status_codes/" + code);
            return response.status();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при запросе API для кода " + code, e);
        }
    }

    private int getUiStatusCode(int code) {
        try {
            String linkText = String.valueOf(code);
            Locator link = page.locator("a:has-text(\"" + linkText + "\")").first();

            // Ждём появления элемента
            link.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            if (!link.isVisible()) {
                throw new RuntimeException("Элемент с текстом '" + linkText + "' найден, но не виден");
            }

            // Ждём ответа от сервера после клика
            Response response = page.waitForResponse(
                    res -> res.url().endsWith("/status_codes/" + code),
                    () -> link.click(new Locator.ClickOptions().setTimeout(15000))
            );

            return response.status();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при UI‑тесте для кода " + code, e);
        }
    }

    @AfterEach
    void teardown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (apiRequest != null) {
            apiRequest.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
