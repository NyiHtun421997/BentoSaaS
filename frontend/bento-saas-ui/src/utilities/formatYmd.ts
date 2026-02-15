import { format, isValid, parseISO } from "date-fns";

const YMD_REGEX = /^\d{4}-\d{2}-\d{2}$/;

const toDate = (value: Date | string | number): Date | null => {
  if (value instanceof Date) return isValid(value) ? value : null;

  if (typeof value === "number") {
    const date = new Date(value);
    return isValid(date) ? date : null;
  }

  if (YMD_REGEX.test(value)) {
    return parseISO(value);
  }

  const date = new Date(value);
  return isValid(date) ? date : null;
};

const formatYmd = (value: Date | string | number): string => {
  if (typeof value === "string" && YMD_REGEX.test(value)) return value;

  const date = toDate(value);
  if (!date) return "";

  return format(date, "yyyy-MM-dd");
};

export default formatYmd;
