SpringTrip — Travel Expense Splitter

Aplicación full-stack para gestionar gastos compartidos de viajes

🇪🇸 Descripción (Español)

SpringTrip es una aplicación full-stack diseñada para gestionar gastos compartidos en viajes de forma sencilla y clara.

Permite:

Crear viajes con nombre, destino, fechas y moneda.

Añadir participantes.

Registrar gastos asociados a un pagador.

Ver un resumen automático de cuánto ha pagado cada persona.

Obtener las instrucciones mínimas de pago (“X debe pagar a Y Z €”).

Este proyecto demuestra dominio en:

Java 21 + Spring Boot 3

Next.js 15 + TypeScript

JWT Authentication

Arquitectura limpia, validaciones, pruebas y experiencia completa de usuario.

⚙️ Tecnologías Principales
Backend (/backend)

Java 21

Spring Boot 3.5

Spring Security + JWT

Spring Data JPA

H2 / PostgreSQL

Maven

Pruebas de dominio (summary, settlement, expenses, trips)

Frontend (/frontend)

Next.js 15 (App Router)

TypeScript

React

Tailwind CSS

Fetch API + JWT

Rutas protegidas y control de sesión

🚀 Funcionalidades Principales
🔐 Autenticación

Registro (POST /api/auth/register)

Login con JWT

Acceso restringido por propietario del viaje

🗺️ Viajes

Crear viajes con validaciones completas

Listado de viajes del usuario autenticado

👥 Participantes

Añadir participantes

Asociarlos como posibles pagadores de gastos

💸 Gastos

Validaciones en backend:

Pagador debe pertenecer al viaje

Fecha dentro del rango del viaje

Importe > 0

📊 Resumen y liquidación

/summary: balance total y por participante

/settlement: instrucciones de pago mínimas

🖥️ Frontend funcional

Login conectado a JWT

Dashboard

Crear viajes

Detalle con recalculado automático

📁 Estructura del Proyecto
SpringTrip/
  backend/                
    src/main/java/...     
    src/test/java/...      
    docs/api.md           

  frontend/
    app/
      login/
      dashboard/
      trips/
        new/
        [id]/
    lib/api/
    lib/utils/

🧪 Cómo Ejecutarlo en Local
1. Backend
cd backend
./mvnw spring-boot:run


Corre en:
👉 http://localhost:8080

Documentación: backend/docs/api.md

2. Frontend
cd frontend
npm install
npm run dev


Disponible en:
👉 http://localhost:3000

Crear archivo .env.local:

NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

☁️ Demo / Deploy (Próximamente)
Servicio	Estado
Backend (Render / Railway)	🔄 Preparando
Frontend (Vercel)	🔄 Preparando
Demo pública	🔜 Próximamente

URL prevista:
https://springtrip.vercel.app

🧪 Tests

Incluidos en el backend:

Lógica de dominio completa

Validaciones de negocio

Algoritmo de settlement

Seguridad: acceso por propietario

Ejecutar:

cd backend
./mvnw test

📌 Mejoras Futuras

Página de registro en frontend

Edición de gastos y participantes

Eliminación de viajes

UI mejorada

Internacionalización

Deploy con PostgreSQL

🇬🇧 English Version
Overview

SpringTrip is a full-stack application that helps users track shared travel expenses easily and accurately.

It provides:

Trip creation with destination, dates and currency

Participant management

Expense tracking

Automatic balance summary

Minimal payment instructions

This project demonstrates practical skills with:

Java 21 + Spring Boot 3

Next.js 15 + TypeScript

JWT Authentication

Domain logic, validations and clean architecture

Tech Stack
Backend (/backend)

Java 21

Spring Boot 3

Spring Security + JWT

JPA / Hibernate

Maven

Domain & service tests

Frontend (/frontend)

Next.js 15

TypeScript

React

Tailwind CSS

Fetch API + JWT

Features

Authentication: register, login, owner-based resource access

Trips: create and list trips

Participants: add participants

Expenses: add expenses with validations

Summary & Settlement: per-user balance, minimal payment calculations

Project Structure
backend/   → Spring Boot API
frontend/  → Next.js 15 application

Running Locally
Backend
cd backend
./mvnw spring-boot:run


API: http://localhost:8080

Frontend
cd frontend
npm install
npm run dev


App: http://localhost:3000

Deployment (coming soon)

Backend on Render / Railway

Frontend on Vercel

License

MIT License.
