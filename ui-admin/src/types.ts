export interface Profile {
  sid: string;
  nickName: string;
  avatarUrl: string;
  uniqueId: string;
  intro: string;
  createTime: string;
}
export interface Dialogue {
  id: string;
  channelId: string;
  type: number;
  name: string;
  avatar: string;
  brief: string;
  lastTime: string;
  unreadCount: number;
}
export interface MessageContent {
  type: "text" | "image" | "event";
  text?: string;
  imageUrl?: string;
  eventKey?: string;
}
export interface Message {
  id: string;
  channelId: string;
  senderId: string;
  time: string;
  content: MessageContent;
}
export interface Result {
  code: number;
  msg: string | null;
}
export type Params = Record<string, string | number | string[]>;
