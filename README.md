# FoodShare - Surplus Food Donation Matching Platform

A Java 17 / Spring Boot application for donors to publish surplus food and NGOs to claim it. It includes a REST API, MySQL persistence, session-based sign-in, account registration for donors and NGOs, a dashboard, and a searchable browser for all domain records.

## Folder Structure

```text
foodshare/
|-- .github/
|   `-- copilot-instructions.md
|-- pom.xml
|-- README.md
`-- src/
    |-- main/
    |   |-- java/com/example/foodshare/
    |   |   |-- FoodshareApplication.java
    |   |   |-- config/SecurityConfig.java
    |   |   |-- controller/
    |   |   |   |-- AuthController.java
    |   |   |   |-- AuthPageController.java
    |   |   |   |-- ClaimController.java
    |   |   |   |-- DashboardController.java
    |   |   |   |-- DonorController.java
    |   |   |   |-- FoodListingController.java
    |   |   |   `-- NGOController.java
    |   |   |-- entity/
    |   |   |   |-- AccountRole.java
    |   |   |   |-- Claim.java
    |   |   |   |-- ClaimStatus.java
    |   |   |   |-- Donor.java
    |   |   |   |-- FoodListing.java
    |   |   |   |-- FoodStatus.java
    |   |   |   |-- FoodShareAccount.java
    |   |   |   `-- NGO.java
    |   |   |-- exception/
    |   |   |   |-- BusinessRuleException.java
    |   |   |   |-- GlobalExceptionHandler.java
    |   |   |   `-- ResourceNotFoundException.java
    |   |   |-- repository/
    |   |   |   |-- ClaimRepository.java
    |   |   |   |-- DonorRepository.java
    |   |   |   |-- FoodListingRepository.java
    |   |   |   |-- FoodShareAccountRepository.java
    |   |   |   `-- NGORepository.java
    |   |   `-- service/
    |   |       |-- AccountService.java
    |   |       |-- ClaimService.java
    |   |       |-- DonorService.java
    |   |       |-- FoodListingService.java
    |   |       |-- FoodShareUserDetailsService.java
    |   |       `-- NGOService.java
    |   `-- resources/
    |       |-- application.properties
    |       `-- static/
    |           |-- auth.css
    |           |-- auth.js
    |           |-- entities.css
    |           |-- entities.html
    |           |-- entities.js
    |           |-- app.js
    |           |-- index.html
    |           |-- signin.html
    |           |-- signup.html
    |           `-- styles.css
    `-- test/java/com/example/foodshare/service/
        |-- ClaimServiceTest.java
        `-- FoodListingServiceTest.java
```

## Create the MySQL Database

1. Open MySQL Workbench and connect to the local MySQL server on port `3306` as `root`.
2. Open a SQL tab and run:

   ```sql
   CREATE DATABASE foodshare;
   USE foodshare;
   ```

3. Set the `DB_PASSWORD` environment variable to the password for your local `root` account. This keeps the password out of project files and Git history.
4. `spring.jpa.hibernate.ddl-auto=update` creates or updates the entity tables when the application starts. Keep the database server running while starting the API.

## Run in VS Code

1. Install a JDK 17 and Maven, then open this project folder in VS Code. The Java Extension Pack is useful for Java editing and running, but is not required by the application.
2. Set `DB_PASSWORD` and create the `foodshare` schema as above. In PowerShell, set it for the current terminal before running the app:
3. Open `FoodshareApplication.java` and click **Run** above `main`, or use the VS Code terminal at the project root:

   ```powershell
  $env:DB_PASSWORD = "your-local-mysql-password"
  mvn spring-boot:run
   ```

4. Open `http://localhost:8080/signup` to register as a Donor or NGO. Passwords must be at least 8 characters and are stored as BCrypt hashes. After registration, sign in at `http://localhost:8080/signin`.
5. After sign-in, the dashboard is at `http://localhost:8080/`. Browse Donors, NGOs, Food Listings, and Claims at `http://localhost:8080/entities`; tabs and search use the existing database records. Sign out with the button in the top-right corner.
6. Stop the running process with `Ctrl+C` in the terminal.
7. To run the unit tests:

   ```powershell
   mvn test
   ```

### Confirm the MySQL Connection

Look in the startup terminal for Hibernate schema SQL and a successful Hikari connection-pool startup, followed by the Spring Boot `Started FoodshareApplication` message. A wrong password, stopped MySQL service, or missing `foodshare` schema will produce a startup connection error; check the datasource values and MySQL service before retrying.

## Sign-In and Entity Browser

Sign-up accepts a name, email, phone, password, and role (`DONOR` or `NGO`). A successful sign-up creates the corresponding donor or NGO record and a linked account record. The email must be unique; passwords must have at least 8 characters and are BCrypt-hashed. Sign in uses a server-side HTTP session; JWT is not used. Spring Security protects the dashboard, the entity browser, and all application API endpoints. The claims list is available to signed-in users through `GET /api/claims`.

## Postman Requests

All routes use `http://localhost:8080`. Sign in first; Spring Security keeps an HTTP session cookie. For POST and PUT calls in Postman, retain the session cookie and include the CSRF header and token obtained from `GET /api/auth/csrf`. For JSON request bodies, select **Body > raw > JSON**. Replace IDs with the IDs returned by your own create requests. The food expiry example is deliberately in the future; use a future local date-time when testing.

**Sign-up** - `POST /api/auth/signup` (public; CSRF header required)

```json
{
  "name": "Asha Kumar",
  "email": "asha@example.com",
  "phone": "9876543210",
  "password": "minimum-eight-characters",
  "role": "DONOR"
}
```

Use `"role": "NGO"` to create an NGO account. Successful sign-up returns `201 Created`; then sign in through the website at `/signin`.

**Get CSRF token** - `GET /api/auth/csrf` (public). Keep the session cookie and send the returned token as the `X-CSRF-TOKEN` header for state-changing requests.

**Current account** - `GET /api/auth/me` (signed in). Returns the signed-in email and `DONOR` or `NGO` role.

**List claims** - `GET /api/claims` (signed in). Returns claims with their food listing and NGO partner.

### Donors

**Create donor** - `POST /api/donors`

```json
{
  "name": "Asha Kumar",
  "email": "asha@example.com",
  "phone": "9876543210"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "Asha Kumar",
  "email": "asha@example.com",
  "phone": "9876543210"
}
```

**List donors** - `GET /api/donors` (no body). Response `200 OK`:

```json
[
  {
    "id": 1,
    "name": "Asha Kumar",
    "email": "asha@example.com",
    "phone": "9876543210"
  }
]
```

**Get donor** - `GET /api/donors/1` (no body). Response `200 OK` is the same single-donor object above. An unknown ID returns `404` with `{"error":"Donor not found with id 1"}`.

### NGOs

**Create NGO** - `POST /api/ngos`

```json
{
  "name": "Community Kitchen",
  "email": "hello@community.example",
  "phone": "9123456780"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "Community Kitchen",
  "email": "hello@community.example",
  "phone": "9123456780"
}
```

**List NGOs** - `GET /api/ngos` (no body). Response `200 OK` is a JSON array of NGO objects with `id`, `name`, `email`, and `phone`.

**Get NGO** - `GET /api/ngos/1` (no body). Response `200 OK` is the single-NGO object above. An unknown ID returns `404` with `{"error":"NGO not found with id 1"}`.

### Food Listings

**Create listing** - `POST /api/food-listings?donorId=1`

```json
{
  "foodType": "Vegetable Rice",
  "quantity": 25,
  "safeToEatUntil": "2099-12-31T18:00:00"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "donor": {
    "id": 1,
    "name": "Asha Kumar",
    "email": "asha@example.com",
    "phone": "9876543210"
  },
  "foodType": "Vegetable Rice",
  "quantity": 25,
  "safeToEatUntil": "2099-12-31T18:00:00",
  "createdAt": "2026-09-28T12:00:00",
  "status": "AVAILABLE"
}
```

`createdAt` in the response is set by the server; its example value is illustrative.

**List all listings** - `GET /api/food-listings` (no body). Response `200 OK` is a JSON array of listing objects in the format above. The response includes related donor data but not inverse collections.

**List available listings** - `GET /api/food-listings/available` (no body). Response `200 OK` is a JSON array of unexpired `AVAILABLE` listings. Listings whose safety deadline has passed are marked `EXPIRED` and omitted.

**Mark listing collected** - `PUT /api/food-listings/1/collected` (no body). First claim the listing. Response `200 OK` is the listing object with `"status": "COLLECTED"`. If it is not `CLAIMED`, response `409 Conflict` is `{"error":"Only CLAIMED food can be marked as COLLECTED"}`.

**Monthly diverted total** - `GET /api/food-listings/diverted?year=2026&month=9` (no body). Response `200 OK` is a JSON number, for example:

```json
25
```

The total sums quantities for `COLLECTED` listings whose `createdAt` falls in the requested calendar month. The specified entity fields do not include a collection timestamp, so `createdAt` is the month boundary available to the report. If there are no matching rows, the response is `0`.

### Claims

**Claim a listing** - `POST /api/claims?listingId=1&ngoId=1` (no body). Response `201 Created`:

```json
{
  "id": 1,
  "foodListing": {
    "id": 1,
    "donor": {
      "id": 1,
      "name": "Asha Kumar",
      "email": "asha@example.com",
      "phone": "9876543210"
    },
    "foodType": "Vegetable Rice",
    "quantity": 25,
    "safeToEatUntil": "2099-12-31T18:00:00",
    "createdAt": "2026-09-28T12:00:00",
    "status": "CLAIMED"
  },
  "ngo": {
    "id": 1,
    "name": "Community Kitchen",
    "email": "hello@community.example",
    "phone": "9123456780"
  },
  "claimedAt": "2026-09-28T12:05:00",
  "status": "ACTIVE"
}
```

Claiming locks the listing row, checks the listing and NGO, checks expiry/status/active-claim rules, stores the claim, and changes the listing to `CLAIMED` in one transaction. Invalid claim rules return `409 Conflict`; unknown IDs return `404`.

### Error Response Example

Validation errors and business errors return JSON with a message rather than a stack trace. For example, a non-positive quantity returns `400 Bad Request`:

```json
{
  "error": "quantity: Quantity must be greater than zero"
}
```

## Business Rule Test Cases

The service unit tests under `src/test/java` cover these cases:

| Rule | Test/check | Expected result |
|---|---|---|
| Listing requires an existing donor | Create using an unknown donor ID | `404`; no listing saved |
| Listing requires a safety deadline | Omit `safeToEatUntil` | `400` validation or service error JSON |
| Safety deadline must be in the future | Send a past `safeToEatUntil` | `409` with a future-deadline message |
| Quantity must be positive | Send `quantity: 0` or a negative number | `400` validation JSON |
| Food type, donor/NGO name, phone, email are validated | Omit required text or send malformed email | `400` validation JSON |
| Only available food is claimable | Claim a `CLAIMED`, `COLLECTED`, or `EXPIRED` listing | `409`; no new claim |
| Expired food cannot be claimed | Claim after `safeToEatUntil` | `409`; an available row becomes `EXPIRED` |
| Only one active claim is allowed | Claim the same listing a second time | `409`; first claim remains |
| Claim is atomic | Successful claim operation | Claim is `ACTIVE`; listing becomes `CLAIMED` |
| Only claimed food can be collected | Mark an `AVAILABLE` listing collected | `409`; status unchanged |
| Claimed food can be collected | Mark a `CLAIMED` listing collected | `200`; status becomes `COLLECTED` |
| Only collected food counts in the report | Compare collected and non-collected listings in the selected month | Only collected quantities are included |
| Empty monthly result is zero | Request a month with no collected rows | `200` and numeric response `0` |

Run the automated service tests with `mvn test`. For the full REST checks, perform the Postman requests above in order, then repeat the claim and collection calls to see the rejected transitions.

## Verify Tables and Data in MySQL Workbench

After starting the application, refresh the `foodshare` schema in the Workbench **SCHEMAS** pane. Open a SQL tab and run:

```sql
USE foodshare;
SHOW TABLES;
SELECT * FROM donor;
SELECT * FROM ngo;
SELECT * FROM food_listing;
SELECT * FROM claim;
```

The tables are created from the JPA entities: `donor`, `ngo`, `food_listing`, and `claim`. Use the select statements after creating donors, NGOs, listings, and claims in Postman to inspect persisted rows and status changes.