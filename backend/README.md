# SpringTrip — Backend for Group Trip & Shared Expense Management

SpringTrip is a **secure backend API** built with **Java + Spring Boot**, designed to manage group trips, participants and shared expenses, including automatic balance calculation (summary) and settlement (who pays whom).

This backend powers the future full-stack application (Next.js frontend).

---

#  Features

###  Authentication & Security
- User registration & login
- JWT-based stateless authentication
- Ownership-based authorization (each trip belongs to exactly one user)
- Protected endpoints for trips, participants, expenses, summary & settlement

###  Trips
- Create, list, update and delete trips
- Trips are always linked to the authenticated user (owner)

###  Participants
- Add/update/delete participants inside a trip
- A participant belongs to exactly one trip

###  Expenses
- Add/update/delete expenses
- Validation: payer must be a participant of the trip
- Date validation inside the trip's range

###  Summary & Settlement
- Summary: total paid per participant + balance (positive = must receive, negative = must pay)
- Settlement: minimal set of payments required to settle the trip

## Real Usage Flow

1. User registers and logs in (JWT issued)
2. User creates a trip (becomes owner)
3. Participants and expenses are added
4. Backend calculates balances and settlement
5. Frontend consumes summary & settlement endpoints in real time

---

#  Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Security + JWT**
- **Spring Data JPA / Hibernate**
- **H2 Database (dev)**
- **Maven**
- **Lombok**

Testing:
- **JUnit 5**
- **Mockito** (service tests)

Documentation:
- `docs/api.md` — full endpoint reference
- Postman collection included

---

# 📂 Project Structure

src/main/java/com/jcatena/travelbackend
│
├── auth
│ ├── AuthController.java
│ ├── JwtService.java
│ ├── JwtAuthenticationFilter.java
│ └── SecurityConfig.java
│
├── user
│ ├── User.java
│ ├── UserRepository.java
│ └── UserService.java
│
├── trip
│ ├── TripController.java
│ ├── TripService.java
│ ├── TripRepository.java
│ └── dto/
│
├── participant
│ ├── ParticipantController.java
│ ├── ParticipantService.java
│ ├── ParticipantRepository.java
│ └── dto/
│
├── expense
│ ├── ExpenseController.java
│ ├── ExpenseService.java
│ ├── ExpenseRepository.java
│ └── dto/
│
├── common
│ └── exceptions/
│
└── docs
└── api.md


---

#  API Documentation

Complete endpoint documentation is available at:  
`docs/api.md`

You will find:

- Auth (register, login)
- Trips CRUD
- Participants CRUD
- Expenses CRUD
- Summary
- Settlement
- Request/response examples
- Status codes
- Usage notes

A Postman collection is also included in `/postman/`.

---

# ▶️ Running the Project

Clone the repository:

```sh
git clone https://github.com/J-Catena/SpringTrip.git
cd SpringTrip
Run the application:

sh
Copiar código
./mvnw spring-boot:run
The API will start at:

arduino
Copiar código
http://localhost:8080
Use Postman to register, login, and test all secured endpoints with the generated JWT token.

 Backend Status
Completed:

JWT authentication

All CRUD operations (Trips, Participants, Expenses)

Summary & Settlement logic

Service-level tests

Full API documentation (api.md)

Postman Collection

CORS ready for frontend (http://localhost:3000)

Deployment:

The backend is deployed on **Render** and actively consumed by the Next.js frontend.

The API is production-ready and secured using JWT.

CI/CD (optional)

Integration tests (optional)

 Project Purpose
SpringTrip is a portfolio-grade backend system designed to demonstrate:

Real authentication & authorization

Clean, layered architecture

Domain-driven logic

Professional API design

Testable and extendable backend

Ability to build production-ready systems

This is not a demo API — it's the backend of a real application.

 Author
Juan Catena — Backend Developer (Java · Spring Boot)
Portfolio: https://juancatena.vercel.app
LinkedIn: https://www.linkedin.com/in/juan-catena-marin