package courseplayw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DragDropTest {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private DragDropPage dragDropPage;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        dragDropPage = new DragDropPage(page);
    }

    @Test
    void testDragAndDrop() {
        // Навигация на страницу с формой Drag and Drop
        dragDropPage.navigateTo("https://the-internet.herokuapp.com/drag_and_drop");

        // Выполнение перетаскивания с использованием цепочки вызовов
        dragDropPage.performDragAndDropAndVerify();

        // Проверка результата
        assertTrue(
                dragDropPage.verifyDragResult("A"),
                "Текст в зоне B должен измениться на 'A' после перетаскивания"
        );
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