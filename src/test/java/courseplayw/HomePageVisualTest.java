package courseplayw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;

import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;

public class HomePageVisualTest {
    // Пути к файлам
    private final Path expectedPath = Paths.get("expected.png");
    private final Path actualPath = Paths.get("actual.png");
    private final Path diffPath = Paths.get("diff.png");

    private static Playwright playwright;
    private static Browser browser;

    private Page page;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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
    void setup() {
        BrowserContext context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void cleanup() {
        if (page != null) {
            page.context().close();
        }
    }

    @Test
    void testHomePageVisual() throws Exception {
        // Навигация к странице
        page.navigate("https://the-internet.herokuapp.com/");

        // Сделать скриншот
        page.screenshot(new Page.ScreenshotOptions().setPath(actualPath).setFullPage(true));

        // Если эталонного файла нет, создаем его и завершаем тест
        if (!Files.exists(expectedPath)) {
            Files.copy(actualPath, expectedPath);
            Assertions.fail("Эталонный скриншот создан: " + expectedPath.toString());
        }

        // Сравнить файлы
        long mismatch = Files.mismatch(actualPath, expectedPath);

        if (mismatch != -1) {
            // Обнаружены различия - создаем diff
            createImageDiff(expectedPath, actualPath, diffPath);

            // Прикрепляем diff к отчету
            byte[] diffBytes = Files.readAllBytes(diffPath);
            Allure.addAttachment("Visual Diff", "image/png", new ByteArrayInputStream(diffBytes), ".png");

            Assertions.fail("Обнаружены различия в скриншотах. См. diff: " + diffPath.toString());
        }
    }

    private void createImageDiff(Path expected, Path actual, Path diffOutput) throws Exception {
        String command = String.format("convert %s %s -compose difference -composite %s",
                expected.toAbsolutePath().toString(),
                actual.toAbsolutePath().toString(),
                diffOutput.toAbsolutePath().toString());

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Ошибка при запуске ImageMagick для создания diff");
        }
    }
}
