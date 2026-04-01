package courseplayw;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicTest {
        private TestContext context;
        private DynamicControlsPage controlsPage;

        @BeforeEach
        void setup() {
            context = new TestContext();

            // Внедрение Page в Page Object через конструктор (DI)
            controlsPage = new DynamicControlsPage(context.getPage());

            // Навигация на тестируемую страницу
            context.getPage().navigate("https://the-internet.herokuapp.com/dynamic_controls");
            context.getPage().waitForLoadState(LoadState.NETWORKIDLE);
        }

        @Test
        @DisplayName("Тест: Чекбокс исчезает после нажатия кнопки Remove")
        void testCheckboxRemoval() {
            // Предварительная проверка: чекбокс изначально виден
            assertTrue(
                    controlsPage.isCheckboxVisible(),
                    "Чекбокс должен быть виден до нажатия кнопки"
            );

            // Действие: нажатие на кнопку Remove
            controlsPage.clickRemoveButton();

            // Ожидание исчезновения элемента (до 10 секунд)
            boolean disappeared = controlsPage.waitForCheckboxToDisappear(10000);

            // Проверка: чекбокс действительно исчез
            assertTrue(
                    disappeared,
                    "Чекбокс должен исчезнуть после нажатия кнопки Remove"
            );
        }

        @AfterEach
        void teardown() {
            // Закрытие ресурсов
            context.close();
        }
    }