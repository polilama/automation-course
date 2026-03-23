package courseplayw;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HoverTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testHoverProfiles() {
        // Переход на тестовую страницу
        page.navigate("https://the-internet.herokuapp.com/hovers");

        // Находим все элементы с классом .figure
        Locator figures = page.locator(".figure");
        int count = figures.count();

        for (int i = 0; i < count; i++) {
            Locator figure = figures.nth(i);

            // Наводим курсор на элемент
            figure.hover();

            // Проверяем, что появилась ссылка "View profile"
            Locator profileLink = figure.locator("text=View profile");
            assertTrue(profileLink.isVisible(),
                    "Ссылка 'View profile' должна появиться после наведения курсора на элемент " + i);

            // Кликаем на ссылку "View profile"
            profileLink.click();

            // Ждём загрузки страницы и проверяем, что URL содержит /users/{id}
            page.waitForURL(url -> url.contains("/users/"));
            String currentUrl = page.url();
            assertTrue(currentUrl.contains("/users/"),
                    "URL должен содержать '/users/', но текущий URL: " + currentUrl);

            // Возвращаемся назад для проверки следующего элемента
            page.goBack();
            page.waitForLoadState(LoadState.LOAD); // Ждём полной загрузки страницы
        }
    }

    @AfterAll
    static void teardownClass() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }
}
