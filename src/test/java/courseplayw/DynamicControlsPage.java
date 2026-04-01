package courseplayw;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class DynamicControlsPage {
    private final Page page;

    // Статические финальные селекторы
    private static final String REMOVE_BUTTON_SELECTOR = "button[type='button'][onclick='swapCheckbox()']";
    private static final String CHECKBOX_SELECTOR = "#checkbox";

    public DynamicControlsPage(Page page) {
        this.page = page;
    }

    /**
     * Нажатие на кнопку Remove
     */
    public void clickRemoveButton() {
        page.locator(REMOVE_BUTTON_SELECTOR).click();
    }

    /**
     * Проверка видимости чекбокса
     * @return true, если чекбокс виден, false — если нет
     */
    public boolean isCheckboxVisible() {
        return page.locator(CHECKBOX_SELECTOR).isVisible();
    }

    /**
     * Ожидание исчезновения чекбокса с таймаутом
     * @param timeout таймаут в миллисекундах
     * @return true, если элемент исчез в течение таймаута, false — в случае ошибки или таймаута
     */
    public boolean waitForCheckboxToDisappear(int timeout) {
        try {
            page.locator(CHECKBOX_SELECTOR)
                    .waitFor(new Locator.WaitForOptions()
                            .setTimeout(timeout)
                            .setState(WaitForSelectorState.HIDDEN));
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при ожидании исчезновения чекбокса: " + e.getMessage());
            return false;
        }
    }
}