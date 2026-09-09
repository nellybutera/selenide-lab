package com.swaglabs.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

/**
 * Single product detail page, reached from {@link ProductsPage#openProduct(String)}.
 */
public class ProductDetailsPage {

    private final SelenideElement productName = $(".inventory_details_name");
    private final SelenideElement productPrice = $(".inventory_details_price");
    private final SelenideElement addToCartButton = $("#add-to-cart");
    private final SelenideElement removeButton = $("#remove");
    private final SelenideElement backButton = $("#back-to-products");

    public String getProductName() {
        return productName.getText();
    }

    public String getProductPrice() {
        return productPrice.getText();
    }

    public ProductDetailsPage addToCart() {
        addToCartButton.click();
        return this;
    }

    public boolean isInCart() {
        return removeButton.isDisplayed();
    }

    public ProductsPage backToProducts() {
        backButton.click();
        return new ProductsPage();
    }
}
