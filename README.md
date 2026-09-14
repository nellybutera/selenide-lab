# Selenide Lab — Swag Labs UI Automation

[![CI](https://github.com/nellybutera/selenide-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/nellybutera/selenide-lab/actions/workflows/ci.yml)
[![Nightly Regression](https://github.com/nellybutera/selenide-lab/actions/workflows/nightly.yml/badge.svg)](https://github.com/nellybutera/selenide-lab/actions/workflows/nightly.yml)

QA Automation lab — Selenide-based UI test suite for the [Swag Labs demo app](https://www.saucedemo.com/), covering login, product browsing, cart, and checkout.

## Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Maven | 3.9+ | Build & dependency management |
| Selenide | 7.9 | Selenium wrapper — fluent API, built-in waits, screenshots |
| JUnit 5 | 5.12 | Test runner |
| Allure (allure-selenide, allure-junit5) | 2.29 | Test reporting |
| Browsers | Chrome + Firefox (headless) | Both run in the CI matrix on every push; the Docker image bundles Chrome |
| Docker | — | Reproducible environment setup — headless Chrome + the exact dependency versions, run both locally and as its own CI job |
| GitHub Actions | — | CI/CD pipeline: a browser matrix, a Docker job, a nightly canary, and Slack alerts on failure |

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
    ├── data/
    │   ├── Users.java           # seeded Swag Labs accounts
    │   ├── Products.java        # the six catalog item names
    │   └── Buyer.java           # default checkout buyer info
    └── resources/testdata/
        └── invalid_logins.csv   # negative-login cases (username, password, expected error)
```

Test data lives in `data/` (constants) and `resources/testdata/` (CSV) — never inline in a test method — so a reviewer can audit what's tested without reading test logic, and vice versa.

## Test Coverage

28 tests across four suites:

| Test Class | Tag | Scenarios |
|---|---|---|
| `LoginTests` (6) | `smoke` | standard login; invalid credentials rejected (locked-out user, missing password, unknown username — one `@ParameterizedTest` driven by `testdata/invalid_logins.csv`); logout then log back in; direct URL navigation to the inventory page without logging in is rejected |
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

The image bundles headless Chrome and the exact dependency versions the suite needs — a self-contained environment setup, so "works on my machine" isn't a question (Firefox is exercised separately in the CI matrix, see below):

```bash
docker build -t selenide-lab .
docker run selenide-lab
```

This is the same image and command CI's `docker` job runs on every push — see below.

## CI Architecture

Two workflows, answering two different questions:

| | `ci.yml` | `nightly.yml` |
|---|---|---|
| Trigger | a code change (push/PR to `main`), or on demand | the clock — `0 3 * * *` (03:00 UTC daily), or on demand |
| Answers | "did my change break the suite?" | "did the live site break the suite?" |
| Who's watching | whoever just pushed / opened the PR | nobody — hence Slack matters most here |

**`ci.yml`** runs three jobs on every push/PR:
- **`test`** — a `chrome`/`firefox` matrix job, installing each browser directly on the runner via `browser-actions/setup-*`. This proves the suite is cross-browser correct.
- **`docker`** — builds the actual `Dockerfile` and runs the suite inside that container: environment setup as code, not a manual runner install. This is a *different* guarantee from the matrix job — it proves the Dockerfile/image itself still works, not just that Maven+a browser works on a GitHub-hosted runner. It's genuinely caught real bugs the matrix job didn't (see commit history — two race conditions in page objects were found by this job within an hour of it existing).
- **`notify-on-failure`** — runs once, after both jobs above, only if something failed (`needs: [test, docker]` + `if: ...contains(needs.*.result, 'failure')`). Posts to Slack. Runs once regardless of how many matrix combinations failed, so a bad push doesn't spam the channel with duplicate pings.

The Chrome run of the `test` job also generates the Allure report and publishes it to GitHub Pages — **[view it live](https://nellybutera.github.io/selenide-lab/)**.

**`nightly.yml`** runs the full suite once a day, independent of any code change, against Chrome only. Its own failure step posts to Slack directly (no separate notify job needed — it's a single job, so there's no duplicate-ping risk to guard against).

### Slack notifications

Both workflows post to Slack **only on failure**, via an [Incoming Webhook](https://api.slack.com/messaging/webhooks) stored as the `SLACK_WEBHOOK_URL` repository secret. Each message is a Slack Block Kit card, not a one-line ping:

- **`ci.yml`** — repository, branch, who/what triggered it (push, PR, or manual dispatch), a link to the exact commit, the browser-matrix result and the Docker job result *individually* (so you know at a glance whether it's a real regression or just one environment), and a **View run** button.
- **`nightly.yml`** — the same repository/commit/button info, plus a note on *why it matters*: this run wasn't triggered by a push, so a failure here means either saucedemo.com itself changed (the whole point of this canary) or the suite regressed silently since the last commit — and nobody is watching in real time to notice.

If the secret isn't set, the step logs `SLACK_WEBHOOK_URL secret not set - skipping Slack notification` and exits cleanly — the workflow doesn't turn red just because Slack isn't configured.

To enable it: create an Incoming Webhook for a Slack channel, then set the secret yourself (this prompts for the value and never echoes or logs it):
```bash
gh secret set SLACK_WEBHOOK_URL
```

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

Other test data, kept out of the test classes themselves:

- `data/Products.java` — the six catalog item names, referenced by `ProductTests`, `CartTests`, and `CheckoutTests` instead of repeating string literals
- `data/Buyer.java` — the default checkout name/postal code used by the happy-path checkout tests
- `resources/testdata/invalid_logins.csv` — the three negative-login rows (username, password, expected error) that drive `LoginTests#invalidLoginIsRejected` via `@CsvFileSource`
