import { useSession } from "./store";
import type { Result, Params } from "./types";

export function encode(params: Params): URLSearchParams {
  const form = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    for (const item of Array.isArray(value) ? value : [value])
      form.append(key, String(item));
  }
  return form;
}

/** 后端使用业务 code 和表单参数；Long ID 始终保留为字符串。 */
export async function api<T extends object = Record<string, never>>(
  path: string,
  params: Params = {},
  method: "GET" | "POST" = "GET",
): Promise<T & Result> {
  const session = useSession();
  const form = encode(params);
  try {
    const response = await fetch(
      "/im/" + path + (method === "GET" ? "?" + form : ""),
      {
        method,
        headers: { token: session.token },
        body: method === "POST" ? form : undefined,
        signal: AbortSignal.timeout(30000),
      },
    );
    if (!response.ok) throw new Error(`请求失败（${response.status}）`);
    const data = (await response.json()) as T & Result;
    if (data.code === 666) session.logout();
    if (data.code !== 0) throw new Error(data.msg || "请求失败");
    return data;
  } catch (error) {
    session.notice = error instanceof Error ? error.message : "网络请求失败";
    throw error;
  }
}

/** 事件入口统一消化已展示的错误，失败时不推进页面状态。 */
export async function perform(action: () => Promise<unknown>) {
  try {
    await action();
  } catch {
    /* api 已将错误显示到全局提示。 */
  }
}
