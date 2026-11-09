# Car Service Estimate Calculator

A web application for small car repair workshops that calculates a repair estimate (labour, parts, discounts and VAT) from the workshop's own price list, so the customer knows the price before any work starts.

Course project for **Software Systems Design** at VILNIUS TECH. The idea comes from a real family-run workshop that still fills in estimates by hand on paper.

## Status

- **Stage I, design document:** done and approved ([docs/I-etapas-projektavimo-dokumentas.md](docs/I-etapas-projektavimo-dokumentas.md), in Lithuanian)
- **Stage II, working prototype:** this code. REST API with the core module, two workshops (tenants) with sample data, automated tests.

## Requirements

| Tool | Version |
|---|---|
| Java (JDK) | 21 or newer (tested with 21 and 25) |
| Maven | 3.9 or newer |

Nothing else is needed: the database (H2) runs in memory inside the app. No API keys.

Check: `java -version` and `mvn -v`.

## Run

```
mvn spring-boot:run
```

The app starts on <http://localhost:8080>. Every start creates a fresh database and loads the sample data from [`src/main/resources/data.sql`](src/main/resources/data.sql).

Configuration ([`application.properties`](src/main/resources/application.properties)): the only setting you may want to change is the port, via the `PORT` environment variable (default 8080).

## Test

```
mvn test
```

| Test | What it checks |
|---|---|
| `EstimateCalculatorTest.normalCase_...` | Normal case: D01 + D03 with parts → total **€172.06**, status `PARENGTA` |
| `EstimateCalculatorTest.boundary_...` | Total exactly equal to the customer's limit (€172.06) → still `PARENGTA` |
| `EstimateCalculatorTest.conflict_...` | Shared operation charged once, largest discount wins (20 % not 25 %), limit exceeded → **€326.22**, `LAUKIA_PATVIRTINIMO`, over by €76.22 |
| `EstimateCalculatorTest.rounding_...` | Half a cent rounds up: VAT 4.305 → **€4.31** (the risk named in the design document) |
| `EstimateCalculatorTest.error_...` | Other workshop's job code and zero quantity → rejected with both messages, nothing calculated |
| `TenantIsolationTest` (3 tests) | Workshop `alfa` cannot read, list or approve workshop `beta`'s estimate, nor use `beta`'s price list |
| `EstimateApiTest` | The demo scenario through HTTP and the database, then approval (and a second approval is refused) |

Every push also runs the tests and a demo request on GitHub Actions (see the badge on the Actions tab).

## Sample data (two tenants)

| | Autoservisas Alfa (`alfa`) | Autoservisas Beta (`beta`) |
|---|---|---|
| Hourly rate | €40.00 | €45.00 |
| Parts markup | 20 % | 15 % |
| Regular-customer discount | 5 % | 10 % |
| Car class coefficients | 1.00 / 1.20 / 1.30 | 1.00 / 1.15 / 1.25 |
| Jobs | D01 front brake pads (1.0 h), D02 front brake discs (1.6 h), D03 oil change (0.5 h), D05 diagnostics (0.5 h, 20 % promotion) | B01–B05 (balancing, tyres with 10 % promotion, suspension check, rear pads and discs) |

D01 and D02 share the operation "remove front wheels" (0.4 h); B04 and B05 share "remove rear wheels". VAT is 21 % for both.

## Demo

The workshop is chosen with the `X-Tenant-Id` header (`alfa` or `beta`). In stage III it will come from the logged-in user.

**With the form:** open <http://localhost:8080>. It is a simple HTML page ([`static/index.html`](src/main/resources/static/index.html)) that only collects input and shows results; all calculation happens on the server.

1. Main scenario: workshop *Autoservisas Alfa*, class `VISUREIGIS`, tick *Nuolatinis klientas*, limit `250`, jobs D01, D02, D05, parts *Stabdžių kaladėlės* 30.00 × 1 and *Stabdžių diskai* 45.00 × 2 → **€326.22**, *LAUKIA_PATVIRTINIMO (riba viršyta 76.22 €)*.
2. Click *Klientas patvirtino* → *PATVIRTINTA*.
3. Error: switch the workshop to *Beta*, tick B03, set a part quantity to 0 → the errors are listed in red and nothing is saved. (Choosing Beta also shows that each workshop sees only its own jobs.)
4. Isolation: with *Beta* selected, enter the number of Alfa's estimate under *Išsaugota sąmata* and click *Peržiūrėti* → *Sąmata nerasta*. Switch back to *Alfa* → it shows.

**With Swagger UI:** open <http://localhost:8080/swagger-ui.html>, pick an endpoint, click *Try it out*, enter `alfa` as `X-Tenant-Id` and paste a file from [`demo/`](demo) as the body.

**Or with curl** (on Windows PowerShell write `curl.exe`, not `curl`), from the project folder:

1. Main scenario, over the customer's limit:
   ```
   curl -X POST localhost:8080/api/estimates -H "X-Tenant-Id: alfa" -H "Content-Type: application/json" -d @demo/1-virs-ribos.json
   ```
   Expected: D01 0.8 h → €30.40, D02 2.0 h → €76.00, D05 0.6 h → €19.20; parts €144.00; subtotal €269.60, VAT €56.62, **total €326.22**, status `LAUKIA_PATVIRTINIMO`, `overLimitBy` €76.22. Note the returned `id` (1 on a fresh start).
2. Customer approves: `curl -X POST localhost:8080/api/estimates/1/approve -H "X-Tenant-Id: alfa"` → status `PATVIRTINTA`. Running it again → 409, already approved.
3. Error case: `curl -X POST localhost:8080/api/estimates -H "X-Tenant-Id: alfa" -H "Content-Type: application/json" -d @demo/3-klaida.json` → 400 with *"Nežinomas darbo kodas: B03"* and *"Detalės „Stabdžių kaladėlės“ kiekis turi būti didesnis už 0"*; nothing is saved.
4. Tenant isolation: `curl localhost:8080/api/estimates/1 -H "X-Tenant-Id: beta"` → 404, Beta cannot see Alfa's estimate. `curl localhost:8080/api/estimates -H "X-Tenant-Id: beta"` → empty list.
5. Normal case: `demo/2-iprastas.json` → total €172.06, status `PARENGTA`.

## API

| Method and path | Does |
|---|---|
| `GET /api/works` | The workshop's price list |
| `POST /api/estimates` | Calculate and save an estimate (201), or 400 with every validation error |
| `GET /api/estimates` | The workshop's estimates, newest first |
| `GET /api/estimates/{id}` | One estimate (404 if it belongs to another workshop) |
| `POST /api/estimates/{id}/approve` | Customer approves an estimate that is over their limit (409 if not awaiting approval) |

## Code structure

```
src/main/java/lt/samata/
  domain/        core module, plain Java (no Spring, no database)
    EstimateCalculator         rules R1–R7: shared operations, billed hours, discounts, parts, VAT, price limit
    EstimateRequestValidator   rule R8: rejects bad input, reports every problem at once
    discount/                  DiscountRule interface (strategy pattern) + PromotionDiscount, RegularCustomerDiscount
    Money                      the one place money is rounded (to the cent, HALF_UP)
  service/       EstimateService: load price list -> calculate -> save, always for one workshop
  persistence/   JPA entities and repositories; every query is filtered by workshop id
  web/           REST controller and error-to-HTTP mapping
```

Design choices:

- The core module has no Spring or database code, so each rule is unit tested directly; Spring wires it in `EstimateApplication`.
- Each discount is its own class behind `DiscountRule`; the calculator only picks the largest. A new discount is a new class, not an edit to the calculator.
- Money is `BigDecimal`, never `double`.
- Repositories extend Spring's bare `Repository`, not `JpaRepository`, so there is no `findById` without a workshop id: isolation is enforced in code, not by hiding things in the UI. Another workshop's estimate returns the same 404 as a missing one.

## Differences from the Stage I plan

- **Database:** the plan said a file-based H2 database; the prototype uses in-memory H2, so every start gives the same clean sample data for the demo and the tests. Switching to a file or PostgreSQL is a change in `application.properties` only.
- **Interface:** the plan's simple HTML form is there (create, approve, view a saved estimate by number); Swagger UI was added as a second way to call the API.
- **Entity names:** the plan listed `Tenant`, `PriceList`, `WorkItem`, `Estimate`. In code the tenant is `Workshop`, and the price list is not a table of its own: `Workshop.toPriceList()` builds it from the workshop's rates and its `work_item` rows. The tenant column is `tenant_id`, as planned.
- **Seeded estimates:** quality check 4.2 in the design document mentions three saved estimates per workshop; the prototype seeds none and the isolation tests create their own. The full 4.2 check is planned for stage III.

## Tech stack

Java 21, Spring Boot 3.5 (Web, Data JPA), H2, JUnit 5, AssertJ, MockMvc, springdoc (Swagger UI), Maven, GitHub Actions

---

**LT:** Autoserviso sąmatų skaičiuoklė. Programų sistemų projektavimo kursinis darbas: sistema mažiems autoservisams, apskaičiuojanti remonto sąmatą pagal serviso kainoraštį, nuolaidų taisykles ir PVM. Paleidimas: `mvn spring-boot:run`, testai: `mvn test`.
