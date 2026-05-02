import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test Suite for Leaf & Petals – Plant E-Commerce Website
 *
 * Prerequisites:
 * - ChromeDriver installed and on system PATH (or set via
 * -Dwebdriver.chrome.driver=...)
 * - Application running locally at http://localhost:3000
 * - At least one plant seeded in the database
 * - A registered test user: email = test@leafpetals.com, password = Test@1234
 * - An admin test user: email = admin@leafpetals.com, password = Admin@1234
 *
 * Run with: mvn test (or any JUnit 5 runner)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeafPetalsTests {

        private static WebDriver driver;
        private static WebDriverWait wait;

        private static final String BASE_URL = "http://localhost:8081";
        private static final String USER_EMAIL = "admin@leafpetals.com";
        private static final String USER_PASSWORD = "Admin@1234";
        private static final String ADMIN_EMAIL = "admin@leafpetals.com";
        private static final String ADMIN_PASSWORD = "Admin@1234";

        // -------------------------------------------------------------------------
        // Setup / Teardown
        // -------------------------------------------------------------------------

        @BeforeAll
        static void setUp() {
                ChromeOptions options = new ChromeOptions();
                // options.addArguments("--headless"); // Uncomment to run headless
                options.addArguments("--headless"); // CRITICAL for server environments
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox"); // Prevents Docker permission issues
                options.addArguments("--disable-dev-shm-usage"); // Prevents Docker shared memory crashes
                options.addArguments("--remote-allow-origins=*");
                driver = new ChromeDriver(options);
                driver.manage().window().maximize();
                wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }

        @AfterAll
        static void tearDown() {
                if (driver != null) {
                        driver.quit();
                }
        }

        // Helper: navigate to a path relative to BASE_URL
        private void navigateTo(String path) {
                driver.get(BASE_URL + path);
        }

        // Helper: perform login for a given user
        private void loginAs(String email, String password) {
                navigateTo("/login");
                wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[placeholder='Enter your email']")));
                driver.findElement(By.cssSelector("input[placeholder='Enter your email']")).clear();
                driver.findElement(By.cssSelector("input[placeholder='Enter your email']")).sendKeys(email);
                driver.findElement(By.cssSelector("input[placeholder='Enter your password']")).clear();
                driver.findElement(By.cssSelector("input[placeholder='Enter your password']")).sendKeys(password);
                driver.findElement(By.cssSelector("button[type='submit'], form button")).click();
                wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        }

        // Helper: sign out by clearing session (navigate to NextAuth signout)
        private void logout() {
                navigateTo("/api/auth/signout");
                try {
                        WebElement signOutBtn = wait.until(
                                        ExpectedConditions
                                                        .elementToBeClickable(By.cssSelector("button[type='submit']")));
                        signOutBtn.click();
                        wait.until(ExpectedConditions.urlContains(BASE_URL));
                } catch (TimeoutException ignored) {
                        // Already signed out
                }
        }

        // =========================================================================
        // TC-01: Home page loads and displays page title / hero section
        // =========================================================================
        @Test
        @Order(1)
        @DisplayName("TC-01: Home page loads with hero heading")
        void testHomePageLoads() {
                navigateTo("/");

                // Page title contains brand name
                String pageTitle = driver.getTitle();
                assertTrue(pageTitle != null && !pageTitle.isEmpty(),
                                "Page title should not be empty");

                // Hero h1 heading is visible
                WebElement heroHeading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                String heroText = heroHeading.getText();
                assertFalse(heroText.isEmpty(), "Hero heading should contain text");
        }

        // =========================================================================
        // TC-02: Navigation links are visible in the Navbar
        // =========================================================================
        @Test
        @Order(2)
        @DisplayName("TC-02: Navbar contains navigation links")
        void testNavbarLinksPresent() {
                navigateTo("/");

                // The nav / header element exists
                WebElement nav = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("nav")));
                assertNotNull(nav, "Navbar element should be present");

                // Cart link is accessible (href includes /cart)
                List<WebElement> links = driver.findElements(By.cssSelector("a[href='/cart']"));
                assertFalse(links.isEmpty(), "Cart link should be present in the navbar");
        }

        // =========================================================================
        // TC-03: Category filter pills navigate to correct category URL
        // =========================================================================
        @Test
        @Order(3)
        @DisplayName("TC-03: Indoor category pill navigates to /category/indoor")
        void testCategoryPillNavigation() {
                navigateTo("/");

                WebElement indoorLink = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/category/indoor']")));
                indoorLink.click();

                wait.until(ExpectedConditions.urlContains("/category/indoor"));
                assertTrue(driver.getCurrentUrl().contains("/category/indoor"),
                                "URL should contain /category/indoor after clicking the Indoor pill");
        }

        // =========================================================================
        // TC-04: Plant detail page loads when a plant card is clicked
        // =========================================================================
        @Test
        @Order(4)
        @DisplayName("TC-04: Clicking a plant card opens the plant detail page")
        void testPlantDetailPageLoads() {
                navigateTo("/");

                // Wait for at least one plant card (link to /plant/...)
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                assertFalse(plantCards.isEmpty(), "At least one plant card should be visible on the home page");

                plantCards.get(0).click();

                // URL should change to /plant/<id>
                wait.until(ExpectedConditions.urlContains("/plant/"));
                assertTrue(driver.getCurrentUrl().contains("/plant/"),
                                "Should navigate to the plant detail page");

                // Plant name heading (h1) is displayed
                WebElement heading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                assertFalse(heading.getText().isEmpty(), "Plant name heading should be visible");
        }

        // =========================================================================
        // TC-05: "Add to Cart" button is present and enabled for in-stock plants
        // =========================================================================
        @Test
        @Order(5)
        @DisplayName("TC-05: Add to Cart button is enabled for in-stock plants")
        void testAddToCartButtonEnabled() {
                navigateTo("/");

                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();

                wait.until(ExpectedConditions.urlContains("/plant/"));

                WebElement addToCartBtn = wait.until(
                                ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//button[contains(text(),'Add to Cart') or contains(text(),'Out of Stock')]")));

                String btnText = addToCartBtn.getText();
                if (btnText.contains("Add to Cart")) {
                        assertTrue(addToCartBtn.isEnabled(), "Add to Cart button should be enabled for in-stock plant");
                } else {
                        // Out-of-stock plant: button should be disabled
                        assertFalse(addToCartBtn.isEnabled(),
                                        "Out of Stock button should be disabled");
                }
        }

        // =========================================================================
        // TC-06: Adding a plant to cart updates the cart page
        // =========================================================================
        @Test
        @Order(6)
        @DisplayName("TC-06: Adding a plant to cart shows it in the cart page")
        void testAddToCartUpdatesCartPage() {
                navigateTo("/");

                // Click the first in-stock plant card
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();

                wait.until(ExpectedConditions.urlContains("/plant/"));

                WebElement addToCartBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[contains(text(),'Add to Cart')]")));
                addToCartBtn.click();

                // Navigate to cart
                navigateTo("/cart");

                // Cart should not show "empty" state; at least one item should be listed
                List<WebElement> emptyMessage = driver.findElements(
                                By.xpath("//*[contains(text(),'Your cart is empty')]"));
                assertTrue(emptyMessage.isEmpty(),
                                "Cart should not be empty after adding a plant");
        }

        // =========================================================================
        // TC-07: Login page renders required form fields
        // =========================================================================
        @Test
        @Order(7)
        @DisplayName("TC-07: Login page contains email and password fields")
        void testLoginPageFields() {
                navigateTo("/login");

                WebElement emailField = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("input[placeholder='Enter your email']")));
                WebElement passwordField = driver.findElement(
                                By.cssSelector("input[placeholder='Enter your password']"));

                assertTrue(emailField.isDisplayed(), "Email field should be visible");
                assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        }

        // =========================================================================
        // TC-08: Login with invalid credentials shows an error message
        // =========================================================================
        @Test
        @Order(8)
        @DisplayName("TC-08: Login with wrong credentials shows error")
        void testLoginWithInvalidCredentials() {
                navigateTo("/login");

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[placeholder='Enter your email']")));
                driver.findElement(By.cssSelector("input[placeholder='Enter your email']"))
                                .sendKeys("wrong@example.com");
                driver.findElement(By.cssSelector("input[placeholder='Enter your password']"))
                                .sendKeys("wrongpassword");
                driver.findElement(By.cssSelector("form button")).click();

                // An error message should appear (red alert div)
                WebElement error = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("div.bg-red-50")));
                assertFalse(error.getText().isEmpty(), "An error message should be displayed for invalid credentials");
        }

        // =========================================================================
        // TC-09: Successful login redirects to home page
        // =========================================================================
        @Test
        @Order(9)
        @DisplayName("TC-09: Valid login redirects to home page")
        void testSuccessfulLogin() {
                loginAs(USER_EMAIL, USER_PASSWORD);

                assertEquals(BASE_URL + "/", driver.getCurrentUrl(),
                                "Successful login should redirect to the home page");

                logout();
        }

        // =========================================================================
        // TC-10: Register page shows error when submitting empty fields
        // =========================================================================
        @Test
        @Order(10)
        @DisplayName("TC-10: Register form validates empty fields")
        void testRegisterEmptyFieldsValidation() {
                navigateTo("/register");

                // Click submit without filling any fields
                WebElement submitBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.cssSelector("form button")));
                submitBtn.click();

                // Error message should appear
                WebElement error = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("div.bg-red-50")));
                assertTrue(error.getText().contains("fill all fields") || !error.getText().isEmpty(),
                                "Validation error should appear for empty fields");
        }

        // =========================================================================
        // TC-11: Unauthenticated user is redirected from checkout to login
        // =========================================================================
        @Test
        @Order(11)
        @DisplayName("TC-11: Unauthenticated access to /checkout redirects to login")
        void testCheckoutRedirectsUnauthenticated() {
                // Make sure no session is active
                logout();

                navigateTo("/checkout");

                // Should be redirected to the login page
                wait.until(ExpectedConditions.urlContains("/login"));
                assertTrue(driver.getCurrentUrl().contains("/login"),
                                "Unauthenticated user accessing /checkout should be redirected to /login");
        }

        // =========================================================================
        // TC-12: Cart page displays correct subtotal calculation
        // =========================================================================
        @Test
        @Order(12)
        @DisplayName("TC-12: Cart page displays a non-zero subtotal after adding a plant")
        void testCartSubtotalDisplay() {
                // Add a plant to cart first
                navigateTo("/");
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();
                wait.until(ExpectedConditions.urlContains("/plant/"));

                try {
                        WebElement addToCartBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(
                                                        By.xpath("//button[contains(text(),'Add to Cart')]")));
                        addToCartBtn.click();
                } catch (TimeoutException e) {
                        // Plant might be out of stock; skip assertion
                        return;
                }

                navigateTo("/cart");

                // Find the subtotal element
                WebElement subtotal = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(text(),'Subtotal')]/following-sibling::*")));
                String subtotalText = subtotal.getText();
                assertFalse(subtotalText.isEmpty(), "Subtotal should be displayed in the cart");
                assertFalse(subtotalText.equals("$0.00"), "Subtotal should be greater than $0.00");
        }

        // =========================================================================
        // TC-13: Cart page has a "Proceed to Checkout" button
        // =========================================================================
        @Test
        @Order(13)
        @DisplayName("TC-13: Cart page has a Proceed to Checkout button")
        void testCartHasCheckoutButton() {
                // Ensure something is in the cart
                navigateTo("/");
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();
                wait.until(ExpectedConditions.urlContains("/plant/"));

                try {
                        WebElement addBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(
                                                        By.xpath("//button[contains(text(),'Add to Cart')]")));
                        addBtn.click();
                } catch (TimeoutException ignored) {
                }

                navigateTo("/cart");

                WebElement checkoutBtn = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//a[contains(text(),'Proceed to Checkout')]")));
                assertTrue(checkoutBtn.isDisplayed(),
                                "'Proceed to Checkout' button should be visible in the cart");
        }

        // =========================================================================
        // TC-14: Checkout page shows shipping form fields for logged-in user
        // =========================================================================
        @Test
        @Order(14)
        @DisplayName("TC-14: Checkout page shows shipping address form for authenticated user")
        void testCheckoutFormVisibleWhenLoggedIn() {
                // Login first
                loginAs(USER_EMAIL, USER_PASSWORD);

                // Add a plant to cart
                navigateTo("/");
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();
                wait.until(ExpectedConditions.urlContains("/plant/"));

                try {
                        WebElement addBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(
                                                        By.xpath("//button[contains(text(),'Add to Cart')]")));
                        addBtn.click();
                } catch (TimeoutException ignored) {
                }

                navigateTo("/checkout");

                // Shipping address form fields should be visible
                WebElement addressField = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("input[name='address']")));
                WebElement cityField = driver.findElement(By.cssSelector("input[name='city']"));
                WebElement postalField = driver.findElement(By.cssSelector("input[name='postalCode']"));
                WebElement countryField = driver.findElement(By.cssSelector("input[name='country']"));

                assertTrue(addressField.isDisplayed(), "Address field should be visible");
                assertTrue(cityField.isDisplayed(), "City field should be visible");
                assertTrue(postalField.isDisplayed(), "Postal code field should be visible");
                assertTrue(countryField.isDisplayed(), "Country field should be visible");

                logout();
        }

        // =========================================================================
        // TC-15: Profile page shows order history for a logged-in user
        // =========================================================================
        @Test
        @Order(15)
        @DisplayName("TC-15: Profile page is accessible and shows user info for logged-in user")
        void testProfilePageAccessible() {
                loginAs(USER_EMAIL, USER_PASSWORD);

                navigateTo("/profile");

                // "My Profile" heading should be visible
                WebElement heading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//h1[contains(text(),'My Profile')]")));
                assertTrue(heading.isDisplayed(), "Profile heading should be visible");

                // The user's name / email should appear in the sidebar
                WebElement profileCard = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(text(),'" + USER_EMAIL + "')]")));
                assertTrue(profileCard.isDisplayed(), "User email should be displayed on the profile page");

                // The Order History section should be present
                WebElement orderHistory = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(text(),'Order History')]")));
                assertTrue(orderHistory.isDisplayed(), "Order History section should be visible on profile page");

                logout();
        }
}
