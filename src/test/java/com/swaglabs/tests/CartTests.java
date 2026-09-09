package com.swaglabs.tests;

import com.swaglabs.data.Users;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
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
    @Description("Items added on the products page appear in the cart")
    void addedItemsAppearInCart() {
        productsPage.addProductToCart("Sauce Labs Backpack");
        productsPage.addProductToCart("Sauce Labs Bike Light");

        CartPage cartPage = productsPage.goToCart();
        assertEquals(2, cartPage.getItemCount());
    }

    @Test
    @Description("Removing an item from the cart page decreases the item count")
    void canRemoveItemFromCart() {
        productsPage.addProductToCart("Sauce Labs Backpack");
        productsPage.addProductToCart("Sauce Labs Bike Light");
        CartPage cartPage = productsPage.goToCart();

        cartPage.removeItem("Sauce Labs Backpack");

        assertEquals(1, cartPage.getItemCount());
    }

    @Test
    @Description("Continue Shopping returns from the cart to the full inventory page")
    void continueShoppingReturnsToProducts() {
        productsPage.addProductToCart("Sauce Labs Backpack");
        CartPage cartPage = productsPage.goToCart();

        ProductsPage backToProducts = cartPage.continueShopping();

        assertEquals(6, backToProducts.getProductCount());
    }

    @Test
    @Description("An item added to the cart survives a round trip through its product details page")
    void cartSurvivesProductDetailsRoundTrip() {
        productsPage.addProductToCart("Sauce Labs Backpack");

        var detailsPage = productsPage.openProduct("Sauce Labs Bike Light");
        var backOnProductsPage = detailsPage.backToProducts();

        backOnProductsPage.cartBadge().shouldHave(text("1"));
    }

    @Test
    @Description("The cart badge disappears entirely (not just showing zero) once the cart is emptied")
    void cartBadgeDisappearsWhenCartBecomesEmpty() {
        productsPage.addProductToCart("Sauce Labs Backpack");
        CartPage cartPage = productsPage.goToCart();

        cartPage.removeItem("Sauce Labs Backpack");

        productsPage.cartBadge().shouldNot(exist);
    }

    @Test
    @Description("Reset App State (hamburger menu) clears any items previously added to the cart")
    void resetAppStateClearsCart() {
        productsPage.addProductToCart("Sauce Labs Backpack");
        productsPage.cartBadge().shouldHave(text("1"));

        productsPage.resetAppState();

        productsPage.cartBadge().shouldNot(exist);
    }
}
