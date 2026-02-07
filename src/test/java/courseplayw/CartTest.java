package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setupPlaywright() {
    }

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/")));
        page = context.newPage();
    }

    @Test
    void testCartActions() {
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");

        // 1. Добавление элемента
        page.click("button:has-text('Add Element')");
        assertTrue(page.locator("button:has-text('Delete')").count() > 0);

        // Скриншот после добавления
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(getTimestampPath("cart_after_add.png")));

        // 2. Удаление элемента
        page.click("button:has-text('Delete')");
        assertEquals(0, page.locator("button:has-text('Delete')").count());

        // Скриншот после удаления
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(getTimestampPath("cart_after_remove.png")));
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private Path getTimestampPath(String filename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return Paths.get(timestamp, filename);
    }
}
