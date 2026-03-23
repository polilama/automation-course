package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MobileDragAndDropTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();

        // Ручная настройка параметров Samsung Galaxy S22 Ultra
        Browser.NewContextOptions deviceOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Linux; Android 12; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36")
                .setViewportSize(384, 873)  // Разрешение экрана
                .setDeviceScaleFactor(3.5)
                .setIsMobile(true)
                .setHasTouch(true);

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext(deviceOptions);
        page = context.newPage();
    }

    @Test
    void testDragAndDropMobile() {
        // 1. Переход на страницу с тестом перетаскивания
        page.navigate("https://the-internet.herokuapp.com/drag_and_drop");

        // 2. Поиск элементов для перетаскивания
        Locator columnA = page.locator("#column-a");
        Locator columnB = page.locator("#column-b");

        // 3. Проверка начального состояния
        String initialTextA = columnA.locator("header").textContent();
        String initialTextB = columnB.locator("header").textContent();

        assertEquals("A", initialTextA, "Начальный текст в колонке A должен быть 'A'");
        assertEquals("B", initialTextB, "Начальный текст в колонке B должен быть 'B'");

        // 4. Выполнение перетаскивания элемента A в зону B
        columnA.dragTo(columnB);

        // 5. Ожидание небольшого времени для завершения анимации/операции (опционально)
        page.waitForTimeout(1000);

        // 6. Проверка результата: текст в зоне B должен измениться на "A"
        String finalTextB = columnB.locator("header").textContent();
        assertEquals("A", finalTextB, "После перетаскивания текст в колонке B должен стать 'A'");
    }

    @AfterEach
    void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }
}
