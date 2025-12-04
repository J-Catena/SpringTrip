const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export interface Trip {
  id: number;
  name: string;
  destination: string;
  startDate: string;
  endDate: string;
}

// Crear viaje
export interface CreateTripRequest {
  name: string;
  destination: string;
  startDate: string; // "YYYY-MM-DD"
  endDate: string;   // "YYYY-MM-DD"
  currency: string;  // p.ej. "EUR"
}

// Participante
export interface Participant {
  id: number;
  name: string;
}

export interface CreateParticipantRequest {
  name: string;
}

// Gasto
export interface CreateExpenseRequest {
  amount: number;
  description?: string;
  date: string; // "YYYY-MM-DD"
  payerId: number;
}

export interface Expense {
  id: number;
  amount: number;
  description: string;
  date: string;
  payerId: number;
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

// -------- Trips --------

export async function getTrips(token: string): Promise<Trip[]> {
  const response = await fetch(`${API_BASE_URL}/api/trips`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    let message = "Error al cargar los viajes";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {
      // ignoramos errores de parseo
    }

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as Trip[];
  return data;
}

export async function createTrip(
  body: CreateTripRequest,
  token: string,
): Promise<Trip> {
  const response = await fetch(`${API_BASE_URL}/api/trips`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = "Error al crear el viaje";

    try {
      const text = await response.text();
      console.log("Respuesta 400 createTrip:", text);

      try {
        const data = JSON.parse(text) as {
          message?: string;
          error?: string;
          errors?: unknown;
        };

        if (data.message) {
          message = data.message;
        } else if (data.error) {
          message = data.error;
        } else if (data.errors) {
          message = "Error de validación en los datos del viaje.";
        }
      } catch {
        if (text) {
          message = text;
        }
      }
    } catch {
      // dejamos el mensaje por defecto
    }

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as Trip;
  return data;
}

// -------- Summary --------

export interface TripSummaryParticipant {
  id: number;
  name: string;
  totalPaid: number;
  balance: number;
}

export interface TripSummary {
  tripId: number;
  tripName: string;
  totalAmount: number;
  participants: TripSummaryParticipant[];
}

// -------- Settlement --------

export interface TripPayment {
  payerId: number;
  payerName: string;
  receiverId: number;
  receiverName: string;
  amount: number;
}

export interface TripSettlement {
  tripId: number;
  tripName: string;
  payments: TripPayment[];
}

export async function getTripSummary(
  tripId: number,
  token: string,
): Promise<TripSummary> {
  const response = await fetch(
    `${API_BASE_URL}/api/trips/${tripId}/summary`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    let message = "Error al cargar el resumen del viaje";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {}

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as TripSummary;
  return data;
}

export async function getTripSettlement(
  tripId: number,
  token: string,
): Promise<TripSettlement> {
  const response = await fetch(
    `${API_BASE_URL}/api/trips/${tripId}/settlement`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    let message = "Error al cargar las instrucciones de pago";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {}

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as TripSettlement;
  return data;
}

// -------- Participants --------

export async function createParticipant(
  tripId: number,
  body: CreateParticipantRequest,
  token: string,
): Promise<Participant> {
  const response = await fetch(
    `${API_BASE_URL}/api/trips/${tripId}/participants`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    },
  );

  if (!response.ok) {
    let message = "Error al crear el participante";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {}

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as Participant;
  return data;
}

// -------- Expenses --------

export async function createExpense(
  tripId: number,
  body: CreateExpenseRequest,
  token: string,
): Promise<Expense> {
  const response = await fetch(
    `${API_BASE_URL}/api/trips/${tripId}/expenses`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    },
  );

  if (!response.ok) {
    let message = "Error al crear el gasto";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {}

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as Expense;
  return data;
}
