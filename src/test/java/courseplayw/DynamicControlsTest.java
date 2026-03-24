package courseplayw;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicControlsTest {
    Playwright playwright;
    Browser browser;
    Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @Test
    void testDynamicCheckbox() {
        page.navigate("https://the-internet.herokuapp.com/dynamic_controls");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator checkbox = page.locator("input[type='checkbox']");
        assertTrue(checkbox.isVisible(), "Чекбокс должен быть изначально виден");

        // Кликаем кнопку "Remove"
        page.locator("button[onclick='swapCheckbox()']").click();

        // Ждём исчезновения чекбокса
        page.waitForSelector("input[type='checkbox']",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(10000)
        );

        // Проверяем текст "It's gone!"
        Locator message = page.getByText("It's gone!");
        page.waitForSelector("text=It's gone!",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10000)
        );
        assertEquals("It's gone!", message.textContent(), "Должен появиться текст 'It's gone!'");

        // Кликаем кнопку "Add"
        page.locator("button[onclick='swapCheckbox()']").click();

        // Ждём появления чекбокса
        page.waitForSelector("input[type='checkbox']",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10000)
        );
        assertTrue(checkbox.isVisible(), "Чекбокс должен снова появиться");
    }

    @AfterEach
    void tearDown() {
        page.close();
        browser.close();
        playwright.close();
    }
}
