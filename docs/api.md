# SpringTrip API – Documentación mínima

Base URL (local):
- `http://localhost:8080`

Autenticación:
- Todas las rutas excepto registro/login requieren header:
  Authorization: Bearer <JWT_TOKEN>


---

# 1. Autenticación

## 1.1. Registro

**POST** `/api/auth/register`

Crea un nuevo usuario.

### Request (JSON)

```json
{
"name": "Juan",
"email": "juan@example.com",
"password": "password123"
}
Responses
201 Created – Usuario creado.

400 Bad Request – Datos inválidos o email ya registrado.

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Juan",
  "email": "juan@example.com"
}
1.2. Login
POST /api/auth/login

Retorna un token JWT.

Request (JSON)
json
Copiar código
{
  "email": "juan@example.com",
  "password": "password123"
}
Responses
200 OK

401 Unauthorized

Response (ejemplo)
json
Copiar código
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
2. Trips
Los trips pertenecen a un usuario (owner). Solo el propietario puede accederlos.

2.1. Listar trips del usuario actual
GET /api/trips

Response (ejemplo)
json
Copiar código
[
  {
    "id": 1,
    "name": "Viaje a Asturias",
    "startDate": "2025-08-10",
    "endDate": "2025-08-15",
    "currency": "EUR",
    "createdAt": "2025-08-01T10:00:00",
    "updatedAt": "2025-08-01T10:00:00"
  }
]
2.2. Crear trip
POST /api/trips

Request (JSON)
json
Copiar código
{
  "name": "Viaje a Asturias",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "currency": "EUR"
}
Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje a Asturias",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "currency": "EUR",
  "createdAt": "2025-08-01T10:00:00",
  "updatedAt": "2025-08-01T10:00:00"
}
2.3. Obtener trip por ID
GET /api/trips/{tripId}

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje a Asturias",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "currency": "EUR",
  "participantsCount": 4,
  "expensesCount": 12,
  "createdAt": "2025-08-01T10:00:00",
  "updatedAt": "2025-08-01T10:00:00"
}
2.4. Actualizar trip
PUT /api/trips/{tripId}

Request (JSON)
json
Copiar código
{
  "name": "Viaje al Norte",
  "startDate": "2025-08-11",
  "endDate": "2025-08-16",
  "currency": "EUR"
}
Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje al Norte",
  "startDate": "2025-08-11",
  "endDate": "2025-08-16",
  "currency": "EUR",
  "createdAt": "2025-08-01T10:00:00",
  "updatedAt": "2025-08-02T18:30:00"
}
2.5. Eliminar trip
DELETE /api/trips/{tripId}

Respuestas:

204 No Content

404 Not Found

3. Participants
3.1. Listar participantes del trip
GET /api/trips/{tripId}/participants

Response (ejemplo)
json
Copiar código
[
  {
    "id": 1,
    "name": "Juan",
    "email": "juan@example.com"
  },
  {
    "id": 2,
    "name": "María",
    "email": "maria@example.com"
  }
]
3.2. Crear participante
POST /api/trips/{tripId}/participants

Request (JSON)
json
Copiar código
{
  "name": "Luis",
  "email": "luis@example.com"
}
Response (ejemplo)
json
Copiar código
{
  "id": 3,
  "name": "Luis",
  "email": "luis@example.com"
}
3.3. Actualizar participante
PUT /api/trips/{tripId}/participants/{participantId}

Request (JSON)
json
Copiar código
{
  "name": "Luis García",
  "email": "luis.garcia@example.com"
}
Response (ejemplo)
json
Copiar código
{
  "id": 3,
  "name": "Luis García",
  "email": "luis.garcia@example.com"
}
3.4. Eliminar participante
DELETE /api/trips/{tripId}/participants/{participantId}

Respuestas:

204 No Content

404 Not Found

4. Expenses
4.1. Listar gastos del trip
GET /api/trips/{tripId}/expenses

Response (ejemplo)
json
Copiar código
[
  {
    "id": 1,
    "amount": 120.50,
    "description": "Hotel",
    "date": "2025-08-10",
    "payerId": 1,
    "payerName": "Juan"
  },
  {
    "id": 2,
    "amount": 45.00,
    "description": "Gasolina",
    "date": "2025-08-11",
    "payerId": 2,
    "payerName": "María"
  }
]
4.2. Crear gasto
POST /api/trips/{tripId}/expenses

Request (JSON)
json
Copiar código
{
  "amount": 120.50,
  "description": "Hotel",
  "date": "2025-08-10",
  "payerId": 1
}
Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "amount": 120.50,
  "description": "Hotel",
  "date": "2025-08-10",
  "payerId": 1,
  "payerName": "Juan"
}
4.3. Actualizar gasto
PUT /api/trips/{tripId}/expenses/{expenseId}

Request (JSON)
json
Copiar código
{
  "amount": 130.00,
  "description": "Hotel (actualizado)",
  "date": "2025-08-10",
  "payerId": 1
}
Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "amount": 130.00,
  "description": "Hotel (actualizado)",
  "date": "2025-08-10",
  "payerId": 1,
  "payerName": "Juan"
}
4.4. Eliminar gasto
DELETE /api/trips/{tripId}/expenses/{expenseId}

Respuestas:

204 No Content

404 Not Found

5. Summary
Resumen de cuánto ha pagado cada participante y cuánto debería haber pagado.

5.1. Obtener summary del trip
GET /api/trips/{tripId}/summary

Response (ejemplo)
json
Copiar código
{
  "tripId": 1,
  "currency": "EUR",
  "totalAmount": 295.50,
  "perParticipant": [
    {
      "participantId": 1,
      "name": "Juan",
      "paid": 165.50,
      "shouldPay": 98.50,
      "balance": 67.00
    },
    {
      "participantId": 2,
      "name": "María",
      "paid": 130.00,
      "shouldPay": 98.50,
      "balance": 31.50
    }
  ]
}
6. Settlement
Cálculo de deudas: quién paga a quién.

6.1. Obtener settlement del trip
GET /api/trips/{tripId}/settlement

Response (ejemplo)
json
Copiar código
{
  "tripId": 1,
  "currency": "EUR",
  "transactions": [
    {
      "fromParticipantId": 2,
      "fromName": "María",
      "toParticipantId": 1,
      "toName": "Juan",
      "amount": 31.50
    }
  ]
}
FIN DEL DOCUMENTO