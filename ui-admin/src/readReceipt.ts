import { onScopeDispose } from "vue";
import { api, perform } from "./api";
import type { Dialogue } from "./types";

/** 合并同一窗口的重复确认，只有服务端成功清零才记为已确认。 */
export function useReadReceipt(
  channel: () => string,
  active: () => boolean,
  updated: (model: Dialogue) => void,
  newest: () => string,
) {
  let pending: { channelId: string; messageId: string } | undefined;
  let running = false,
    disposed = false;
  const acknowledged = new Map<string, string>();
  onScopeDispose(() => {
    disposed = true;
  });
  return async function confirm(messageId: string) {
    const channelId = channel();
    if (!active() || disposed || acknowledged.get(channelId) === messageId)
      return;
    pending = { channelId, messageId };
    if (running) return;
    running = true;
    try {
      while (pending && !disposed) {
        const current = pending;
        pending = undefined;
        if (
          !active() ||
          channel() !== current.channelId ||
          acknowledged.get(current.channelId) === current.messageId
        )
          continue;
        await perform(async () => {
          const { model } = await api<{ model: Dialogue }>(
            "markRead",
            current,
            "POST",
          );
          if (disposed) return;
          if (model.unreadCount === 0)
            acknowledged.set(current.channelId, current.messageId);
          if (channel() === current.channelId && newest() === current.messageId)
            updated(model);
        });
      }
    } finally {
      running = false;
    }
  };
}

/** 后端同毫秒消息按数字字符串 ID 排序，不能转 Number 丢失精度。 */
export function compareMessages(
  a: { time: string; id: string },
  b: { time: string; id: string },
) {
  return (
    Number(a.time) - Number(b.time) ||
    a.id.length - b.id.length ||
    a.id.localeCompare(b.id)
  );
}
