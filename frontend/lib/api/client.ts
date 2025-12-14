export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export async function parseError(res: Response, fallback: string) {
  let message = fallback;

  try {
    const data = (await res.json()) as { message?: string; error?: string };
    message = data.message ?? data.error ?? fallback;
  } catch {
    
    try {
      const text = await res.text();
      if (text) message = text;
    } catch {}
  }

  return message;
}
