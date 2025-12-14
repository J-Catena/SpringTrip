import { API_BASE_URL, ApiError, parseError } from "./client";

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
  currency: string;  // "EUR"
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

// -------- Trips --------

export async function getTrips(token: string): Promise<Trip[]> {
  const response = await fetch(`${API_BASE_URL}/api/trips`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new ApiError(
      await parseError(response, "Error al cargar los viajes"),
      response.status,
    );
  }

  return (await response.json()) as Trip[];
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
    throw new ApiError(
      await parseError(response, "Error al crear el viaje"),
      response.status,
    );
  }

  return (await response.json()) as Trip;
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
  const response = await fetch(`${API_BASE_URL}/api/trips/${tripId}/summary`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new ApiError(
      await parseError(response, "Error al cargar el resumen del viaje"),
      response.status,
    );
  }

  return (await response.json()) as TripSummary;
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
      cache: "no-store",
    },
  );

  if (!response.ok) {
    throw new ApiError(
      await parseError(response, "Error al cargar las instrucciones de pago"),
      response.status,
    );
  }

  return (await response.json()) as TripSettlement;
}

// -------- Participants --------

export async function createParticipant(
  tripId: number,
  body: CreateParticipantRequest,
  token: string,
): Promise<Participant> {
  const response = await fetch(`${API_BASE_URL}/api/trips/${tripId}/participants`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new ApiError(
      await parseError(response, "Error al crear el participante"),
      response.status,
    );
  }

  return (await response.json()) as Participant;
}

// -------- Expenses --------

export async function createExpense(
  tripId: number,
  body: CreateExpenseRequest,
  token: string,
): Promise<Expense> {
  const response = await fetch(`${API_BASE_URL}/api/trips/${tripId}/expenses`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new ApiError(
      await parseError(response, "Error al crear el gasto"),
      response.status,
    );
  }

  return (await response.json()) as Expense;
}
