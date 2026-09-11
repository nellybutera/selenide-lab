package com.swaglabs.tests;

import com.swaglabs.data.Products;
import com.swaglabs.data.Users;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Regression suite: cart management. */
@Epic("Swag Labs")
@Feature("Cart")
@Tag("regression")
class CartTests extends BaseTest {

    private ProductsPage productsPage;

    @BeforeEach
    void setUp() {
        productsPage = new LoginPage().navigateTo().loginAs(Users.STANDARD_USER, Users.PASSWORD);
    }

    @Test
    @DisplayName("Verify that items added on the products page appear in the cart")
    @Description("Items added on the products page appear in the cart")
    void addedItemsAppearInCart() {
        productsPage.addProductToCart(Products.BACKPACK);
        productsPage.addProductToCart(Products.BIKE_LIGHT);

        CartPage cartPage = productsPage.goToCart();
        assertEquals(2, cartPage.getItemCount());
    }

    @Test
    @DisplayName("Verify that removing an item from the cart page decreases the item count")
    @Description("Removing an item from the cart page decreases the item count")
    void canRemoveItemFromCart() {
        productsPage.addProductToCart(Products.BACKPACK);
        productsPage.addProductToCart(Products.BIKE_LIGHT);
        CartPage cartPage = productsPage.goToCart();

        cartPage.removeItem(Products.BACKPACK);

        assertEquals(1, cartPage.getItemCount());
    }

    @Test
    @DisplayName("Verify that Continue Shopping returns from the cart to the full inventory page")
    @Description("Continue Shopping returns from the cart to the full inventory page")
    void continueShoppingReturnsToProducts() {
        productsPage.addProductToCart(Products.BACKPACK);
        CartPage cartPage = productsPage.goToCart();

        ProductsPage backToProducts = cartPage.continueShopping();

        assertEquals(6, backToProducts.getProductCount());
    }

    @Test
    @DisplayName("Verify that an item added to the cart survives a round trip through its product details page")
    @Description("An item added to the cart survives a round trip through its product details page")
    void cartSurvivesProductDetailsRoundTrip() {
        productsPage.addProductToCart(Products.BACKPACK);

        var detailsPage = productsPage.openProduct(Products.BIKE_LIGHT);
        var backOnProductsPage = detailsPage.backToProducts();

        backOnProductsPage.cartBadge().shouldHave(text("1"));
    }

    @Test
    @DisplayName("Verify that the cart badge disappears entirely once the cart is emptied")
    @Description("The cart badge disappears entirely (not just showing zero) once the cart is emptied")
    void cartBadgeDisappearsWhenCartBecomesEmpty() {
        productsPage.addProductToCart(Products.BACKPACK);
        CartPage cartPage = productsPage.goToCart();

        cartPage.removeItem(Products.BACKPACK);

        productsPage.cartBadge().shouldNot(exist);
    }

    @Test
    @DisplayName("Verify that Reset App State clears any items previously added to the cart")
    @Description("Reset App State (hamburger menu) clears any items previously added to the cart")
    void resetAppStateClearsCart() {
        productsPage.addProductToCart(Products.BACKPACK);
        productsPage.cartBadge().shouldHave(text("1"));

        productsPage.resetAppState();

        productsPage.cartBadge().shouldNot(exist);
    }
}
