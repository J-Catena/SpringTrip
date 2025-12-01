
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export interface Trip {
  id: number;
  name: string;
  destination: string;
  startDate: string;
  endDate: string;

}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

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
      
    }

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as Trip[];
  return data;
}
