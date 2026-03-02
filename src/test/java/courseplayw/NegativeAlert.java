package courseplayw;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Epic("Тесты алертов")
@Feature("Проверка страниц с JavaScript-алертами")
public class NegativeAlert {
    private static ExtentReports extent;
    private Browser browser;
    private Playwright playwright;
    private Page page;
    private ExtentTest test;
    private static final Path ERRORS_DIR = Paths.get("allure-results");
    private boolean testFailed = false;

    @BeforeAll
    static void setupExtent() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("target/extent-report.html");
        reporter.config().setDocumentTitle("Playwright Extent Report");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        test = extent.createTest(testInfo.getDisplayName());
        logExtent(Status.INFO, "Запуск теста: " + testInfo.getDisplayName());

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
        BrowserContext context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        try {
            if (page != null) {
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                if (testFailed) {
                    // Скриншот ошибки для Allure
                    try (InputStream errorStream = new ByteArrayInputStream(screenshot)) {
                        Allure.addAttachment("Скриншот ошибки", "image/png", errorStream, ".png");
                    }
                    logExtent(Status.FAIL, "Тест завершился с ошибкой");
                } else {
                    // Скриншот успеха для ExtentReports
                    test.addScreenCaptureFromPath("screenshot.png", "Успешное выполнение");
                }
            }
        } catch (Exception e) {
            logExtent(Status.WARNING, "Ошибка при создании скриншота: " + e.getMessage());
        } finally {
            try {
                if (playwright != null) playwright.close();
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии Playwright: " + e.getMessage());
            }
        }
    }

    @AfterAll
    static void tearDown() {
        extent.flush();
    }

    @Step("Переход на страницу с алертами")
    private void navigateToAlertsPage() {
        page.navigate("https://the-internet.herokuapp.com/javascript_alerts");
        page.waitForLoadState(LoadState.LOAD);
        logExtent(Status.INFO, "Страница загружена успешно");
    }

    @Step("Клик на кнопку '{buttonText}'")
    private void clickAlertButton(String buttonText) {
        page.locator("text=" + buttonText).waitFor();
        page.locator("text=" + buttonText).click();
        logExtent(Status.INFO, "Клик на кнопке: " + buttonText);
    }

    @Step("Проверка результата: ожидается '{expectedText}'")
    private void verifyResult(String expectedText) {
        String resultText = page.locator("#result").textContent();
        Assertions.assertEquals(expectedText, resultText);
        logExtent(Status.PASS, "Результат проверен успешно: " + resultText);
    }

    @Test
    @Story("Проверка страницы javascript_alerts")
    @Description("Тест проверяет загрузку страницы и обработку JavaScript-алерта")
    @Severity(SeverityLevel.CRITICAL)
    void testJavaScriptAlert() {
        try {
            // Настройка обработчика алерта
            page.onDialog(dialog -> {
                logExtent(Status.INFO, "Alert text: " + dialog.message());
                dialog.accept();
            });

            navigateToAlertsPage();
            clickAlertButton("Click for JS Alert");
            verifyResult("You clicked: Ok");

            logExtent(Status.PASS, "Тест успешно пройден: Alert обработан корректно");
        } catch (AssertionError e) {
            testFailed = true;
            logExtent(Status.FAIL, "Ошибка утверждения: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            testFailed = true;
            logExtent(Status.FAIL, "Неожиданная ошибка: " + e.getMessage());
            throw e;
        }
    }

    private void logExtent(Status status, String message) {
        if (test != null) {
            test.log(status, message);
        }
    }
}
