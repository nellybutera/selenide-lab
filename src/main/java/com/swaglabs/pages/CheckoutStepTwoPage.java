package com.swaglabs.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

/** "Checkout: Overview" step - order summary before payment. */
public class CheckoutStepTwoPage {

    private final SelenideElement subtotalLabel = $(".summary_subtotal_label");
    private final SelenideElement taxLabel = $(".summary_tax_label");
    private final SelenideElement totalLabel = $(".summary_total_label");
    private final SelenideElement finishButton = $("#finish");
    private final SelenideElement cancelButton = $("#cancel");

    public String getSubtotal() {
        return subtotalLabel.getText();
    }

    public String getTax() {
        return taxLabel.getText();
    }

    public String getTotal() {
        return totalLabel.getText();
    }

    public CheckoutCompletePage finish() {
        finishButton.click();
        return new CheckoutCompletePage();
    }

    public ProductsPage cancel() {
        cancelButton.click();
        return new ProductsPage();
    }
}
