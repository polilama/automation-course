package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ParallelTests {

    @Test
    public void testLoginPage() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            try (BrowserContext context = browser.newContext()) {
                Page page = context.newPage();
                page.navigate("https://the-internet.herokuapp.com/login");
                assertEquals("The Internet", page.title());
               // assertTrue(page.isVisible("input#username"));
               // assertTrue(page.isVisible("input#password"));
               // assertTrue(page.isVisible("button:has-text('Login')"));
            }
            browser.close();
        }
    }

    @Test
    public void testAddRemoveElements() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            try (BrowserContext context = browser.newContext()) {
                Page page = context.newPage();
                page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
                page.click("button:text('Add Element')");
                assertTrue(page.isVisible("button.added-manually"));
                page.click("button.added-manually");
                assertFalse(page.isVisible("button.added-manually"),
                        "Кнопка должна исчезнуть после удаления");
            }
            browser.close();
        }
    }
}
