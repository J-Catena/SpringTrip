"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { register, login } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { saveToken } from "@/lib/auth";

interface FormState {
  name: string;
  email: string;
  password: string;
}

export default function RegisterPage() {
  const router = useRouter();

  const [form, setForm] = useState<FormState>({
    name: "",
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange =
    (field: keyof FormState) =>
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.name.trim() || !form.email.trim() || !form.password) {
      setError("Nombre, email y contraseña son obligatorios.");
      return;
    }

    if (form.password.length < 6) {
      setError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }

    setLoading(true);
    try {
      // 1) Crear usuario
      await register(form.name.trim(), form.email.trim(), form.password);

      // 2) Login automático
      const { token } = await login({
        email: form.email.trim(),
        password: form.password,
      });

      saveToken(token);
      router.push("/trips");
    } catch (err) {
      console.error("ERROR EN REGISTER:", err);

      if (err instanceof ApiError) {
        if (err.status === 400) setError(err.message || "Datos inválidos.");
        else if (err.status === 409) setError("Ese email ya está registrado.");
        else setError(err.message || "Error al registrarse.");
      } else {
        setError("Error inesperado. Revisa la consola.");
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
          Crea tu cuenta para empezar.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-200 mb-1">
              Nombre
            </label>
            <input
              type="text"
              value={form.name}
              onChange={handleChange("name")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="Tu nombre"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-200 mb-1">
              Email
            </label>
            <input
              type="email"
              value={form.email}
              onChange={handleChange("email")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="tucorreo@ejemplo.com"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-200 mb-1">
              Contraseña
            </label>
            <input
              type="password"
              value={form.password}
              onChange={handleChange("password")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="mínimo 6 caracteres"
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
            {loading ? "Creando cuenta..." : "Crear cuenta"}
          </button>
        </form>

        <div className="mt-6 text-xs text-slate-500 text-center space-y-2">
          <p>
            ¿Ya tienes cuenta?{" "}
            <button
              onClick={() => router.push("/login")}
              className="text-emerald-400 hover:text-emerald-300 underline"
            >
              Inicia sesión
            </button>
          </p>
        </div>
      </div>
    </main>
  );
}
