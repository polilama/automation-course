package courseplayw;

import com.github.javafaker.Faker;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicContentTest {

    @Test
    void testDynamicContentWithMockedAPI() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            // 1. Генерация случайного имени пользователя с помощью Faker
            Faker faker = new Faker();
            String generatedUserName = faker.name().fullName();
            System.out.println("Сгенерировано имя: " + generatedUserName);

            // 2. Мокирование API — перехват запроса и замена ответа
            page.route("**/dynamic_content", route -> {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("text/html") // Для этой страницы подходит text/html
                        .setBody("<div class='large-6'>" + generatedUserName + "</div>")
                );
            });

            // 3. Навигация на страницу
            page.navigate("https://the-internet.herokuapp.com/dynamic_content");

            // 4. Ожидание загрузки страницы и появления контента
            page.waitForLoadState(LoadState.NETWORKIDLE); // Ждём завершения сетевых запросов

            // 5. Поиск элемента с именем пользователя
            Locator nameElement = page.locator("div.large-6:has-text('" + generatedUserName + "')");

            try {
                // Ждём видимости элемента с таймаутом 10 с
                nameElement.waitFor(new Locator.WaitForOptions().setTimeout(10000));

                // 6. Проверка, что сгенерированное имя отображается на странице
                String actualText = nameElement.textContent().trim();
                assertTrue(
                        actualText.contains(generatedUserName),
                        "Сгенерированное имя '" + generatedUserName + "' должно отображаться на странице, но найден текст: '" + actualText + "'"
                );
            } catch (TimeoutError e) {
                // Если элемент не найден, выводим отладочную информацию
                System.err.println("Элемент не найден за отведённое время!");
                System.err.println("Текущий URL: " + page.url());
                System.err.println("Содержимое страницы: " + page.textContent("body"));
                throw e;
            }

            // Пауза для визуального контроля (опционально)
            page.waitForTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Перебрасываем исключение, чтобы тест провалился
        }
    }
}