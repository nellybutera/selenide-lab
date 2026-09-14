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

    /**
     * ElementsCollection#size() reads the DOM immediately with no retry, unlike
     * shouldHave/shouldBe - calling it right after a navigation can catch the page
     * mid-transition and return 0. Waiting on a stable, always-present element first
     * forces Selenide's normal auto-wait to run before we count anything.
     */
    public int getItemCount() {
        checkoutButton.shouldBe(Condition.visible);
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
