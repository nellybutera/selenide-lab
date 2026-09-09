package com.swaglabs.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class CartPage {

    private final ElementsCollection cartItems = $$(".cart_item");
    private final SelenideElement checkoutButton = $("#checkout");
    private final SelenideElement continueShoppingButton = $("#continue-shopping");

    public int getItemCount() {
        return cartItems.size();
    }

    public CartPage removeItem(String productName) {
        cartItems.findBy(Condition.text(productName)).$(".btn_secondary").click();
        return this;
    }

    public ProductsPage continueShopping() {
        continueShoppingButton.click();
        return new ProductsPage();
    }

    public CheckoutStepOnePage checkout() {
        checkoutButton.click();
        return new CheckoutStepOnePage();
    }
}
