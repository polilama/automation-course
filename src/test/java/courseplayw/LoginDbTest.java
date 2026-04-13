package courseplayw;

import com.microsoft.playwright.*;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

// Конфигурация подключения к базе данных

public class LoginDbTest {
    private Connection connection;
    private Page page;
    private Browser browser;
    private static DbConfig dbConfig;

    @BeforeAll
    static void loadConfig() {
        // Загружаем параметры конфигурации БД
        dbConfig = ConfigFactory.create(DbConfig.class, System.getProperties());
    }

    @BeforeEach
    void setup() throws SQLException {
        // Устанавливаем соединение с БД
        connection = DriverManager.getConnection(
                dbConfig.dbUrl(),
                dbConfig.dbUser(),
                dbConfig.dbPassword()
        );

        // Создаем тестового пользователя
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "INSERT INTO users (username, password) VALUES ('test_user', 'test_pass')"
            );
        }

        // Инициализация Playwright
        Playwright playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @Test
    void testLoginWithDbUser() throws SQLException {
        // Получение данных пользователя из БД
        String username = null;
        String password = null;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT username, password FROM users WHERE username = 'test_user'")) {

            if (rs.next()) {
                username = rs.getString("username");
                password = rs.getString("password");
            }
        }

        // Проверяем, что данные получены
        assertNotNull(username, "Username не найден в базе данных");
        assertNotNull(password, "Password не найден в базе данных");

        // Выполняем вход через UI
        page.navigate("https://the-internet.herokuapp.com/login");
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        page.locator("button[type='submit']").click();

        // Проверка успешной авторизации
        assertTrue(page.locator(".flash.success").isVisible(), "Сообщение об успехе не отображается");
        assertTrue(page.url().endsWith("/secure"), "URL не содержит /secure");
    }

    @AfterEach
    void teardown() throws SQLException {
        // Удаляем тестового пользователя
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM users WHERE username = 'test_user'");
        }
        // Закрываем ресурсы
        if (connection != null) connection.close();
        if (page != null) page.close();
        if (browser != null) browser.close();
    }
}