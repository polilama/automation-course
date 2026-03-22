package courseplayw;


import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MobileDynamicControlsTest {
    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();

        // Настройка параметров iPad Pro 11
        Browser.NewContextOptions deviceOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)")
                .setViewportSize(834, 1194)
                .setDeviceScaleFactor(2)
                .setIsMobile(true)
                .setHasTouch(true);

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext(deviceOptions);
        page = context.newPage();
    }

    @Test
    void testInputEnabling() {
        // 1. Переход на страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_controls");

        // 2. Поиск и клик по кнопке "Enable"
        Locator enableButton = page.locator("button:has-text('Enable')");
        assertTrue(enableButton.isVisible(), "Кнопка Enable не найдена");
        enableButton.click();

        // Ожидание появления/активации поля ввода
        page.waitForTimeout(3000);

        // 3. Поиск поля ввода
        Locator inputField = page.locator("input[type='text']");


        // 4. Проверка, что поле ввода стало активным (enabled)
        assertTrue(inputField.isEnabled(), "Поле ввода не стало активным после нажатия кнопки Enable");

        // Дополнительная проверка: вводим текст в поле
        inputField.fill("Тестовый текст");
        String inputValue = inputField.inputValue();
        assertTrue(!inputValue.isEmpty(), "Поле ввода не приняло текст");
    }

    @AfterEach
    void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }
}