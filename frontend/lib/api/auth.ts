import { API_BASE_URL, ApiError, parseError } from "./client";

export async function register(name: string, email: string, password: string) {
  const res = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, email, password }),
  });

  if (!res.ok) {
    throw new ApiError(await parseError(res, "Error al registrarse"), res.status);
  }

  return (await res.json()) as { id: number; name: string; email: string };
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new ApiError(await parseError(response, "Error al iniciar sesión"), response.status);
  }

  return (await response.json()) as LoginResponse;
}
