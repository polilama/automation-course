package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;

public class DynamicLoadingTraceTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @Test
    void testDynamicLoadingWithTrace() {
        // Создаем Playwright
        playwright = Playwright.create();
        // Запускаем браузер
        browser = playwright.chromium().launch();

        // Создаем контекст с трассировкой
        context = browser.newContext();

        // Начинаем трассировку с захватом скриншотов и снапшотов DOM
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
        );

        // Создаем новую страницу
        page = context.newPage();

        // Навигация на страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");

        // Кликаем по кнопке "Start"
        page.click("button");

        // Ожидаем появления текста "Hello World!"
        page.locator("#finish").waitFor();

        // Проверка, что текст появился
        Assertions.assertTrue(page.locator("#finish").innerText().contains("Hello World!"));

        // Остановка трассировки и сохранение файла
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace-dynamic-loading.zip"))
        );
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}