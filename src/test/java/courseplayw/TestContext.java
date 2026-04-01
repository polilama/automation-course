package courseplayw;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class TestContext {
    private final Playwright playwright;
    private final Browser browser;
    private final Page page;

    public TestContext() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        this.page = browser.newPage();
    }

    public Page getPage() {
        return page;
    }

    public void close() {
        page.close();
        browser.close();
        playwright.close();
    }
}
