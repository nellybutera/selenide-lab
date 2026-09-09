package com.swaglabs.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * https://www.saucedemo.com/ - login page.
 */
public class LoginPage {

    private final SelenideElement usernameInput = $("#user-name");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton = $("#login-button");
    private final SelenideElement errorMessage = $("[data-test='error']");

    /** Navigates to the login page (the application's base URL). */
    public LoginPage navigateTo() {
        open("/");
        return this;
    }

    /** Logs in with valid credentials and expects to land on the products page. */
    public ProductsPage loginAs(String username, String password) {
        attemptLogin(username, password);
        return new ProductsPage();
    }

    /** Submits the login form without asserting on the outcome, for negative test cases. */
    public LoginPage attemptLogin(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        loginButton.click();
        return this;
    }

    public String getErrorMessage() {
        return errorMessage.shouldBe(Condition.visible).getText();
    }
}
