"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { login } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { saveToken } from "@/lib/auth";

interface FormState {
  email: string;
  password: string;
}

export default function LoginPage() {
  const [form, setForm] = useState<FormState>({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const handleChange =
    (field: keyof FormState) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.email || !form.password) {
      setError("Email y contraseña son obligatorios.");
      return;
    }

    setLoading(true);
    try {
      const data = await login({
        email: form.email.trim(),
        password: form.password,
      });

      saveToken(data.token);

      router.push("/trips");
    } catch (err) {
      console.error("ERROR EN LOGIN:", err);

      if (err instanceof ApiError) {
        if (err.status === 401) setError("Credenciales incorrectas.");
        else setError(err.message || "Error al iniciar sesión.");
      } else {
        setError("Error inesperado. Revisa la consola del navegador.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-lg">
        <h1 className="text-2xl font-semibold text-slate-50 mb-1 text-center">
          SpringTrip
        </h1>
        <p className="text-sm text-slate-400 mb-6 text-center">
          Inicia sesión para gestionar tus viajes y gastos compartidos.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-slate-200 mb-1"
            >
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              value={form.email}
              onChange={handleChange("email")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="tucorreo@ejemplo.com"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-200 mb-1"
            >
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={form.password}
              onChange={handleChange("password")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="••••••••"
            />
          </div>

          {error && (
            <p className="text-sm text-red-400 bg-red-950/40 border border-red-900 rounded-lg px-3 py-2">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-emerald-500 hover:bg-emerald-400 disabled:opacity-70 disabled:cursor-not-allowed text-slate-950 font-medium py-2.5 text-sm transition"
          >
            {loading ? "Iniciando sesión..." : "Entrar"}
          </button>
        </form>
        <p className="mt-6 text-xs text-slate-500 text-center">
          ¿No tienes cuenta?{" "}
          <a
            href="/register"
            className="text-emerald-400 hover:text-emerald-300 underline"
          >
            Regístrate
          </a>
        </p>

        <p className="mt-6 text-xs text-slate-500 text-center">
          Proyecto SpringTrip — Login conectado al backend.
        </p>
      </div>
    </main>
  );
}
