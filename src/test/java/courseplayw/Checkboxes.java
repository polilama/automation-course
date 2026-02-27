package courseplayw;


import com.microsoft.playwright.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Веб-интерфейс тестов")
@Feature("Операции с чекбоксами")
public class Checkboxes {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeEach
    @Step("Инициализация браузера и контекста")
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    @Story("Проверка работы чекбоксов")
    @DisplayName("Тестирование выбора/снятия чекбоксов")
    @Severity(SeverityLevel.CRITICAL)
    void testCheckboxes() {
        navigateToCheckboxesPage();
        verifyInitialState();
        toggleCheckboxes();
        verifyToggledState();
    }

    @Step("Переход на страницу /checkboxes")
    private void navigateToCheckboxesPage() {
        page.navigate("https://the-internet.herokuapp.com/checkboxes");
        assertEquals("The Internet", page.title());
    }

    @Step("Проверка начального состояния чекбоксов")
    private void verifyInitialState() {
        Locator firstCheckbox = page.locator("input[type='checkbox']:nth-of-type(1)");
        Locator secondCheckbox = page.locator("input[type='checkbox']:nth-of-type(2)");

        assertThat(firstCheckbox.isChecked()).isFalse();
        assertThat(secondCheckbox.isChecked()).isTrue();
    }

    @Step("Изменение состояния чекбоксов")
    private void toggleCheckboxes() {
        Locator firstCheckbox = page.locator("input[type='checkbox']:nth-of-type(1)");
        Locator secondCheckbox = page.locator("input[type='checkbox']:nth-of-type(2)");

        firstCheckbox.click();
        secondCheckbox.click();
    }

    @Step("Проверка изменённого состояния чекбоксов")
    private void verifyToggledState() {
        Locator firstCheckbox = page.locator("input[type='checkbox']:nth-of-type(1)");
        Locator secondCheckbox = page.locator("input[type='checkbox']:nth-of-type(2)");

        assertThat(firstCheckbox.isChecked()).isTrue();
        assertThat(secondCheckbox.isChecked()).isFalse();

    }

    @AfterEach
    @Step("Закрытие ресурсов")
    void tearDown() {
        context.close();
        browser.close();
        playwright.close();
    }
}
