"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { createTrip, CreateTripRequest, ApiError } from "@/lib/api/trips";

export default function NewTripPage() {
  const router = useRouter();

  const [form, setForm] = useState<CreateTripRequest>({
    name: "",
    destination: "",
    startDate: "",
    endDate: "",
    currency: "EUR",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange =
    (field: keyof CreateTripRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (
      !form.name ||
      !form.destination ||
      !form.startDate ||
      !form.endDate ||
      !form.currency
    ) {
      setError("Todos los campos son obligatorios.");
      return;
    }

    if (form.endDate < form.startDate) {
      setError("La fecha de fin no puede ser anterior a la de inicio.");
      return;
    }

    const token = localStorage.getItem("authToken");
    if (!token) {
      router.replace("/login");
      return;
    }

    setLoading(true);
    try {
      const created = await createTrip(form, token);
      router.push(`/trips/${created.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 400) {
          setError(err.message || "Datos inválidos al crear el viaje.");
        } else if (err.status === 401 || err.status === 403) {
          localStorage.removeItem("authToken");
          router.replace("/login");
          return;
        } else {
          setError(err.message || "Error al crear el viaje.");
        }
      } else {
        setError("Error inesperado al crear el viaje.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push("/dashboard");
  };

  return (
    <main className="min-h-screen bg-slate-950 text-slate-50">
      <header className="border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <h1 className="text-lg font-semibold">Nuevo viaje</h1>
        <button
          onClick={handleCancel}
          className="text-xs px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 transition"
        >
          Cancelar
        </button>
      </header>

      <section className="p-6 max-w-xl mx-auto">
        <p className="text-sm text-slate-400 mb-4">
          Define un nuevo viaje. Después podrás añadir participantes y gastos.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="name"
              className="block text-sm font-medium text-slate-200 mb-1"
            >
              Nombre
            </label>
            <input
              id="name"
              type="text"
              value={form.name}
              onChange={handleChange("name")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="Título del viaje"
            />
          </div>

          <div>
            <label
              htmlFor="destination"
              className="block text-sm font-medium text-slate-200 mb-1"
            >
              Destino
            </label>
            <input
              id="destination"
              type="text"
              value={form.destination}
              onChange={handleChange("destination")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              placeholder="...dónde vas?"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label
                htmlFor="startDate"
                className="block text-sm font-medium text-slate-200 mb-1"
              >
                Fecha de inicio
              </label>
              <input
                id="startDate"
                type="date"
                value={form.startDate}
                onChange={handleChange("startDate")}
                className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              />
            </div>

            <div>
              <label
                htmlFor="endDate"
                className="block text-sm font-medium text-slate-200 mb-1"
              >
                Fecha de fin
              </label>
              <input
                id="endDate"
                type="date"
                value={form.endDate}
                onChange={handleChange("endDate")}
                className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="currency"
              className="block text-sm font-medium text-slate-200 mb-1"
            >
              Moneda
            </label>
            <select
              id="currency"
              value={form.currency}
              onChange={handleChange("currency")}
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            >
              <option value="EUR">EUR (€)</option>
              <option value="USD">USD ($)</option>
              
            </select>
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
            {loading ? "Creando viaje..." : "Crear viaje"}
          </button>
        </form>
      </section>
    </main>
  );
}
