"use client";

import { formatCurrency } from "@/lib/utils/format";
import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  getTripSummary,
  getTripSettlement,
  TripSummary,
  TripSettlement,
  ApiError,
  createParticipant,
  createExpense,
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

  const [token, setToken] = useState<string | null>(null);

  // Estado del formulario de participante
  const [participantName, setParticipantName] = useState("");
  const [participantError, setParticipantError] = useState<string | null>(null);
  const [participantLoading, setParticipantLoading] = useState(false);

  // Estado del formulario de gasto
  const [expenseAmount, setExpenseAmount] = useState("");
  const [expenseDescription, setExpenseDescription] = useState("");
  const [expenseDate, setExpenseDate] = useState("");
  const [expensePayer, setExpensePayer] = useState<number | null>(null);
  const [expenseError, setExpenseError] = useState<string | null>(null);
  const [expenseLoading, setExpenseLoading] = useState(false);

  if (!id || Number.isNaN(tripId)) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-slate-950 text-red-400">
        ID de viaje inválido.
      </main>
    );
  }

  const loadData = async (authToken: string) => {
    try {
      setLoading(true);
      setError(null);

      const [summaryData, settlementData] = await Promise.all([
        getTripSummary(tripId, authToken),
        getTripSettlement(tripId, authToken),
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

  useEffect(() => {
    const authToken = localStorage.getItem("authToken");
    if (!authToken) {
      router.replace("/login");
      return;
    }

    setToken(authToken);
    void loadData(authToken);
  }, [tripId, router]);

  const handleBack = () => {
    router.push("/dashboard");
  };

  const handleAddParticipant = async (e: React.FormEvent) => {
    e.preventDefault();
    setParticipantError(null);

    if (!participantName.trim()) {
      setParticipantError("El nombre es obligatorio.");
      return;
    }

    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      setParticipantLoading(true);
      await createParticipant(tripId, { name: participantName.trim() }, token);
      setParticipantName("");
      await loadData(token);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 400) {
          setParticipantError(err.message || "Datos inválidos.");
        } else if (err.status === 401 || err.status === 403) {
          localStorage.removeItem("authToken");
          router.replace("/login");
          return;
        } else {
          setParticipantError(err.message || "Error al crear el participante.");
        }
      } else {
        setParticipantError("Error inesperado al crear el participante.");
      }
    } finally {
      setParticipantLoading(false);
    }
  };

  const handleAddExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    setExpenseError(null);

    if (!expenseAmount || !expenseDate || !expensePayer) {
      setExpenseError("Importe, fecha y pagador son obligatorios.");
      return;
    }

    const amountValue = Number(expenseAmount);
    if (Number.isNaN(amountValue) || amountValue <= 0) {
      setExpenseError("El importe debe ser mayor que 0.");
      return;
    }

    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      setExpenseLoading(true);

      await createExpense(
        tripId,
        {
          amount: amountValue,
          description: expenseDescription || "",
          date: expenseDate,
          payerId: expensePayer,
        },
        token
      );

      // limpiar formulario
      setExpenseAmount("");
      setExpenseDescription("");
      setExpenseDate("");
      setExpensePayer(null);

      // recargar summary + settlement
      await loadData(token);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 400) {
          setExpenseError(err.message || "Datos inválidos.");
        } else if (err.status === 401 || err.status === 403) {
          localStorage.removeItem("authToken");
          router.replace("/login");
          return;
        } else {
          setExpenseError(err.message || "Error al crear el gasto.");
        }
      } else {
        setExpenseError("Error inesperado al crear el gasto.");
      }
    } finally {
      setExpenseLoading(false);
    }
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

  const hasParticipants = summary.participants.length > 0;

  return (
    <main className="min-h-screen bg-slate-950 text-slate-50">
      <header className="border-b border-slate-800 px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">{summary.tripName}</h1>
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

      <section className="p-6 max-w-5xl mx-auto grid gap-6 md:grid-cols-[2fr,1.5fr]">
        {/* Columna izquierda: resumen, participantes, gastos */}
        <div className="space-y-4">
          {/* Resumen */}
          <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
            <h2 className="text-sm font-semibold mb-2">Resumen de gastos</h2>
            <p className="text-xs text-slate-400 mb-4">
              Total del viaje:{" "}
              <span className="font-semibold text-slate-100">
                {formatCurrency(summary.totalAmount)}
              </span>
            </p>

            {summary.participants.length === 0 ? (
              <p className="text-xs text-slate-400">
                Este viaje todavía no tiene participantes.
              </p>
            ) : (
              <ul className="space-y-2">
                {summary.participants.map((p) => (
                  <li
                    key={p.id}
                    className="flex items-center justify-between text-xs bg-slate-950 rounded-xl px-3 py-2"
                  >
                    <div>
                      <p className="font-medium">{p.name}</p>
                      <p className="text-slate-400">
                        Pagado: {formatCurrency(p.totalPaid)}
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
                      {formatCurrency(p.balance)}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Formulario para añadir participante */}
          <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
            <h2 className="text-sm font-semibold mb-2">Añadir participante</h2>
            <p className="text-xs text-slate-400 mb-3">
              Solo necesitas el nombre por ahora. Más adelante podrás gestionar
              emails u otros datos.
            </p>

            <form onSubmit={handleAddParticipant} className="space-y-3">
              <input
                type="text"
                value={participantName}
                onChange={(e) => setParticipantName(e.target.value)}
                placeholder="Nombre del participante"
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-xs text-slate-50 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              />

              {participantError && (
                <p className="text-xs text-red-400 bg-red-950/40 border border-red-900 rounded-lg px-3 py-2">
                  {participantError}
                </p>
              )}

              <button
                type="submit"
                disabled={participantLoading}
                className="text-xs px-3 py-2 rounded-lg bg-emerald-500 text-slate-950 font-medium hover:bg-emerald-400 disabled:opacity-70 disabled:cursor-not-allowed transition"
              >
                {participantLoading
                  ? "Añadiendo participante..."
                  : "Añadir participante"}
              </button>
            </form>
          </div>

          {/* Formulario para añadir gasto */}
          <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
            <h2 className="text-sm font-semibold mb-2">Añadir gasto</h2>

            {!hasParticipants ? (
              <p className="text-xs text-slate-400">
                Primero añade al menos un participante para poder registrar
                gastos.
              </p>
            ) : (
              <form onSubmit={handleAddExpense} className="space-y-3 text-xs">
                <input
                  type="number"
                  step="0.01"
                  value={expenseAmount}
                  onChange={(e) => setExpenseAmount(e.target.value)}
                  placeholder="Importe"
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-slate-50 focus:ring-2 focus:ring-emerald-500"
                />

                <input
                  type="text"
                  value={expenseDescription}
                  onChange={(e) => setExpenseDescription(e.target.value)}
                  placeholder="Descripción (opcional)"
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-slate-50 focus:ring-2 focus:ring-emerald-500"
                />

                <input
                  type="date"
                  value={expenseDate}
                  onChange={(e) => setExpenseDate(e.target.value)}
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-slate-50 focus:ring-2 focus:ring-emerald-500"
                />

                <select
                  value={expensePayer ?? ""}
                  onChange={(e) => setExpensePayer(Number(e.target.value))}
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-slate-50"
                >
                  <option value="">Selecciona pagador</option>
                  {summary.participants.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>

                {expenseError && (
                  <p className="text-xs text-red-400 bg-red-950/40 border border-red-900 px-3 py-2 rounded-lg">
                    {expenseError}
                  </p>
                )}

                <button
                  type="submit"
                  disabled={expenseLoading}
                  className="w-full rounded-lg bg-emerald-500 text-slate-950 font-medium py-2.5 hover:bg-emerald-400 disabled:opacity-70"
                >
                  {expenseLoading ? "Añadiendo gasto..." : "Añadir gasto"}
                </button>
              </form>
            )}
          </div>
        </div>

        {/* Columna derecha: settlement */}
        <div className="border border-slate-800 rounded-2xl bg-slate-900 p-4">
          <h2 className="text-sm font-semibold mb-2">Instrucciones de pago</h2>
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
                    <span className="font-semibold">{pay.payerName}</span> debe
                    pagar a{" "}
                    <span className="font-semibold">{pay.receiverName}</span>
                  </span>
                  <span className="font-semibold text-emerald-400">
                    {formatCurrency(pay.amount)}
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
