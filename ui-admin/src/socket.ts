import { watch, onScopeDispose } from "vue";
import { useIntervalFn } from "@vueuse/core";
import { useSession } from "./store";
import type { Message } from "./types";

/** 根组件持有唯一连接；切换页面不断线，退出登录立即释放连接。 */
export function useChatSocket() {
  const session = useSession();
  let socket: WebSocket | undefined;
  function connect() {
    if (
      !session.token ||
      socket?.readyState === WebSocket.OPEN ||
      socket?.readyState === WebSocket.CONNECTING
    )
      return;
    const url = new URL("/im/webSocket", location.href);
    url.protocol = location.protocol === "https:" ? "wss:" : "ws:";
    url.searchParams.set("token", session.token);
    const current = new WebSocket(url);
    socket = current;
    current.onopen = () => {
      if (socket === current) session.connected = true;
    };
    current.onclose = () => {
      if (socket === current) session.connected = false;
    };
    current.onmessage = (event) => {
      if (socket !== current || event.data === "pong") return;
      try {
        const message = JSON.parse(String(event.data)) as Message;
        if (message.id && message.channelId && message.content)
          session.latest = message;
      } catch {
        session.notice = "收到无法识别的消息";
      }
    };
  }
  function disconnect() {
    const previous = socket;
    socket = undefined;
    previous?.close();
    session.connected = false;
  }
  watch(
    () => session.token,
    () => {
      disconnect();
      connect();
    },
    { immediate: true },
  );
  useIntervalFn(() => {
    if (socket?.readyState === WebSocket.OPEN) socket.send("ping");
    else connect();
  }, 15000);
  onScopeDispose(disconnect);
}
