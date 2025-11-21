```md
# SpringTrip — Travel & Expense Management Backend

SpringTrip is a backend system built with **Java** and **Spring Boot**, designed to manage group trips and shared expenses.  
The goal of the project is to build a clean, scalable architecture for handling trips, participants, expenses, and balance summaries.

This repository contains the backend API that will power the full-stack application.

---

## 🚀 Features

- Create and manage **Trips**
- Add / delete **Participants**
- Add / delete **Expenses**
- REST API with modular architecture
- Controller → Service → Repository → Domain structure
- SQL persistence using Spring Data JPA

---

## 🧱 Tech Stack

- **Java 17+**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Hibernate**
- **H2** (dev) / **PostgreSQL** (future)
- **Maven**
- **JUnit** (upcoming)
- **Docker** (upcoming)

---

## 📁 Project Structure

```
src/main/java/com/jcatena/travelbackend
│
├── trip
│   ├── TripController.java
│   ├── TripService.java
│   ├── TripRepository.java
│   └── dto
│       ├── TripRequest.java
│       ├── TripResponse.java
│
├── participant
│   ├── ParticipantController.java
│   ├── ParticipantService.java
│   ├── ParticipantRepository.java
│   └── dto
│       ├── ParticipantRequest.java
│
├── expense
│   ├── ExpenseController.java
│   ├── ExpenseService.java
│   ├── ExpenseRepository.java
│   └── dto
│       ├── ExpenseRequest.java
│
└── shared
    ├── ApiResponse.java
    ├── exceptions
    └── utils
```

---

## 📡 API Endpoints (current)

### **Trips**
```
POST   /api/trips
GET    /api/trips/{id}
DELETE /api/trips/{id}
```

### **Participants**
```
POST   /api/trips/{tripId}/participants
DELETE /api/participants/{id}
```

### **Expenses**
```
POST   /api/trips/{tripId}/expenses
DELETE /api/expenses/{id}
```

More endpoints will be added as the system evolves.

---

## ▶️ Running the Project

```
git clone https://github.com/J-Catena/SpringTrip.git
cd SpringTrip
./mvnw spring-boot:run
```

The app runs at:

```
http://localhost:8080
```

---

## 🧪 Upcoming Work

- PUT endpoints for updating trip/participant/expense  
- Expense summary calculation  
- JUnit tests  
- Docker containerization  
- Deployment on Render/Railway  

---

## 🎯 Project Goal

SpringTrip serves as a **real-world backend portfolio project**, showcasing:

- Solid Java + Spring Boot foundations  
- Domain-driven design  
- Clean API architecture  
- Realistic business logic  
- Production-ready backend structure  

---

## 📬 Author

**Juan Catena — Backend Developer**  
Portfolio: https://juancatena.vercel.app  
LinkedIn: https://www.linkedin.com/in/juan-catena-marin
```
