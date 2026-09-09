package com.swaglabs.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

/** "Checkout: Your Information" step. */
public class CheckoutStepOnePage {

    private final SelenideElement firstNameInput = $("#first-name");
    private final SelenideElement lastNameInput = $("#last-name");
    private final SelenideElement postalCodeInput = $("#postal-code");
    private final SelenideElement continueButton = $("#continue");
    private final SelenideElement cancelButton = $("#cancel");
    private final SelenideElement errorMessage = $("[data-test='error']");

    public CheckoutStepOnePage fillInfo(String firstName, String lastName, String postalCode) {
        firstNameInput.setValue(firstName);
        lastNameInput.setValue(lastName);
        postalCodeInput.setValue(postalCode);
        return this;
    }

    public CheckoutStepTwoPage continueCheckout() {
        continueButton.click();
        return new CheckoutStepTwoPage();
    }

    public String getErrorMessage() {
        return errorMessage.shouldBe(Condition.visible).getText();
    }

    public CartPage cancel() {
        cancelButton.click();
        return new CartPage();
    }
}
