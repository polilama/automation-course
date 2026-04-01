package courseplayw;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DragDropArea {
    private final Page page;
    private static final String ELEMENT_A_SELECTOR = "#column-a";
    private static final String ELEMENT_B_SELECTOR = "#column-b";

    public DragDropArea(Page page) {
        this.page = page;
    }

    /**
     * Перетаскивает элемент A в зону B с проверкой видимости элементов
     */
    public DragDropArea dragAToB() {
        Locator elementA = page.locator(ELEMENT_A_SELECTOR);
        Locator elementB = page.locator(ELEMENT_B_SELECTOR);

        // Ожидание видимости элементов перед действием
        elementA.waitFor();
        elementB.waitFor();

        elementA.dragTo(elementB);

        // Небольшая пауза для завершения анимации
        page.waitForTimeout(500);
        return this;
    }

    /**
     * Получает текст из зоны B
     */
    public String getTextB() {
        return page.locator(ELEMENT_B_SELECTOR).textContent().trim();
    }

    /**
     * Проверяет, что текст в зоне B соответствует ожидаемому
     */
    public boolean isTextInBA(String expectedText) {
        return getTextB().equals(expectedText);
    }
}