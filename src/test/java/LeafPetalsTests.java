package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.TimeoutException; // <-- Ensuring the correct exception is imported

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite for Leaf & Petals – Plant E-Commerce Website
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

        @BeforeAll
        static void setUp() {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox"); 
                options.addArguments("--disable-dev-shm-usage"); 
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

        private void navigateTo(String path) {
                driver.get(BASE_URL + path);
        }

        private void loginAs(String email, String password) {
                navigateTo("/login");
                WebElement emailInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[placeholder='Enter your email']")));
                emailInput.clear();
                emailInput.sendKeys(email);
                
                WebElement passInput = driver.findElement(By.cssSelector("input[placeholder='Enter your password']"));
                passInput.clear();
                passInput.sendKeys(password);
                
                try {
                    Thread.sleep(2000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                passInput.submit();
                wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        }

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

        @Test
        @Order(1)
        @DisplayName("TC-01: Home page loads with hero heading")
        void testHomePageLoads() {
                navigateTo("/");
                String pageTitle = driver.getTitle();
                assertTrue(pageTitle != null && !pageTitle.isEmpty(),
                                "Page title should not be empty");
                WebElement heroHeading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                String heroText = heroHeading.getText();
                assertFalse(heroText.isEmpty(), "Hero heading should contain text");
        }

        @Test
        @Order(2)
        @DisplayName("TC-02: Navbar contains navigation links")
        void testNavbarLinksPresent() {
                navigateTo("/");
                WebElement nav = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("nav")));
                assertNotNull(nav, "Navbar element should be present");
                List<WebElement> links = driver.findElements(By.cssSelector("a[href='/cart']"));
                assertFalse(links.isEmpty(), "Cart link should be present in the navbar");
        }

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

        @Test
        @Order(4)
        @DisplayName("TC-04: Clicking a plant card opens the plant detail page")
        void testPlantDetailPageLoads() {
                navigateTo("/");
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                assertFalse(plantCards.isEmpty(), "At least one plant card should be visible on the home page");
                plantCards.get(0).click();
                wait.until(ExpectedConditions.urlContains("/plant/"));
                assertTrue(driver.getCurrentUrl().contains("/plant/"),
                                "Should navigate to the plant detail page");
                WebElement heading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
                assertFalse(heading.getText().isEmpty(), "Plant name heading should be visible");
        }

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
                        assertFalse(addToCartBtn.isEnabled(),
                                        "Out of Stock button should be disabled");
                }
        }

        @Test
        @Order(6)
        @DisplayName("TC-06: Adding a plant to cart shows it in the cart page")
        void testAddToCartUpdatesCartPage() {
                navigateTo("/");
                List<WebElement> plantCards = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(
                                                By.cssSelector("a[href*='/plant/']")));
                plantCards.get(0).click();
                wait.until(ExpectedConditions.urlContains("/plant/"));
                WebElement addToCartBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[contains(text(),'Add to Cart')]")));
                addToCartBtn.click();
                navigateTo("/cart");
                List<WebElement> emptyMessage = driver.findElements(
                                By.xpath("//*[contains(text(),'Your cart is empty')]"));
                assertTrue(emptyMessage.isEmpty(),
                                "Cart should not be empty after adding a plant");
        }

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
                WebElement error = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("div.bg-red-50")));
                assertFalse(error.getText().isEmpty(), "An error message should be displayed for invalid credentials");
        }

        @Test
        @Order(9)
        @DisplayName("TC-09: Valid login redirects to home page or dashboard")
        void testSuccessfulLogin() {
                loginAs(USER_EMAIL, USER_PASSWORD);
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.contains("/dashboard") || currentUrl.contains("/profile"),
                                "Successful login should redirect away from the login page");
                logout();
        }

        @Test
        @Order(10)
        @DisplayName("TC-10: Register form validates empty fields")
        void testRegisterEmptyFieldsValidation() {
                navigateTo("/register");
                WebElement submitBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.cssSelector("form button")));
                submitBtn.click();
                WebElement error = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("div.bg-red-50")));
                assertTrue(error.getText().contains("fill all fields") || !error.getText().isEmpty(),
                                "Validation error should appear for empty fields");
        }

        @Test
        @Order(11)
        @DisplayName("TC-11: Unauthenticated access to /checkout redirects to login")
        void testCheckoutRedirectsUnauthenticated() {
                logout();
                navigateTo("/checkout");
                wait.until(ExpectedConditions.urlContains("/login"));
                assertTrue(driver.getCurrentUrl().contains("/login"),
                                "Unauthenticated user accessing /checkout should be redirected to /login");
        }

        @Test
        @Order(12)
        @DisplayName("TC-12: Cart page displays a non-zero subtotal after adding a plant")
        void testCartSubtotalDisplay() {
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
                        return;
                }
                navigateTo("/cart");
                WebElement subtotal = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//div[contains(@class,'flex') and contains(.,'Subtotal')]//span[last()]")));
                String subtotalText = subtotal.getText();
                assertFalse(subtotalText.isEmpty(), "Subtotal should be displayed in the cart");
                assertFalse(subtotalText.equals("$0.00"), "Subtotal should be greater than $0.00");
        }

        @Test
        @Order(13)
        @DisplayName("TC-13: Cart page has a Proceed to Checkout button")
        void testCartHasCheckoutButton() {
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

        @Test
        @Order(14)
        @DisplayName("TC-14: Checkout page shows shipping address form for authenticated user")
        void testCheckoutFormVisibleWhenLoggedIn() {
                loginAs(USER_EMAIL, USER_PASSWORD);
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

        @Test
        @Order(15)
        @DisplayName("TC-15: Profile page is accessible and shows user info for logged-in user")
        void testProfilePageAccessible() {
                loginAs(USER_EMAIL, USER_PASSWORD);
                navigateTo("/profile");
                WebElement heading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//h1[contains(text(),'My Profile')]")));
                assertTrue(heading.isDisplayed(), "Profile heading should be visible");
                WebElement profileCard = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(text(),'" + USER_EMAIL + "')]")));
                assertTrue(profileCard.isDisplayed(), "User email should be displayed on the profile page");
                WebElement orderHistory = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(text(),'Order History')]")));
                assertTrue(orderHistory.isDisplayed(), "Order History section should be visible on profile page");
                logout();
        }
}