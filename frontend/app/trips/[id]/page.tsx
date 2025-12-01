"use client";

import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  getTripSummary,
  getTripSettlement,
  TripSummary,
  TripSettlement,
  ApiError,
} from "@/lib/api/trips";

interface TripDetailPageProps {
  params: Promise<{ id: string }>;
}

export default function TripDetailPage({ params }: TripDetailPageProps) {
  
  const { id } = use(params);
  const tripId = Number(id);

  const router = useRouter();

  const [summary, setSummary] = useState<TripSummary | null>(null);
  const [settlement, setSettlement] = useState<TripSettlement | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  if (!id || Number.isNaN(tripId)) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-slate-950 text-red-400">
        ID de viaje inválido.
      </main>
    );
  }

  useEffect(() => {
    const token = localStorage.getItem("authToken");
    if (!token) {
      router.replace("/login");
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        const [summaryData, settlementData] = await Promise.all([
          getTripSummary(tripId, token),
          getTripSettlement(tripId, token),
        ]);

        setSummary(summaryData);
        setSettlement(settlementData);
      } catch (err) {
        console.error("Error cargando detalle de viaje:", err);

        if (err instanceof ApiError) {
          if (err.status === 401 || err.status === 403) {
            localStorage.removeItem("authToken");
            router.replace("/login");
            return;
          }
          setError(err.message);
        } else {
          setError("Error inesperado al cargar el viaje.");
        }
      } finally {
        setLoading(false);
      }
    };

    void loadData();
  }, [tripId, router]);

  const handleBack = () => {
    router.push("/dashboard");
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-slate-950 text-slate-50 flex items-center justify-center">
        <p className="text-sm text-slate-300">Cargando detalle del viaje...</p>
      </main>
    );
  }

  if (error || !summary || !settlement) {
    return (
      <main className="min-h-screen bg-slate-950 text-slate-50 flex flex-col items-center justify-center px-4">
        <p className="text-sm text-red-400 mb-4">
          {error ?? "No se pudieron cargar los datos del viaje."}
        </p>
        <button
          onClick={handleBack}
          className="text-xs px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 transition"
        >
          Volver al dashboard
        </button>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-950 text-slate-50">
      <header className="border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">
            {summary.tripName}
          </h1>
          <p className="text-xs text-slate-400">
            Detalle del viaje · ID {summary.tripId}
          </p>
        </div>
        <button
          onClick={handleBack}
          className="text-xs px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 transition"
        >
          Volver
        </button>
      </header>

      <section className="p-6 max-w-5xl mx-auto grid gap-6 md:grid-cols-2">
        {/* Resumen */}
        <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
          <h2 className="text-sm font-semibold mb-2">
            Resumen de gastos
          </h2>
          <p className="text-xs text-slate-400 mb-4">
            Total del viaje:{" "}
            <span className="font-semibold text-slate-100">
              {summary.totalAmount.toFixed(2)} €
            </span>
          </p>

          <ul className="space-y-2">
            {summary.participants.map((p) => (
              <li
                key={p.id}
                className="flex items-center justify-between text-xs bg-slate-950 rounded-xl px-3 py-2"
              >
                <div>
                  <p className="font-medium">{p.name}</p>
                  <p className="text-slate-400">
                    Pagado: {p.totalPaid.toFixed(2)} €
                  </p>
                </div>
                <p
                  className={
                    "font-semibold " +
                    (p.balance < 0
                      ? "text-red-400"
                      : p.balance > 0
                      ? "text-emerald-400"
                      : "text-slate-300")
                  }
                >
                  {p.balance.toFixed(2)} €
                </p>
              </li>
            ))}
          </ul>
        </div>

        {/* Settlement */}
        <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
          <h2 className="text-sm font-semibold mb-2">
            Instrucciones de pago
          </h2>
          {settlement.payments.length === 0 ? (
            <p className="text-xs text-slate-400">
              No hay pagos pendientes. El viaje está equilibrado.
            </p>
          ) : (
            <ul className="space-y-2 text-xs">
              {settlement.payments.map((pay, index) => (
                <li
                  key={index}
                  className="bg-slate-950 rounded-xl px-3 py-2 flex justify-between gap-3"
                >
                  <span>
                    <span className="font-semibold">{pay.payerName}</span>{" "}
                    debe pagar a{" "}
                    <span className="font-semibold">{pay.receiverName}</span>
                  </span>
                  <span className="font-semibold text-emerald-400">
                    {pay.amount.toFixed(2)} €
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </main>
  );
}
