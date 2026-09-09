package com.swaglabs.tests;

import com.swaglabs.data.Users;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Smoke suite: product discovery and detail pages. */
@Epic("Swag Labs")
@Feature("Products")
@Tag("smoke")
class ProductTests extends BaseTest {

    private ProductsPage productsPage;

    @BeforeEach
    void setUp() {
        productsPage = new LoginPage().navigateTo().loginAs(Users.STANDARD_USER, Users.PASSWORD);
    }

    @Test
    @Description("Inventory page lists all six seeded products")
    void productsPageListsSixItems() {
        assertEquals(6, productsPage.getProductCount());
    }

    @Test
    @Description("Sorting by price, low to high, puts the cheapest item first")
    void canSortProductsByPriceLowToHigh() {
        productsPage.sortBy("Price (low to high)");

        assertEquals("Sauce Labs Onesie", productsPage.getAllProductNames().get(0),
                "Cheapest seeded item ($7.99) should be first once sorted low to high");
    }

    @Test
    @Description("Opening a product from the inventory list shows its details page")
    void canOpenProductDetails() {
        var detailsPage = productsPage.openProduct("Sauce Labs Backpack");

        assertEquals("Sauce Labs Backpack", detailsPage.getProductName());
    }

    @Test
    @Description("Adding a product to the cart from the inventory list updates the cart badge")
    void addingProductUpdatesCartBadge() {
        productsPage.addProductToCart("Sauce Labs Backpack");

        productsPage.cartBadge().shouldHave(text("1"));
    }

    @Test
    @Description("Adding a product from its details page also updates the cart badge")
    void addingProductFromDetailsPageUpdatesCartBadge() {
        var detailsPage = productsPage.openProduct("Sauce Labs Bike Light");
        detailsPage.addToCart();

        assertTrue(detailsPage.isInCart());
        var backOnProductsPage = detailsPage.backToProducts();
        backOnProductsPage.cartBadge().shouldHave(text("1"));
    }

    @Test
    @Description("Sorting by price, high to low, puts the priciest item first")
    void canSortProductsByPriceHighToLow() {
        productsPage.sortBy("Price (high to low)");

        assertEquals("Sauce Labs Fleece Jacket", productsPage.getAllProductNames().get(0),
                "Priciest seeded item ($49.99) should be first once sorted high to low");
    }

    @Test
    @Description("Adding every seeded product updates the cart badge to match the full count")
    void addingAllProductsUpdatesBadgeToSix() {
        productsPage.getAllProductNames().forEach(productsPage::addProductToCart);

        productsPage.cartBadge().shouldHave(text("6"));
    }
}
