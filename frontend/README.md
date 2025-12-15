# SpringTrip — Frontend

SpringTrip is a full-stack web application designed to manage shared travel expenses in a simple, transparent way.

This repository contains the **frontend**, built with **Next.js (App Router)** and connected to a secured Spring Boot backend using **JWT authentication**.

---

##  What does the app do?

SpringTrip allows a user to:

- Register and log in securely
- Create trips
- Add participants to each trip
- Register expenses paid by participants
- Automatically calculate balances
- Generate clear payment instructions (who pays whom and how much)

Everything works end-to-end without manual API tools (no Postman required).

---

##  Why this project exists

This project was built as a **realistic full-stack exercise**, focusing on:

- Clean separation between frontend and backend
- Real authentication and authorization (JWT)
- Real business logic (expense splitting, balances, settlements)
- Error handling and protected routes
- A usable UI, not just API calls

It is **not a demo**, but a complete working application.

---

##  Tech Stack

### Frontend
- **Next.js 15** (App Router)
- **TypeScript**
- **Tailwind CSS**
- Client-side authentication with JWT
- Modular API layer (`lib/api`)
- Protected routes and redirects

### Backend (separate repository)
- **Java 21**
- **Spring Boot**
- **Spring Security + JWT**
- **JPA / Hibernate**
- Deployed on **Render**

---

##  Authentication Flow

1. User registers or logs in
2. Backend returns a JWT
3. Token is stored in localStorage
4. All protected requests include `Authorization: Bearer <token>`
5. Invalid or expired tokens automatically redirect to login

---

##  Running the project locally

### 1. Install dependencies
```bash
npm install

2. Environment variables

Create a .env.local file:

NEXT_PUBLIC_API_BASE_URL=https://springtrip-backend.onrender.com

3. Start development server
npm run dev


Open:
 http://localhost:3000

 Tested flows

Register → Login → Create trip

Add participants

Add expenses

Automatic balance recalculation

Settlement instructions generation

Auth protection (401 / 403 handling)

📸 Screenshots

(Add 2–3 screenshots here: trips list, trip detail with summary + settlement)

 Project status

✅ Core functionality complete
✅ Stable and deployed backend
✅ Ready for portfolio presentation

Possible future improvements (not required for current scope):

User invitations by email

Expense categories

Currency conversion

Mobile optimizations

 Author

Juan Catena Marín
Backend-oriented Java developer transitioning into full-stack development.