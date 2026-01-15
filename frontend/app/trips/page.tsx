"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getTrips, Trip } from "@/lib/api/trips";
import { ApiError } from "@/lib/api/client";
import { clearToken, getToken } from "@/lib/auth";

export default function TripsPage() {
  const router = useRouter();
  const [trips, setTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTrips = useCallback(async () => {
    const token = getToken();

    if (!token) {
      setLoading(false);
      router.replace("/login");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await getTrips(token);
      setTrips(data);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401 || err.status === 403) {
          clearToken();
          router.replace("/login");
          return;
        }
        setError(err.message);
      } else {
        setError("Error inesperado cargando viajes.");
      }
    } finally {
      setLoading(false);
    }
  }, [router]);

  useEffect(() => {
    void loadTrips();
  }, [loadTrips]);

  return (
    <main className="min-h-screen bg-slate-950 px-4 py-10">
      <div className="mx-auto w-full max-w-3xl">
        <header className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-slate-50">Mis viajes</h1>

          <div className="flex gap-2">
            <button
              onClick={() => router.push("/trips/new")}
              className="rounded-lg bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-medium px-4 py-2 text-sm transition"
            >
              Nuevo viaje
            </button>

            <button
              onClick={() => void loadTrips()}
              className="rounded-lg border border-slate-700 text-slate-100 px-4 py-2 text-sm hover:bg-slate-900 transition"
            >
              Recargar
            </button>

            <button
              onClick={() => {
                clearToken();
                router.push("/login");
              }}
              className="rounded-lg border border-slate-700 text-slate-100 px-4 py-2 text-sm hover:bg-slate-900 transition"
            >
              Logout
            </button>
          </div>
        </header>

        {loading && <p className="text-slate-300">Cargando...</p>}

        {error && (
          <p className="text-sm text-red-400 bg-red-950/40 border border-red-900 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        {!loading && !error && trips.length === 0 && (
          <p className="text-slate-300">No tienes viajes todavía.</p>
        )}

        {!loading && !error && trips.length > 0 && (
          <ul className="space-y-3">
            {trips.map((t) => (
              <li
                key={t.id}
                className="rounded-xl border border-slate-800 bg-slate-900 p-4 hover:bg-slate-800/60 transition cursor-pointer"
                onClick={() => router.push(`/trips/${t.id}`)}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-slate-50 font-semibold">{t.name}</div>
                    <div className="text-slate-400 text-sm">{t.destination}</div>
                  </div>
                  <div className="text-slate-400 text-sm">
                    {t.startDate} → {t.endDate}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </main>
  );
}
