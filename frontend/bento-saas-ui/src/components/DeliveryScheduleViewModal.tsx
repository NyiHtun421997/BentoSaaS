import { useEffect, useMemo, useState } from "react";
import BaseModal from "./BaseModal";
import PageLoading from "./PageLoading";
import type {
  UUID,
  DeliverySchedule,
  SubscriptionResponseDto,
  PlanResponseDto,
  IdWrapper,
} from "../lib/api/types";

import {
  isSameMonth,
  isToday,
  format,
  eachDayOfInterval,
  getDay,
  startOfToday,
  isEqual,
  endOfWeek,
} from "date-fns";
import { IoIosCloseCircle } from "react-icons/io";
import { RiCalendarScheduleLine } from "react-icons/ri";
import formatYmd from "../utilities/formatYmd";
import { apiGet } from "../lib/api/http";
import { useQuery } from "@tanstack/react-query";

type SubView = {
  sub: SubscriptionResponseDto;
  plan?: PlanResponseDto;
};

type Props = {
  open: boolean;
  onClose: () => void;
  target: SubView | null;
  monthStart: string; // YYYY-MM-DD
  monthEnd: string; // YYYY-MM-DD
  formatToJapaneseDateTime: (iso: string) => string;
};

const classNames = (...classes: (string | boolean | null | undefined)[]) => {
  return classes.filter(Boolean).join(" ");
};

const colStartClasses = [
  "",
  "col-start-2",
  "col-start-3",
  "col-start-4",
  "col-start-5",
  "col-start-6",
  "col-start-7",
];

const DeliveryScheduleViewModal = ({
  open,
  onClose,
  target,
  monthStart,
  monthEnd,
  formatToJapaneseDateTime,
}: Props) => {
  const planId = target?.sub.planId as UUID | undefined;
  const plan = target?.plan;

  const today = startOfToday();
  const [selectedDay, setSelectedDay] = useState<Date | null>(today);

  const { data: schedule, isLoading } = useQuery({
    queryKey: ["schedule", planId, monthStart, monthEnd],
    queryFn: () =>
      apiGet<DeliverySchedule>(
        `/plan-management/api/v1/plan/delivery-schedule?planId=${encodeURIComponent(
          planId ?? "",
        )}&start=${encodeURIComponent(formatYmd(monthStart))}&end=${encodeURIComponent(formatYmd(monthEnd))}`,
      ),
    enabled: open && !!planId,
  });

  // Reset on close (prevents stale flashes)
  useEffect(() => {
    if (!open) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setSelectedDay(null);
    }
  }, [open]);

  const resolveMeal = (
    plan: PlanResponseDto | undefined,
    planMealId?: UUID,
  ) => {
    if (!planMealId) return undefined;
    const meals = plan?.planMealResponseDtos ?? [];
    return meals.find((m) => m.planMealId === planMealId) ?? undefined;
  };

  const deliveriesByDate = useMemo(() => {
    const map = new Map<string, { planMealId?: IdWrapper }[]>();
    for (const d of schedule?.deliveryScheduleDetails ?? []) {
      const arr = map.get(d.deliveryDate) ?? [];
      arr.push({ planMealId: d.planMealId });
      map.set(d.deliveryDate, arr);
    }
    return map;
  }, [schedule]);

  const selectedDeliveries = useMemo(() => {
    if (!selectedDay) return [];
    const selectedYmd = formatYmd(selectedDay);
    return deliveriesByDate.get(selectedYmd) ?? [];
  }, [deliveriesByDate, selectedDay]);

  const days = eachDayOfInterval({
    start: monthStart,
    end: endOfWeek(monthEnd),
  });

  return (
    <BaseModal
      open={open}
      onClose={onClose}
      panelClassName="w-[95vw] md:w-[900px] md:min-w-[350px] px-4 sm:px-6 md:px-8 py-6"
    >
      {({ close }) => (
        <div className="pt-4 flex justify-between gap-1">
          <div className="w-full space-y-6">
            <div className="md:grid md:grid-cols-2 md:divide-x md:divide-gray-200">
              <div className="md:pr-4">
                <h2 className="text-lg font-semibold text-slate-900">
                  <RiCalendarScheduleLine className="inline mr-1" />
                  Delivery Schedule
                </h2>
                <p className="mt-1 text-xs text-slate-500">
                  {format(monthStart, "yyyy年M月分")}（
                  {format(monthStart, "yyyy年M月d日")} ~{" "}
                  {format(monthEnd, "yyyy年M月d日")}）
                </p>

                {isLoading ? (
                  <div className="text-sm text-slate-600">
                    <PageLoading text="Loading schedules…" />
                  </div>
                ) : !schedule ? (
                  <div className="text-sm text-slate-600">
                    No schedule found.
                  </div>
                ) : (
                  <>
                    {schedule.createdAt ? (
                      <div className="text-xs text-slate-500">
                        Created: {formatToJapaneseDateTime(schedule.createdAt)}
                      </div>
                    ) : null}

                    {/* Weekday header */}
                    <div className="grid grid-cols-7 gap-2 mt-5 text-xs leading-6 text-center text-red-400">
                      <div className="bg-red-100 rounded-full">日</div>
                      <div className="bg-red-100 rounded-full">月</div>
                      <div className="bg-red-100 rounded-full">火</div>
                      <div className="bg-red-100 rounded-full">水</div>
                      <div className="bg-red-100 rounded-full">木</div>
                      <div className="bg-red-100 rounded-full">金</div>
                      <div className="bg-red-100 rounded-full">土</div>
                    </div>

                    <div className="grid grid-cols-7 mt-2 text-sm">
                      {days.map((day, dayIdx) => {
                        const deliveries =
                          deliveriesByDate.get(formatYmd(day)) ?? [];

                        return (
                          <div
                            key={day.toString()}
                            className={classNames(
                              dayIdx === 0 && colStartClasses[getDay(day)],
                              "py-1.5 border-b border-gray-200",
                            )}
                          >
                            <button
                              type="button"
                              onClick={() => setSelectedDay(day)}
                              className={classNames(
                                selectedDay &&
                                  isEqual(day, selectedDay) &&
                                  "text-white",
                                selectedDay &&
                                  !isEqual(day, selectedDay) &&
                                  isToday(day) &&
                                  "text-red-500",
                                selectedDay &&
                                  !isEqual(day, selectedDay) &&
                                  !isToday(day) &&
                                  isSameMonth(day, monthStart) &&
                                  "text-gray-900",
                                selectedDay &&
                                  !isEqual(day, selectedDay) &&
                                  !isToday(day) &&
                                  !isSameMonth(day, monthStart) &&
                                  "text-gray-400",
                                selectedDay &&
                                  isEqual(day, selectedDay) &&
                                  isToday(day) &&
                                  "bg-red-500",
                                selectedDay &&
                                  isEqual(day, selectedDay) &&
                                  !isToday(day) &&
                                  "bg-emerald-500",
                                selectedDay &&
                                  !isEqual(day, selectedDay) &&
                                  "hover:bg-emerald-300",
                                (selectedDay && isEqual(day, selectedDay)) ||
                                  (isToday(day) && "font-semibold"),
                                deliveries.length > 0
                                  ? "bg-emerald-50 border-1 border-emerald-200"
                                  : "",
                                "mx-auto flex h-8 w-8 items-center justify-center rounded-md",
                              )}
                            >
                              <time dateTime={formatYmd(day)}>
                                {format(day, "d")}
                              </time>
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </>
                )}
              </div>

              <div className="ml-4 border-1 border-gray-100 rounded-md">
                {selectedDay ? (
                  selectedDeliveries.length === 0 ? (
                    <div className="p-4 text-center text-gray-500 text-center">
                      No deliveries on {format(selectedDay, "yyyy年M月d日")}。
                    </div>
                  ) : (
                    <div className="p-4">
                      <h3 className="text-md text-center font-semibold text-slate-900 mb-2">
                        Deliveries on {format(selectedDay, "yyyy年M月d日")}
                      </h3>
                      <div className="space-y-2">
                        {selectedDeliveries.map((x, idx) => {
                          const meal = resolveMeal(plan, x.planMealId?.value);
                          return (
                            <div
                              key={idx}
                              className="flex items-center justify-center rounded-lg px-3 py-2 text-center"
                            >
                              <div>
                                <div className="inline-block text-sm font-semibold bg-blue-50 text-blue-700 px-1 py-1 rounded-md">
                                  {meal?.name ?? "Unknown Meal"}
                                </div>

                                <img
                                  className="w-[280px] h-[200px] rounded-lg shadow-lg object-cover"
                                  src={meal?.image ?? ""}
                                  alt=""
                                />
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )
                ) : (
                  <div className="p-4 text-center text-gray-500">
                    Select a date to view deliveries.
                  </div>
                )}
              </div>
            </div>
          </div>

          <button
            className="flex flex-col items-start mb-1 text-sm rounded-full"
            onClick={close}
          >
            <IoIosCloseCircle className="text-xl text-red-500 hover:text-red-700" />
          </button>
        </div>
      )}
    </BaseModal>
  );
};

export default DeliveryScheduleViewModal;
