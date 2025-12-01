

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export interface LoginRequest {
  email: string;
  password: string;
}


export interface LoginResponse {
  token: string;
}


export interface AuthUser {
  id: number;
  email: string;
  fullName: string;
  roles: string[];
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export async function login(
  credentials: LoginRequest
): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    let message = "Error al iniciar sesión";

    try {
      const data = (await response.json()) as { message?: string };
      if (data.message) message = data.message;
    } catch {
      // ignoramos error de parseo
    }

    throw new ApiError(message, response.status);
  }

  const data = (await response.json()) as LoginResponse;
  return data;
}
