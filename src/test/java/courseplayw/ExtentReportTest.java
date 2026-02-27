//package courseplayw;

//import com.aventstack.extentreports.*;
//import com.aventstack.extentreports.reporter.ExtentSparkReporter;
//import com.microsoft.playwright.Browser;
//import com.microsoft.playwright.Page;
//import com.microsoft.playwright.Playwright;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//public class ExtentReportTest {
    //private static ExtentReports extent;

    //@BeforeAll
    //static void setup() {
        // Настраиваем репортер (формат HTML + путь)
        //ExtentSparkReporter reporter = new ExtentSparkReporter("target/extent-report.html");
        //reporter.config().setDocumentTitle("Playwright Report"); // Заголовок отчета
        //extent = new ExtentReports();
        //extent.attachReporter(reporter); // Привязываем репортер
    //}

   // @Test
    //void testLoginPage() {
       //ExtentTest test = extent.createTest("Тест страницы логина");

       // try (Playwright playwright = Playwright.create();
            // Browser browser = playwright.chromium().launch()) {

           // test.log(Status.INFO, "Браузер запущен");
            //Page page = browser.newPage();
           // page.navigate("https://the-internet.herokuapp.com/login");

           // test.log(Status.PASS, "Страница загружена");
           // assertThat(page.title()).isEqualTo("Login Page");

        //} catch (Exception e) {
          //  test.log(Status.FAIL, "Ошибка: " + e.getMessage());
       // }
   // }

    //@AfterAll
    //static void tearDown() {
        //extent.flush(); // Сохраняем отчет на диск
   // }
//}
