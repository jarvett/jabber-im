export function chatTime(value: string, short = true): string {
  const date = new Date(Number(value)),
    now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate() - 1,
  );
  const monday = new Date(today);
  monday.setDate(today.getDate() - (today.getDay() || 7) + 1);
  const pad = (n: number) => String(n).padStart(2, "0");
  const clock = `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  if (date.getTime() > today.getTime()) return clock;
  const day =
    date.getTime() > yesterday.getTime()
      ? "昨天"
      : date.getTime() > monday.getTime()
        ? ["周日", "周一", "周二", "周三", "周四", "周五", "周六"][
            date.getDay()
          ]
        : `${String(date.getFullYear()).slice(-2)}/${pad(date.getMonth() + 1)}/${pad(date.getDate())}`;
  return short ? day : `${day} ${clock}`;
}
