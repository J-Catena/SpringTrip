"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { getTrips, Trip } from "@/lib/api/trips";
import { ApiError } from "@/lib/api/client";

export default function DashboardPage() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("authToken");
    if (!token) {
      router.replace("/login");
      return;
    }

    const loadTrips = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await getTrips(token);
        setTrips(data);
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.status === 401 || err.status === 403) {
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
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    router.push("/login");
  };

  return (
    <main className="min-h-screen bg-slate-950 text-slate-50">
      <header className="border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <h1 className="text-lg font-semibold">SpringTrip · Dashboard</h1>
        <div className="flex items-center gap-3">
          <Link
            href="/trips/new"
            className="text-xs px-3 py-1.5 rounded-lg bg-emerald-500 text-slate-950 font-medium hover:bg-emerald-400 transition"
          >
            Nuevo viaje
          </Link>
          <button
            onClick={handleLogout}
            className="text-xs px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 transition"
          >
            Cerrar sesión
          </button>
        </div>
      </header>

      <section className="p-6 max-w-4xl mx-auto">
        <h2 className="text-base font-semibold mb-2">Tus viajes</h2>
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

        {!loading && !error && trips.length > 0 && (
          <ul className="space-y-3">
            {trips.map((trip) => (
              <li key={trip.id}>
                <Link
                  href={`/trips/${trip.id}`}
                  className="block border border-slate-800 rounded-xl px-4 py-3 bg-slate-900 hover:bg-slate-800 transition"
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
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
