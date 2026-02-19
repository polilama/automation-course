package courseplayw;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(io.qameta.allure.junit5.AllureJunit5.class)
public class CartFixTest {

    private static final String BASE_DIR = "target/allure-results";
    private static final String VIDEOS_DIR = BASE_DIR + "/videos";
    private static final String SCREENSHOTS_DIR = BASE_DIR + "/screenshots";

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;
    private String testName;
    private String timestampDir;

    @BeforeAll
    static void setupAll() throws IOException {
        Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
        Files.createDirectories(Paths.get(VIDEOS_DIR));
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
    }

    @AfterAll
    static void teardownAll() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void init(TestInfo testInfo) {
        // создаем директорию с текущей датой/временем
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        timestampDir = LocalDateTime.now().format(formatter);
        Path dir = Paths.get(BASE_DIR, timestampDir);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // создаем контекст с записью видео
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get(dir.toString()))
        );

        page = context.newPage();
        // навигация к тестовой странице
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        testName = testInfo.getDisplayName();
    }

    @Test
    void testCartFix() {
        try {
            // 1. Открытие страницы
            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/",
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // 2. Добавление элемента
            page.waitForSelector("button:has-text('Add Element')",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("button:has-text('Add Element')");
            // Скриншот корзины
            attachElementScreenshot("cart", page.locator("button:has-text('Delete')"));

            // 3. Удаление элемента
            page.waitForSelector("button:has-text('Delete')",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("button:has-text('Delete')");
            attachElementScreenshot("cart", page.locator("button:has-text('Delete')"));

            // Проверка
            assertEquals(2, page.locator("button:has-text('Delete')").count());
        } catch (Exception e) {
            // при ошибке делаем скриншот всей страницы
            attachFullPageScreenshot("Error_" + testName);
            throw e;
        }
    }

    private void attachElementScreenshot(String name, Locator locator) {
        byte[] bytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        String filename = name + "_" + System.currentTimeMillis() + ".png";
        Path filePath = Paths.get(SCREENSHOTS_DIR, timestampDir, filename);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, bytes);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attachFullPageScreenshot(String name) {
        byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        String filename = name + "_" + System.currentTimeMillis() + ".png";
        Path filePath = Paths.get(SCREENSHOTS_DIR, timestampDir, filename);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshotBytes);
            // Прикрепляем в Allure
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void afterEach() throws IOException {
        // сохраняем видео
        if (page.video() != null) {
            Video video = page.video();
            Path videoFile = video.path();
            if (videoFile != null && Files.exists(videoFile)) {
                Path targetPath = Paths.get(BASE_DIR, timestampDir, "video_" + System.currentTimeMillis() + ".webm");
                Files.copy(videoFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                Allure.addAttachment("Test Video", "video/webm", Files.newInputStream(targetPath), ".webm");
            }
        }

        // закрываем контекст
        if (context != null) {
            context.close();
        }
    }
}