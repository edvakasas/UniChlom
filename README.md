# Car Service Estimate Calculator

A web application for small car repair workshops that calculates a repair estimate (labour, parts, discounts and VAT) from the workshop's own price list, so the customer knows the price before any work starts.

Course project for **Software Systems Design** at VILNIUS TECH. The idea comes from a real family-run workshop that still fills in estimates by hand on paper.

## Status

- **Stage I, design document:** done ([docs/I-etapas-projektavimo-dokumentas.md](docs/I-etapas-projektavimo-dokumentas.md), in Lithuanian)
- **Stage II, working prototype:** in progress. The design and planned stack below describe what is being built; code will be added here as it is written.

## What it will do

- Build an estimate from the selected jobs, parts, vehicle class and customer type
- Avoid charging twice for shared operations (for example, removing the front wheels for both brake pads and discs)
- Apply one discount per labour line, the best of the job promotion and the regular-customer discount, never stacked
- Add the parts markup, calculate 21% VAT and round money to the cent
- Flag estimates that go over the customer's price limit
- Keep each workshop's price list and estimates separate from other workshops

## Planned design

- The calculation module (`EstimateCalculator`) is plain Java with no Spring or database dependencies, so every rule can be unit tested on its own
- Discounts follow the strategy pattern (`DiscountRule`), so a new discount can be added without changing existing code
- All money is handled with `BigDecimal` (HALF_UP to the cent), never `double`
- Every repository query is filtered by workshop ID

## Planned tech stack

Java 21, Spring Boot 3 (Web, Data JPA), H2, JUnit 5, Maven

## Example (from the design document)

Passenger car, not a regular customer. Jobs: front brake pads (D01) and oil and filter change (D03). Parts: brake pads €30.00, 4 l of oil at €8.00, oil filter €6.50.

| | Amount |
|---|---|
| Labour | €60.00 |
| Parts (with 20% markup) | €82.20 |
| Subtotal | €142.20 |
| VAT 21% | €29.86 |
| **Total** | **€172.06** |

## Planned API

- `POST /api/estimates` calculate and save an estimate
- `GET /api/estimates/{id}` view a saved estimate
- `GET /api/works` list the workshop's jobs

---

**LT:** Autoserviso sąmatų skaičiuoklė. Programų sistemų projektavimo kursinis darbas: sistema mažiems autoservisams, apskaičiuojanti remonto sąmatą pagal serviso kainoraštį, nuolaidų taisykles ir PVM.
