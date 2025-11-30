# SpringTrip API – Documentación mínima

Base URL (local):

- `http://localhost:8080`

Autenticación:

- Todas las rutas excepto **registro** y **login** requieren el header:

```http
Authorization: Bearer <JWT_TOKEN>
Si el token falta o es inválido, la API responderá con 401 o 403.

1. Autenticación
1.1. Registro
POST /api/auth/register

Crea un nuevo usuario.

Request (JSON)
json
Copiar código
{
  "name": "Juan",
  "email": "juan@example.com",
  "password": "password123"
}
Responses
201 Created – Usuario creado correctamente.

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

Autentica a un usuario y devuelve un token JWT.

Request (JSON)
json
Copiar código
{
  "email": "juan@example.com",
  "password": "password123"
}
Responses
200 OK – Credenciales correctas.

401 Unauthorized – Credenciales incorrectas.

Response (ejemplo)
json
Copiar código
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
Ese token es el que se envía después en el header Authorization: Bearer <token>.

2. Trips
Los trips pertenecen a un usuario (owner).
Solo el owner puede listar, leer, modificar o eliminar sus trips.

2.1. Listar trips del usuario actual
GET /api/trips

Response (ejemplo)
json
Copiar código
[
  {
    "id": 1,
    "name": "Viaje a Asturias",
    "description": "Viaje de prueba",
    "currency": "EUR",
    "startDate": "2025-08-10",
    "endDate": "2025-08-15",
    "ownerId": 1
  }
]
2.2. Crear trip
POST /api/trips

Crea un viaje asociado al usuario autenticado (owner).

Request (JSON)
json
Copiar código
{
  "name": "Viaje a Asturias",
  "description": "Viaje de prueba",
  "currency": "EUR",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15"
}
Responses
200 OK – Trip creado.

400 Bad Request – Validación fallida (por ejemplo, startDate > endDate).

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje a Asturias",
  "description": "Viaje de prueba",
  "currency": "EUR",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "ownerId": 1
}
2.3. Obtener trip por ID
GET /api/trips/{tripId}

Solo el owner puede acceder a este trip.

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje a Asturias",
  "description": "Viaje de prueba",
  "currency": "EUR",
  "startDate": "2025-08-10",
  "endDate": "2025-08-15",
  "ownerId": 1
}
404 Not Found – Si el trip no existe o no pertenece al usuario.

2.4. Actualizar trip
PUT /api/trips/{tripId}

Actualiza campos del trip del usuario actual.

Request (JSON)
json
Copiar código
{
  "name": "Viaje al Norte",
  "description": "Actualizado",
  "currency": "EUR",
  "startDate": "2025-08-11",
  "endDate": "2025-08-16"
}
Todos los campos son opcionales en el update: solo se modifican los que se envían.

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "name": "Viaje al Norte",
  "description": "Actualizado",
  "currency": "EUR",
  "startDate": "2025-08-11",
  "endDate": "2025-08-16",
  "ownerId": 1
}
2.5. Eliminar trip
DELETE /api/trips/{tripId}

Elimina el trip y sus participantes y gastos asociados.

Responses
204 No Content – Eliminado correctamente.

404 Not Found – No existe o no pertenece al usuario.

3. Participants
Los participantes siempre se crean dentro de un trip.
Solo el owner del trip puede gestionarlos.

3.1. Listar participantes del trip
GET /api/trips/{tripId}/participants

Response (ejemplo)
json
Copiar código
[
  {
    "id": 1,
    "name": "Juan",
    "tripId": 1
  },
  {
    "id": 2,
    "name": "María",
    "tripId": 1
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
El email puede usarse como referencia, pero la respuesta actual incluye id, name y tripId.

Response (ejemplo)
json
Copiar código
{
  "id": 3,
  "name": "Luis",
  "tripId": 1
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
  "tripId": 1
}
3.4. Eliminar participante
DELETE /api/trips/{tripId}/participants/{participantId}

Responses
204 No Content – Eliminado.

404 Not Found – No existe o no pertenece al trip.

4. Expenses
Los gastos están asociados a un trip y a un participante (payer).
Solo el owner del trip puede ver o modificar los gastos.

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
    "tripId": 1,
    "payerId": 1,
    "payerName": "Juan"
  },
  {
    "id": 2,
    "amount": 45.00,
    "description": "Gasolina",
    "date": "2025-08-11",
    "tripId": 1,
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
Restricciones:

amount > 0

payerId debe ser un participante de ese tripId

date debe estar dentro del rango del viaje (startDate–endDate)

Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "amount": 120.50,
  "description": "Hotel",
  "date": "2025-08-10",
  "tripId": 1,
  "payerId": 1,
  "payerName": "Juan"
}
4.3. Actualizar gasto
PUT /api/trips/{tripId}/expenses/{expenseId}

Request (JSON)
Todos los campos son opcionales:

json
Copiar código
{
  "amount": 130.00,
  "description": "Hotel (actualizado)",
  "date": "2025-08-10",
  "payerId": 2
}
Response (ejemplo)
json
Copiar código
{
  "id": 1,
  "amount": 130.00,
  "description": "Hotel (actualizado)",
  "date": "2025-08-10",
  "tripId": 1,
  "payerId": 2,
  "payerName": "María"
}
4.4. Eliminar gasto
DELETE /api/trips/{tripId}/expenses/{expenseId}

Responses
204 No Content – Eliminado.

404 Not Found – No existe o no pertenece al trip.

5. Summary
Resumen de cuánto ha pagado cada participante y cuál es su balance (a favor o en contra).

GET /api/trips/{tripId}/summary

Response (ejemplo)
json
Copiar código
{
  "tripId": 1,
  "tripName": "Viaje a Asturias",
  "totalAmount": 295.50,
  "participants": [
    {
      "id": 1,
      "name": "Juan",
      "totalPaid": 165.50,
      "balance": 67.00
    },
    {
      "id": 2,
      "name": "María",
      "totalPaid": 130.00,
      "balance": -67.00
    }
  ]
}
Notas:

totalPaid = lo que ha pagado cada uno.

balance > 0 → esa persona debe recibir dinero.

balance < 0 → esa persona debe pagar dinero.

6. Settlement
Cálculo de deudas mínimas: quién paga a quién y cuánto.

GET /api/trips/{tripId}/settlement

Response (ejemplo)
json
Copiar código
{
  "tripId": 1,
  "tripName": "Viaje a Asturias",
  "payments": [
    {
      "payerId": 2,
      "payerName": "María",
      "receiverId": 1,
      "receiverName": "Juan",
      "amount": 31.50
    }
  ]
}
Cada elemento de payments indica una transferencia:

payerId / payerName → quién paga.

receiverId / receiverName → quién recibe.

amount → cuánto debe pagar.

FIN DEL DOCUMENTO