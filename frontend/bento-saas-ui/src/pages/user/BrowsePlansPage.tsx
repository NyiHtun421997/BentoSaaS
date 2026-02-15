import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet, isApiError } from "../../lib/api/http";
import type { CategoryDto, PlanResponseDto } from "../../lib/api/types";
import { FiMapPin, FiSearch } from "react-icons/fi";
import PageLoading from "../../components/PageLoading";
import "./BrowsePlansPage.css";
import fallbackImg from "../../assets/fallback.png";

const BrowsePlansPage = () => {
  const [plans, setPlans] = useState<PlanResponseDto[]>([]);
  const [categories, setCategories] = useState<Map<string, CategoryDto>>(
    new Map<string, CategoryDto>(),
  );
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(
    null,
  );

  const [title, setTitle] = useState("");
  const [code, setCode] = useState("");
  const [page, setPage] = useState(0);
  const size = 8;
  const [mode, setMode] = useState<"all" | "search" | "nearby">("all");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const pageShellRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (mode !== "all") return;

    const getAllPlansAndCategories = async () => {
      const [cats, pls] = await Promise.all([
        apiGet<CategoryDto[]>("/plan-management/api/v1/category"),
        apiGet<PlanResponseDto[]>(
          `/plan-management/api/v1/plan?page=${page}&size=${size}`,
        ),
      ]);

      const catsMap = cats.reduce((map, cat) => {
        if (cat.id) {
          map.set(cat.id, cat);
        }
        return map;
      }, new Map<string, CategoryDto>());

      setCategories(catsMap);
      setPlans(pls);
    };

    const loadPlansAndCategories = async () => {
      setLoading(true);
      setError(null);
      try {
        await getAllPlansAndCategories();
      } catch (e: unknown) {
        setError(isApiError(e) ? e.message : "Failed to load plans");
      } finally {
        setLoading(false);
      }
    };

    loadPlansAndCategories();
  }, [page, mode]);

  useLayoutEffect(() => {
    requestAnimationFrame(() => {
      pageShellRef.current?.scrollTo({ top: 0, behavior: "smooth" });
    });
  }, [plans]);

  const searchByTitleAndCode = async () => {
    if (!title || !code) {
      setMode("all");
      setPage(0);
      setError(null);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      setMode("search");
      const plan = await apiGet<PlanResponseDto>(
        `/plan-management/api/v1/plan/bytitleandcode?title=${encodeURIComponent(
          title,
        )}&code=${encodeURIComponent(code)}`,
      );
      setPlans([plan]);
    } catch (e: unknown) {
      setPlans([]);
      setError(isApiError(e) ? e.message : "Plan not found");
    } finally {
      setLoading(false);
    }
  };

  const searchNearMe = async () => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by this browser");
      return;
    }

    setLoading(true);
    setError(null);

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const { latitude, longitude } = pos.coords;

          const pls = await apiGet<PlanResponseDto[]>(
            `/plan-management/api/v1/plan/nearby?latitude=${latitude}&longitude=${longitude}&page=0&size=5`,
          );
          setPlans(pls);
        } catch (e: unknown) {
          setPlans([]);
          setError(isApiError(e) ? e.message : "Failed to find nearby plans");
        } finally {
          setMode("nearby");
          setLoading(false);
        }
      },
      (geoErr) => {
        setLoading(false);
        if (geoErr.code === geoErr.PERMISSION_DENIED) {
          setError("Location permission denied");
        } else {
          setError("Failed to get current location");
        }
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  };

  const filteredPlans = selectedCategoryId
    ? plans.filter((p) => p.categoryIds.includes(selectedCategoryId))
    : plans;

  return (
    <div className="page-shell" ref={pageShellRef}>
      <h1 className="page-title">Browse Plans</h1>
      <p className="page-subtitle">
        Discover bento plans and subscribe in just a few clicks.
      </p>

      <div className="browse-toolbar">
        <div className="grid gap-3 md:grid-cols-[1fr_120px]">
          <div>
            <p className="text-sm font-semibold text-slate-700">
              Search by title + code
            </p>
            <div className="mt-2 flex flex-col gap-2 md:flex-row">
              <input
                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                placeholder="Title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
              <input
                className="w-1/2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                placeholder="Code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
              <button
                onClick={searchByTitleAndCode}
                className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-500"
              >
                <FiSearch className="mr-2 inline-block" aria-hidden="true" />
                Search
              </button>
            </div>
          </div>

          <div className="flex flex-col justify-end">
            <button
              onClick={searchNearMe}
              className="mt-2 h-14 w-full rounded-xl border border-slate-500 bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-slate-300"
            >
              <FiMapPin className="mr-2 inline-block" aria-hidden="true" />
              Near Me
            </button>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3 text-sm">
          <span className="font-semibold text-slate-700">Category</span>
          <select
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm"
            value={selectedCategoryId ?? ""}
            onChange={(e) => setSelectedCategoryId(e.target.value || null)}
          >
            <option value="">All</option>
            {Array.from(categories.values()).map((cat) => (
              <option key={cat.id ?? cat.name} value={cat.id ?? ""}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading && <PageLoading text="Loading plans..." />}

      {error && (
        <p className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
          {error}
        </p>
      )}

      {!loading && filteredPlans.length === 0 && (
        <p className="text-sm text-slate-500">No plans found.</p>
      )}

      <div className="plan-grid">
        {filteredPlans.map((plan) => {
          const planId = plan.planId ?? "";

          return (
            <div
              key={planId || `${plan.title}-${plan.code}`}
              className="plan-card"
            >
              <div className="flex items-start justify-between gap-4">
                <h3 className="text-lg font-semibold">{plan.title}</h3>
                <span className="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
                  {plan.status}
                </span>
              </div>

              <div className="flex items-start justify-between gap-4">
                <p className="mt-2 text-sm text-slate-600">
                  {plan.description}
                </p>
                <span className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700">
                  {plan.address.prefecture} CITY, {plan.address.city}
                </span>
              </div>

              <p className="mt-3 text-sm font-semibold text-slate-700">
                Monthly fee: ¥{plan.displaySubscriptionFee}
              </p>

              <div className="mt-2 text-sm font-semibold text-slate-600">
                Categories:{" "}
                <span className="font-medium italic text-pink-500">
                  {plan.categoryIds
                    .map((catId) => categories.get(catId)?.name || "Unknown")
                    .join(", ")}
                </span>
              </div>

              <Link
                to={`/app/plans/${planId}`}
                className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-emerald-700 hover:text-emerald-600"
              >
                View details
                <span aria-hidden="true">-&gt;</span>
              </Link>

              <Link
                to={`/app/plans/${planId}`}
                className="mt-4 hover:opacity-90"
              >
                <img
                  src={plan.imageUrl || fallbackImg}
                  alt={plan.title}
                  className="mt-4 h-90 w-full rounded-xl object-cover shadow-md"
                />
              </Link>
            </div>
          );
        })}
      </div>

      {mode === "all" ? (
        <div className="mt-4 flex items-center justify-between">
          <button
            type="button"
            className="mb-1 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-slate-300 disabled:cursor-not-allowed disabled:opacity-50"
            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            disabled={loading || page === 0}
          >
            Previous
          </button>
          <span className="text-sm text-slate-500">Page {page + 1}</span>
          <button
            type="button"
            className="mb-1 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-slate-300 disabled:cursor-not-allowed disabled:opacity-50"
            onClick={() => setPage((prev) => prev + 1)}
            disabled={loading}
          >
            Next
          </button>
        </div>
      ) : null}
    </div>
  );
};

export default BrowsePlansPage;
