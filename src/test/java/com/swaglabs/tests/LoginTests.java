package com.swaglabs.tests;

import com.swaglabs.data.Users;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Smoke suite: login is the entry point for every other flow in the app. */
@Epic("Swag Labs")
@Feature("Login")
@Tag("smoke")
class LoginTests extends BaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage().navigateTo();
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Description("A standard user can log in and lands on the products page")
    void standardUserCanLogIn() {
        var productsPage = loginPage.loginAs(Users.STANDARD_USER, Users.PASSWORD);

        productsPage.title().shouldBe(visible);
        assertTrue(productsPage.getProductCount() > 0, "Products page should list items after login");
    }

    @ParameterizedTest(name = "{0}/{1} -> \"{2}\"")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Invalid credentials are rejected with the exact Swag Labs error message")
    @CsvSource({
            "locked_out_user, secret_sauce, 'Epic sadface: Sorry, this user has been locked out.'",
            "standard_user,  '',            'Epic sadface: Password is required'",
            "no_such_user,   secret_sauce,  'Epic sadface: Username and password do not match any user in this service'"
    })
    void invalidLoginIsRejected(String username, String password, String expectedError) {
        loginPage.attemptLogin(username, password);

        assertEquals(expectedError, loginPage.getErrorMessage());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Description("A user can log out and log back in with the same credentials")
    void canLogOutAndLogBackIn() {
        ProductsPage productsPage = loginPage.loginAs(Users.STANDARD_USER, Users.PASSWORD);

        LoginPage loggedOutPage = productsPage.logout();
        ProductsPage productsPageAgain = loggedOutPage.loginAs(Users.STANDARD_USER, Users.PASSWORD);

        productsPageAgain.title().shouldBe(visible);
        assertEquals(6, productsPageAgain.getProductCount());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Description("Navigating directly to the inventory page without logging in is rejected")
    void cannotAccessInventoryPageDirectlyWithoutLogin() {
        open("/inventory.html");

        assertEquals("Epic sadface: You can only access '/inventory.html' when you are logged in.",
                loginPage.getErrorMessage());
    }
}
