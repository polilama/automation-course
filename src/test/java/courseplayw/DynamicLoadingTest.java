package courseplayw;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import java.nio.file.Paths;

public class DynamicLoadingTest {
    Playwright playwright;
    Browser browser;
    Page page;
    private boolean dynamicRequestSuccessful = false;
    private final Object lock = new Object();

    @Test
    void testDynamicLoadingWithNetworkVerification() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();

        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        page = context.newPage();

        page.onRequest(request ->
                System.out.println("→ REQUEST: " + request.method() + " " + request.url())
        );

        page.onResponse(response -> {
            String url = response.url();
            int status = response.status();

            System.out.println("← RESPONSE: " + status + " " + url);

            if (url.contains("/dynamic_loading/1") && status == 200) {
                synchronized (lock) {
                    dynamicRequestSuccessful = true;
                }
                System.out.println("Успешный запрос к /dynamic_loading/1");
            } else if (url.contains("/dynamic_loading") && status != 200) {
                System.out.println("Запрос к /dynamic_loading завершился с ошибкой " + status);
            }
        });

        System.out.println("Начинаем загрузку страницы...");
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");

        assertThat(page.locator("#finish")).isHidden();

        page.click("#start button");
        System.out.println("Запуск загрузки...");

        Locator finishText = page.locator("#finish");
        finishText.waitFor(new Locator.WaitForOptions()
                .setTimeout(10000)
                .setState(WaitForSelectorState.VISIBLE));

        assertThat(finishText).hasText("Hello World!");
        System.out.println("'Hello World!' появился");

        long startTime = System.currentTimeMillis();
        long timeout = 5000;
        while (System.currentTimeMillis() - startTime < timeout) {
            synchronized (lock) {
                if (dynamicRequestSuccessful) {
                    break;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        Assertions.assertTrue(dynamicRequestSuccessful,
                "Запрос к /dynamic_loading/1 не завершился со статусом 200)");
        System.out.println("Все проверки пройдены");

        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace/trace-success.zip")));
    }

    @AfterEach
    void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
