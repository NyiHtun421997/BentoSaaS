const formatYmdJp = (ymd: string | undefined) => {
  if (!ymd) return "-";
  const [y, m, d] = ymd.split("-").map((x) => Number(x));
  if (!y || !m || !d) return ymd;
  return `${y}年${m}月${d}日`;
};

export default formatYmdJp;
