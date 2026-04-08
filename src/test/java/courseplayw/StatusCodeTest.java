package courseplayw;

import com.example.config.EnvironmentConfig;
import com.microsoft.playwright.*;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatusCodeTest {
    private EnvironmentConfig config;
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private List<Response> capturedResponses;

    @BeforeEach
    public void setup() {
        config = ConfigFactory.create(EnvironmentConfig.class, System.getenv());
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
        capturedResponses = new ArrayList<>();
    }

    @Test
    public void testStatusCode200() {
        Response response = page.waitForResponse(
                r -> r.url().contains("/status_codes/200"),
                () -> page.navigate(config.baseUrl() + "/status_codes/200")
        );
        assertNotNull(response, "Response for /status_codes/200 not received");
        assertEquals(200, response.status(), "Expected status code 200 for /status_codes/200");
    }

    @Test
    public void testStatusCode301() {
        page.onResponse(response -> {
            if (response.url().contains("/status_codes/301")) {
                capturedResponses.add(response);
            }
        });

        page.navigate(config.baseUrl() + "/status_codes/301");

        assertFalse(capturedResponses.isEmpty(), "No response captured for /status_codes/301");
        Response response = capturedResponses.get(0);
        assertEquals(301, response.status(), "Expected status code 301 for /status_codes/301");
    }

    @Test
    public void testStatusCode404() {
        Response response = page.waitForResponse(
                r -> r.url().contains("/status_codes/404"),
                () -> page.navigate(config.baseUrl() + "/status_codes/404")
        );
        assertNotNull(response, "Response for /status_codes/404 not received");
        assertEquals(404, response.status(), "Expected status code 404 for /status_codes/404");
    }

    @Test
    public void testStatusCode500() {
        Response response = page.waitForResponse(
                r -> r.url().contains("/status_codes/500"),
                () -> page.navigate(config.baseUrl() + "/status_codes/500")
        );
        assertNotNull(response, "Response for /status_codes/500 not received");
        assertEquals(500, response.status(), "Expected status code 500 for /status_codes/500");
    }

    @AfterEach
    public void teardown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}