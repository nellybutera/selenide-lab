package com.swaglabs.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class CheckoutCompletePage {

    private final SelenideElement completeHeader = $(".complete-header");
    private final SelenideElement backHomeButton = $("#back-to-products");

    public String getCompleteHeaderText() {
        return completeHeader.getText();
    }

    public ProductsPage backToProducts() {
        backHomeButton.click();
        return new ProductsPage();
    }
}
