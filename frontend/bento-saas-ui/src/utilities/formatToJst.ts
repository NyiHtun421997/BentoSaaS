const formatToJapaneseDateTime = (iso: string, includeTime: boolean = true) => {
  const date = new Date(iso);

  const parts = new Intl.DateTimeFormat("ja-JP", {
    timeZone: "Asia/Tokyo",
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).formatToParts(date);

  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? "";

  if (includeTime) {
    return `${get("year")}年${get("month")}月${get("day")}日 ${get("hour")}:${get("minute")}`;
  } else {
    return `${get("year")}年${get("month")}月${get("day")}日`;
  }
};

export default formatToJapaneseDateTime;
