package com.swaglabs.tests;

import com.swaglabs.data.Users;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.CheckoutCompletePage;
import com.swaglabs.pages.CheckoutStepOnePage;
import com.swaglabs.pages.CheckoutStepTwoPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regression suite: the full checkout flow, from cart to order confirmation. */
@Epic("Swag Labs")
@Feature("Checkout")
@Tag("regression")
class CheckoutTests extends BaseTest {

    private CartPage cartPage;

    @BeforeEach
    void setUp() {
        ProductsPage productsPage = new LoginPage().navigateTo().loginAs(Users.STANDARD_USER, Users.PASSWORD);
        productsPage.addProductToCart("Sauce Labs Backpack");
        productsPage.addProductToCart("Sauce Labs Bike Light");
        cartPage = productsPage.goToCart();
    }

    @Test
    @Description("A full checkout with valid buyer info completes and shows the confirmation page")
    void canCompleteCheckoutWithValidInfo() {
        CheckoutCompletePage completePage = cartPage.checkout()
                .fillInfo("Nelly", "Butera", "00000")
                .continueCheckout()
                .finish();

        assertEquals("Thank you for your order!", completePage.getCompleteHeaderText());
    }

    @Test
    @Description("Checkout can be completed even when the cart is empty")
    void canCheckoutWithEmptyCart() {
        cartPage.removeItem("Sauce Labs Backpack");
        cartPage.removeItem("Sauce Labs Bike Light");
        assertEquals(0, cartPage.getItemCount());

        CheckoutCompletePage completePage = cartPage.checkout()
                .fillInfo("Nelly", "Butera", "00000")
                .continueCheckout()
                .finish();

        assertEquals("Thank you for your order!", completePage.getCompleteHeaderText());
    }

    @Test
    @Description("Checkout step one rejects submission when the first name is missing")
    void checkoutRequiresFirstName() {
        CheckoutStepOnePage stepOne = cartPage.checkout().fillInfo("", "Butera", "00000");

        stepOne.continueCheckout();

        assertTrue(stepOne.getErrorMessage().contains("First Name is required"));
    }

    @Test
    @Description("Checkout step one rejects submission when the last name is missing")
    void checkoutRequiresLastName() {
        CheckoutStepOnePage stepOne = cartPage.checkout().fillInfo("Nelly", "", "00000");

        stepOne.continueCheckout();

        assertTrue(stepOne.getErrorMessage().contains("Last Name is required"));
    }

    @Test
    @Description("Checkout step one rejects submission when the postal code is missing")
    void checkoutRequiresPostalCode() {
        CheckoutStepOnePage stepOne = cartPage.checkout().fillInfo("Nelly", "Butera", "");

        stepOne.continueCheckout();

        assertTrue(stepOne.getErrorMessage().contains("Postal Code is required"));
    }

    @Test
    @Description("Cancel on checkout step one returns to the cart, keeping its items")
    void cancelOnStepOneReturnsToCart() {
        CartPage backToCart = cartPage.checkout().cancel();

        assertEquals(2, backToCart.getItemCount());
    }

    @Test
    @Description("Cancel on the checkout overview (step two) returns to the full inventory")
    void cancelOnStepTwoReturnsToProducts() {
        ProductsPage backToProducts = cartPage.checkout()
                .fillInfo("Nelly", "Butera", "00000")
                .continueCheckout()
                .cancel();

        assertEquals(6, backToProducts.getProductCount());
    }

    @Test
    @Description("Back Home from the order confirmation page returns to the full inventory")
    void backHomeFromConfirmationReturnsToProducts() {
        ProductsPage backToProducts = cartPage.checkout()
                .fillInfo("Nelly", "Butera", "00000")
                .continueCheckout()
                .finish()
                .backToProducts();

        assertEquals(6, backToProducts.getProductCount());
    }

    @Test
    @Description("The order overview's subtotal, tax and total are all present and numerically consistent")
    void checkoutOverviewTotalsAreConsistent() {
        CheckoutStepTwoPage overview = cartPage.checkout()
                .fillInfo("Nelly", "Butera", "00000")
                .continueCheckout();

        // A soft-assertion group: every field is checked and reported, rather than
        // aborting at the first failing label.
        assertAll("checkout overview totals",
                () -> assertTrue(overview.getSubtotal().startsWith("Item total:"), "subtotal label"),
                () -> assertTrue(overview.getTax().startsWith("Tax:"), "tax label"),
                () -> assertTrue(overview.getTotal().startsWith("Total:"), "total label"),
                () -> assertEquals(
                        parseAmount(overview.getSubtotal()) + parseAmount(overview.getTax()),
                        parseAmount(overview.getTotal()),
                        0.01,
                        "subtotal + tax should equal the displayed total")
        );
    }

    /** Strips a label like "Item total: $29.99" down to the numeric amount. */
    private static double parseAmount(String labelledAmount) {
        return Double.parseDouble(labelledAmount.replaceAll("[^0-9.]", ""));
    }
}
