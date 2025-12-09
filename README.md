#  SpringTrip — Travel Expense Splitter  
Full-stack application to manage shared travel expenses

---

# 🇪🇸 Descripción en Español

**SpringTrip** es una aplicación **full-stack** diseñada para gestionar gastos compartidos en viajes de forma sencilla y clara.

Permite:

- Crear viajes con nombre, destino, fechas y moneda.
- Añadir participantes.
- Registrar gastos asociados a un pagador.
- Ver un **resumen automático** de cuánto ha pagado cada persona.
- Obtener las **instrucciones mínimas de pago** (“X debe pagar a Y Z €”).

El objetivo del proyecto es demostrar dominio real en:

- **Java 21 + Spring Boot 3** (backend profesional)
- **Next.js 15 + TypeScript** (frontend moderno con App Router)
- **JWT Authentication**
- Arquitectura limpia, validaciones, pruebas y experiencia completa de usuario.

---

##  Tecnologías Principales

### Backend (`/backend`)
- Java 21  
- Spring Boot 3.5  
- Spring Security + JWT  
- Spring Data JPA  
- H2 / PostgreSQL (según entorno)  
- Maven  
- Tests de dominio y servicios (summary, settlement, expenses, trips)

### Frontend (`/frontend`)
- Next.js 15 (App Router)  
- TypeScript  
- React Hooks  
- Tailwind CSS  
- Fetch API + manejo de tokens JWT  
- Rutas protegidas y control de sesiones

---

##  Funcionalidades Principales

###  Autenticación
- Registro de usuario (`POST /api/auth/register`)
- Login con JWT (`POST /api/auth/login`)
- Autorización por propietario: cada usuario solo ve sus viajes.

###  Viajes
- Crear viajes con nombre, destino, fechas y moneda.
- Listado de viajes del usuario autenticado.
- Validaciones robustas: fechas, campos obligatorios, seguridad por usuario.

###  Participantes
- Añadir participantes a un viaje.
- Asociados a gastos como posibles pagadores.

###  Gastos
- Añadir gastos con:
  - importe,
  - descripción opcional,
  - fecha dentro del viaje,
  - pagador válido.
- Validaciones en backend:
  - payer no pertenece al viaje → error
  - fecha fuera de rango → error
  - amount <= 0 → error

###  Resumen y liquidación (Settlement)
- `GET /summary`: total del viaje + balance por participante.
- `GET /settlement`: quién debe a quién y cuánto.
- Algoritmo para minimizar el número de pagos.

###  Frontend funcional
- Login conectado a JWT.
- Dashboard con viajes.
- Crear viaje.
- Página de detalle con:
  - resumen,
  - participantes,
  - gastos,
  - instrucciones de pago,
  - recálculo automático.

---

##  Estructura del Proyecto

```txt
SpringTrip/
  backend/                # API REST (Spring Boot)
    src/main/java/...    # controladores, servicios, seguridad, dominio
    src/test/java/...    # pruebas unitarias
    docs/api.md          # documentación de la API

  frontend/              # Frontend Next.js 15
    app/
      login/
      dashboard/
      trips/
        new/
        [id]/
    lib/api/             # clientes fetch a la API
    lib/utils/           # helpers (formateo de moneda)

Cómo Ejecutarlo en Local
1. Backend
cd backend
./mvnw spring-boot:run


Por defecto correrá en:

http://localhost:8080


Documentación de endpoints: backend/docs/api.md

2. Frontend
cd frontend
npm install
npm run dev


App disponible en:

http://localhost:3000


Crea un archivo:

frontend/.env.local

con:

NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

 Demo / Deploy (Próximamente)
Servicio	Estado
Backend (Render / Railway)	🔄 Preparando
Frontend (Vercel)	🔄 Preparando
Demo pública	Próximamente disponible

Cuando la demo esté activa, se añadirá aquí la URL:

https://springtrip.vercel.app

 Tests

Incluidos en el backend:

Lógica de dominio completa (Trips, Participants, Expenses)

Validaciones de negocio

Algoritmo de settlement

Seguridad: acceso por propietario

Ejecución:

cd backend
./mvnw test

 Mejoras Futuras

Página de registro en frontend

Edición de gastos y participantes

Eliminación de viajes

Animaciones y UI mejorada

Internacionalización

Deploy completo con Postgres en producción

🇬🇧 English Version
Overview

SpringTrip is a full-stack application that helps users track shared travel expenses.

Features include:

Creating trips with name, destination, dates and currency

Adding participants

Adding expenses with payer, date and description

Automatic summary of balances

Settlement instructions showing minimal number of payments

This project demonstrates:

Java 21 + Spring Boot 3 backend skills

Next.js 15 + TypeScript frontend development

JWT authentication

Domain logic, validations and clean architecture

Tech Stack
Backend (/backend)

Java 21

Spring Boot 3

Spring Security + JWT

JPA / Hibernate

Maven

Domain + service tests

Frontend (/frontend)

Next.js 15

TypeScript

React

Tailwind CSS

Fetch API with JWT

Features
Authentication

Register

Login

Owner-based resource access

Trips

Create trips

List user trips

Participants

Add participants to a trip

Expenses

Register expenses with payer + validations

Summary & Settlement

Per-user balance

Minimal payment instructions

Project Structure
backend/   → Spring Boot API
frontend/  → Next.js 15 application

Running Locally
Backend
cd backend
./mvnw spring-boot:run


API at: http://localhost:8080

Frontend
cd frontend
npm install
npm run dev


App at: http://localhost:3000

Deployment (coming soon)

Backend on Render/Railway

Frontend on Vercel

Live demo link will appear here

License

MIT License.