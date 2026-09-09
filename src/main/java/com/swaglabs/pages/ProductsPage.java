package com.swaglabs.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Inventory page shown after a successful login.
 */
public class ProductsPage {

    private final SelenideElement pageTitle = $(".title");
    private final SelenideElement cartBadge = $(".shopping_cart_badge");
    private final SelenideElement cartLink = $(".shopping_cart_link");
    private final SelenideElement sortDropdown = $(".product_sort_container");
    private final SelenideElement menuButton = $("#react-burger-menu-btn");
    private final SelenideElement closeMenuButton = $("#react-burger-cross-btn");
    private final SelenideElement logoutLink = $("#logout_sidebar_link");
    private final SelenideElement resetAppStateLink = $("#reset_sidebar_link");
    private final ElementsCollection inventoryItems = $$(".inventory_item");

    public SelenideElement title() {
        return pageTitle;
    }

    public SelenideElement cartBadge() {
        return cartBadge;
    }

    public int getProductCount() {
        return inventoryItems.size();
    }

    public List<String> getAllProductNames() {
        return inventoryItems.asFixedIterable().stream()
                .map(item -> item.$(".inventory_item_name").getText())
                .collect(Collectors.toList());
    }

    private SelenideElement itemByName(String productName) {
        return inventoryItems.findBy(Condition.text(productName));
    }

    /**
     * Clicks "Add to cart" on the given inventory tile. The button toggles to "Remove"
     * once clicked, so this asserts it is still in the "Add to cart" state first -
     * calling it twice on the same product would otherwise silently remove the item.
     */
    public ProductsPage addProductToCart(String productName) {
        SelenideElement addToCartButton = itemByName(productName).$(".btn_inventory");
        addToCartButton.shouldHave(Condition.exactText("Add to cart"));
        addToCartButton.click();
        return this;
    }

    public ProductDetailsPage openProduct(String productName) {
        itemByName(productName).$(".inventory_item_name").click();
        return new ProductDetailsPage();
    }

    /** @param visibleOptionText e.g. "Name (A to Z)", "Price (low to high)" */
    public ProductsPage sortBy(String visibleOptionText) {
        sortDropdown.selectOption(visibleOptionText);
        return this;
    }

    public CartPage goToCart() {
        cartLink.click();
        return new CartPage();
    }

    public LoginPage logout() {
        menuButton.click();
        logoutLink.click();
        return new LoginPage();
    }

    /** Clears the cart and any other seeded state via the hamburger menu's "Reset App State". */
    public ProductsPage resetAppState() {
        menuButton.click();
        resetAppStateLink.shouldBe(Condition.visible).click();
        closeMenuButton.click();
        return this;
    }
}
