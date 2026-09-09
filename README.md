# Selenide Lab — Swag Labs UI Automation

QA Automation lab — Selenide-based UI test suite for the [Swag Labs demo app](https://www.saucedemo.com/), covering login, product browsing, cart, and checkout.

## Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Maven | 3.9+ | Build & dependency management |
| Selenide | 7.9 | Selenium wrapper — fluent API, built-in waits, screenshots |
| JUnit 5 | 5.12 | Test runner |
| Allure (allure-selenide, allure-junit5) | 2.29 | Test reporting |
| Docker | — | Containerised execution (headless Chrome) |
| GitHub Actions | — | CI/CD pipeline |

## Project Structure

```
src/
├── main/java/com/swaglabs/pages/         # Page Object Model classes
│   ├── LoginPage.java
│   ├── ProductsPage.java
│   ├── ProductDetailsPage.java
│   ├── CartPage.java
│   ├── CheckoutStepOnePage.java
│   ├── CheckoutStepTwoPage.java
│   └── CheckoutCompletePage.java
└── test/java/com/swaglabs/
    ├── tests/
    │   ├── BaseTest.java        # Selenide config, headless/CI switch, Allure listener, screenshot-on-failure
    │   ├── LoginTests.java      # smoke — authentication
    │   ├── ProductTests.java    # smoke — browsing, sorting, product details
    │   ├── CartTests.java       # regression — cart management
    │   └── CheckoutTests.java   # regression — full checkout flow
    └── data/Users.java          # seeded Swag Labs accounts
```

## Test Coverage

28 tests across four suites:

| Test Class | Tag | Scenarios |
|---|---|---|
| `LoginTests` (6) | `smoke` | standard login; invalid credentials rejected (locked-out user, missing password, unknown username — one `@ParameterizedTest`); logout then log back in; direct URL navigation to the inventory page without logging in is rejected |
| `ProductTests` (7) | `smoke` | inventory count; sort ascending and descending by price; open product details; add-to-cart badge (from the list and from the details page); adding all six items updates the badge to "6" |
| `CartTests` (6) | `regression` | items added appear in cart; remove item; continue shopping; cart survives a product-details round trip; badge disappears (not "0") when the cart empties; "Reset App State" clears the cart |
| `CheckoutTests` (9) | `regression` | full checkout completes; checkout with an **empty cart** completes; missing first/last name/postal code are each rejected; Cancel at each of the two checkout steps and Back Home from the confirmation page all return to the correct page; overview subtotal + tax = total (numeric check, soft-asserted with `assertAll` alongside the field-presence checks) |

Every assertion uses Selenide's built-in explicit waits (`shouldBe`, `shouldHave`), so there are no manual `Thread.sleep`s. `checkoutOverviewTotalsAreConsistent` is the suite's soft-assertion example — it reports every failing field via `Assertions.assertAll(...)` instead of aborting at the first one.

A failed test is automatically re-run once (`rerunFailingTestsCount=1` in `pom.xml`) before being reported as a real failure — a safety net for transient flakiness against the live external site, not a substitute for fixing root causes (see `pageLoadStrategy=eager` in `BaseTest`, added after a real page-load hang was observed).

## Run Locally

**Prerequisites:** Java 17, Maven, Google Chrome (or Firefox)

```bash
mvn test
```

Run a specific browser or headless mode:

```bash
mvn test -Dselenide.browser=firefox -Dselenide.headless=true
```

Run only one suite by tag:

```bash
mvn test -Dgroups=smoke
mvn test -Dgroups=regression
```

Generate and open the Allure report after a run:

```bash
mvn allure:report
mvn allure:serve
```

Screenshots and page source for any failing test are saved automatically under `target/screenshots/` (also attached to the Allure report).

## Run with Docker

The image bundles headless Chrome (Firefox is exercised separately in the CI matrix, see below):

```bash
docker build -t selenide-lab .
docker run selenide-lab
```

## CI / Allure Report

Every push to `main` triggers the GitHub Actions pipeline, which runs the full suite headlessly against both Chrome and Firefox. Failure screenshots are uploaded as workflow artifacts, and the Allure report (from the Chrome run) is published to GitHub Pages:

**[View Allure Report](https://nellybutera.github.io/selenide-lab/)**

## Test Data

Seeded Swag Labs accounts, defined in `src/test/java/com/swaglabs/data/Users.java`:

| Username | Password | Behavior |
|---|---|---|
| `standard_user` | `secret_sauce` | happy path |
| `locked_out_user` | `secret_sauce` | login blocked |
| `problem_user` | `secret_sauce` | UI/image glitches |
| `performance_glitch_user` | `secret_sauce` | slow responses |
| `error_user` | `secret_sauce` | scripted checkout errors |
| `visual_user` | `secret_sauce` | visual regressions |
