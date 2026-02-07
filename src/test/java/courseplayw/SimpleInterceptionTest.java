package courseplayw;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class SimpleInterceptionTest {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void simpleInterceptionTest() {
        // 1. Настраиваем перехват POST-запроса к /authenticate
        page.route("**/authenticate", route -> {
            System.out.println("Запрос перехвачен!");

            // Получаем оригинальные данные запроса
            Request request = route.request();
            String postData = request.postData();

            System.out.println("Было: " + postData);

            // Преобразуем POST-данные в Map для удобной модификации
            Map<String, String> params = new HashMap<>();
            if (postData != null) {
                String[] keyValuePairs = postData.split("&");
                for (String pair : keyValuePairs) {
                    String[] parts = pair.split("=", 2); // Разделяем только по первому "="
                    if (parts.length == 2) {
                        params.put(parts[0], parts[1]);
                    }
                }
            }

            // Меняем значение username на "HACKED_USER"
            params.put("username", "HACKED_USER");
            System.out.println("Стало: " + params);


            // Формируем новое тело запроса
            StringBuilder newPostData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (newPostData.length() > 0) {
                    newPostData.append("&");
                }
                newPostData
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
            }

            // Создаем ResumeOptions с модифицированными данными
            Route.ResumeOptions options = new Route.ResumeOptions();
            options.setPostData(newPostData.toString().getBytes(StandardCharsets.UTF_8));

            // Отправляем измененный запрос
            route.resume(options);
        });

        // 2. Переходим на страницу логина
        page.navigate("https://the-internet.herokuapp.com/login");


        // 3. Заполняем форму логина (оригинальные данные)
        page.locator("#username").fill("tomsmith");
        page.locator("#password").fill("SuperSecretPassword!");


        // 4. Нажимаем кнопку "Login"
        page.click("button[type='submit']");


        // 5. Ждем появления сообщения и проверяем результат
        page.waitForSelector("#flash", new Page.WaitForSelectorOptions().setTimeout(5000));
        String flashMessage = page.locator("#flash").textContent();

        // Проверяем, что сообщение содержит ожидаемый текст об ошибке
        Assertions.assertTrue(
                flashMessage.contains("Your username is invalid!"),
                "Ожидалось сообщение об ошибке аутентификации, но получено: " + flashMessage
        );
    }

    @AfterAll
    static void tearDownAll() {
        browser.close();
        playwright.close();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }
}
