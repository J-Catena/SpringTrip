"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getTrips, Trip, ApiError } from "@/lib/api/trips";

export default function DashboardPage() {
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState<string | null>(null);
  const [trips, setTrips] = useState<Trip[]>([]);
  const [error, setError] = useState<string | null>(null);

  const router = useRouter();

  useEffect(() => {
    const storedToken = localStorage.getItem("authToken");

    if (!storedToken) {
      router.replace("/login");
      return;
    }

    setToken(storedToken);
  }, [router]);

  useEffect(() => {
    if (!token) return;

    const loadTrips = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await getTrips(token);
        setTrips(data);
      } catch (err) {
        console.error("Error cargando trips:", err);

        if (err instanceof ApiError) {
          if (err.status === 401 || err.status === 403) {
            // Sesión caducada o no autorizada → limpiamos y al login
            localStorage.removeItem("authToken");
            router.replace("/login");
            return;
          }
          setError(err.message);
        } else {
          setError("Error inesperado al cargar los viajes.");
        }
      } finally {
        setLoading(false);
      }
    };

    void loadTrips();
  }, [token, router]);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    router.push("/login");
  };

  return (
    <main className="min-h-screen bg-slate-950 text-slate-50">
      <header className="border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <h1 className="text-lg font-semibold">SpringTrip · Dashboard</h1>
        <button
          onClick={handleLogout}
          className="text-xs px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 transition"
        >
          Cerrar sesión
        </button>
      </header>

      <section className="p-6 max-w-4xl mx-auto">
        <h2 className="text-base font-semibold mb-2">
          Tus viajes
        </h2>
        <p className="text-sm text-slate-400 mb-4">
          Esta lista viene directamente del backend protegido con JWT.
        </p>

        {loading && (
          <p className="text-sm text-slate-400">Cargando viajes...</p>
        )}

        {error && (
          <p className="text-sm text-red-400 bg-red-950/40 border border-red-900 rounded-lg px-3 py-2 mb-4">
            {error}
          </p>
        )}

        {!loading && !error && trips.length === 0 && (
          <p className="text-sm text-slate-400">
            No tienes viajes creados todavía.
          </p>
        )}

        {!loading && trips.length > 0 && (
          <ul className="space-y-3">
            {trips.map((trip) => (
              <li
                key={trip.id}
                className="border border-slate-800 rounded-xl px-4 py-3 bg-slate-900"
              >
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold">{trip.name}</p>
                    <p className="text-xs text-slate-400">
                      {trip.destination}
                    </p>
                  </div>
                  <p className="text-xs text-slate-400">
                    {trip.startDate} — {trip.endDate}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
