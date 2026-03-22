package courseplayw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TodoApiTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL("https://jsonplaceholder.typicode.com")
                        .setExtraHTTPHeaders(Map.of("Accept", "application/json"))
        );
    }

    @Test
    void testTodoApi() throws Exception {
        // 1. Выполнение GET-запроса
        APIResponse response = requestContext.get("/todos/1");

        // 2. Проверка статуса ответа
        assertEquals(200, response.status(), "Статус ответа должен быть 200");

        // 3. Парсинг JSON
        String responseText = response.text();
        Map<String, Object> jsonResponse = objectMapper.readValue(responseText, Map.class);

        System.out.println("Полученный JSON: " + responseText);

        // 4. Проверка структуры JSON
        // Проверяем наличие обязательных полей
        assertNotNull(jsonResponse.get("userId"), "Поле 'userId' должно присутствовать в ответе");
        assertNotNull(jsonResponse.get("id"), "Поле 'id' должно присутствовать в ответе");
        assertNotNull(jsonResponse.get("title"), "Поле 'title' должно присутствовать в ответе");
        assertNotNull(jsonResponse.get("completed"), "Поле 'completed' должно присутствовать в ответе");
    }

    @AfterEach
    void tearDown() {
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
