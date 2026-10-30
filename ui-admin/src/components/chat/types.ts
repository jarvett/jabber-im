export interface ChatDialogueItem {
  id: string;
  channelId: string;
  name: string;
  avatar: string;
  brief: string;
  lastTime: string;
  unreadCount: number;
}
export interface ChatProfile {
  nickName: string;
  avatarUrl: string;
}
export interface ChatContent {
  type: "text" | "image" | "event";
  text?: string;
  imageUrl?: string;
  eventKey?: string;
}
export interface ChatMessage {
  id: string;
  channelId: string;
  senderId: string;
  time: string;
  content: ChatContent;
}
export type ChatSend = { type: "text"; text: string };
export interface WindowPosition {
  x: number;
  y: number;
}
