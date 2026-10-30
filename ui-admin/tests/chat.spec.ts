import { test, expect } from "@playwright/test";
const prefix = "admin_im_";
const sid = "9007199254740993";
const model = {
  id: "1",
  channelId: "10",
  type: 0,
  name: "测试好友",
  avatar: "",
  brief: "你好",
  lastTime: "1700000000000",
  unreadCount: 2,
};
const profile = {
  sid,
  nickName: "测试用户",
  avatarUrl: "",
  uniqueId: "13800138000",
  intro: "签名",
  createTime: "1700000000000",
};
let forms: URLSearchParams[];
let push: (message: string) => void;

test.beforeEach(async ({ page }) => {
  forms = [];
  let messages = [
    {
      id: "1",
      channelId: "10",
      senderId: "2",
      time: "1700000000000",
      content: { type: "text", text: "历史消息" },
    },
  ];
  await page.routeWebSocket("**/im/webSocket?*", (ws) => {
    push = (message) => ws.send(message);
    ws.onMessage((message) => {
      if (message === "ping") ws.send("pong");
    });
  });
  await page.route("**/im/**", async (route) => {
    const url = new URL(route.request().url()),
      path = url.pathname.split("/").pop();
    const form = new URLSearchParams(route.request().postData() || "");
    if (route.request().method() === "POST" && path !== "markRead")
      forms.push(form);
    let data: object = {};
    switch (path) {
      case "markRead":
        data = { model: { ...model, unreadCount: 0 } };
        break;
      case "getPhoneCode":
        data = { phone: "138****8000" };
        break;
      case "getToken":
        data = { sid, token: "test-token", expiresAtStamp: "1800000000000" };
        break;
      case "getMyProfile":
        data = { profile };
        break;
      case "getMyDialogueList":
        data = { dialogueList: [model] };
        break;
      case "getDialogueByChannelId":
        data = { model };
        break;
      case "getSubscriberModelListByChannelId":
        data = {
          subscriberList: [
            profile,
            { ...profile, sid: "2", nickName: "测试好友" },
          ],
        };
        break;
      case "getRandomStranger":
        data = {
          subscriberList: [
            { ...profile, sid: "2", nickName: "甲" },
            { ...profile, sid: "3", nickName: "乙" },
          ],
        };
        break;
      case "createPrivateDialogue":
      case "createGroupDialogue":
        data = { model };
        break;
      case "getMsgListByChannelId":
        data = {
          channelMsgList:
            url.searchParams.get("pageNow") === "2"
              ? []
              : [...messages].reverse(),
        };
        break;
      case "sendMsgToChannel":
        messages.push({
          id: "2",
          channelId: "10",
          senderId: sid,
          time: "1700000000001",
          content: { type: "text", text: form.get("text") || "" },
        });
        break;
    }
    await route.fulfill({ json: { code: 0, msg: null, ...data } });
  });
});

async function authenticate(page: import("@playwright/test").Page) {
  await page.addInitScript(
    ({ prefix, sid }) => {
      localStorage.setItem(prefix + "token", "test-token");
      localStorage.setItem(prefix + "sid", sid);
    },
    { prefix, sid },
  );
}

test("登录保留字符串 ID 和旧存储键", async ({ page }) => {
  await page.goto("/chat");
  await expect(page).toHaveURL(/login/);
  await page.getByPlaceholder("请输入手机号").fill("13800138000");
  await page.getByRole("button", { name: "获取验证码" }).click();
  await expect(page.getByRole("button", { name: /秒后重发/ })).toBeDisabled();
  await page.getByPlaceholder("请输入6位验证码").fill("123456");
  await page.getByRole("button", { name: "登录", exact: true }).click();
  await expect(page).toHaveURL(/chat$/);
  await expect(page.getByText("测试好友", { exact: true })).toBeVisible();
  expect(
    await page.evaluate((key) => localStorage.getItem(key), prefix + "sid"),
  ).toBe(sid);
});

test("直接打开会话、发送中文表单、恢复消息", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.goto("/chatbox?channelId=10");
  await expect(page.getByText("历史消息", { exact: true })).toBeVisible();
  await page.getByRole("textbox", { name: "消息内容" }).fill("你好 & Vue3");
  await page.getByRole("button", { name: "发送", exact: true }).click();
  await expect(page.getByText("你好 & Vue3", { exact: true })).toBeVisible();
  expect(forms.at(-1)?.get("text")).toBe("你好 & Vue3");
  expect(forms.at(-1)?.get("channelId")).toBe("10");
  await expect(page.getByRole("textbox", { name: "消息内容" })).toHaveValue("");
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.getByRole("textbox", { name: "消息内容" }).focus();
  await expect(
    page.getByRole("button", { name: "发送", exact: true }),
  ).toBeInViewport();
  await page.screenshot({ path: testInfo.outputPath("chat.png") });
});

test("点击发送和 Enter 发送后保持输入焦点", async ({ page }) => {
  await authenticate(page);
  let release: () => void = () => {};
  const pending = new Promise<void>((resolve) => { release = resolve; });
  await page.route("**/im/sendMsgToChannel", async (route) => {
    await pending;
    await route.fallback();
  });
  await page.goto("/chatbox?channelId=10");
  const input = page.getByRole("textbox", { name: "消息内容" });
  await input.fill("第一条消息");
  await page.getByRole("button", { name: "发送", exact: true }).click();
  await expect(input).toBeFocused();
  await expect(input).not.toBeEditable();
  release();
  await expect(input).toBeEditable();
  await expect(input).toHaveValue("");
  await expect(input).toBeFocused();
  // 不重新点击输入框，直接输入并发送下一条。
  await page.keyboard.insertText("第二条消息");
  await page.keyboard.press("Enter");
  await expect.poll(() => forms.length).toBe(2);
  expect(forms[1]?.get("text")).toBe("第二条消息");
  await expect(input).toBeEditable();
  await expect(input).toHaveValue("");
  await expect(input).toBeFocused();
});

test("群聊按重复 sidList 提交", async ({ page }) => {
  await authenticate(page);
  await page.goto("/group");
  await page.getByRole("checkbox", { name: "选择甲" }).check();
  await page.getByRole("checkbox", { name: "选择乙" }).check();
  await page.getByPlaceholder("群聊名称（可选）").fill("测试群");
  await page.getByRole("button", { name: "创建群聊（2）" }).click();
  await expect(page).toHaveURL(/chatbox\?channelId=10/);
  expect(forms[0]?.getAll("sidList")).toEqual(["2", "3"]);
  expect(forms[0]?.get("groupName")).toBe("测试群");
});

test("令牌过期跳转登录并清理状态", async ({ page }) => {
  await authenticate(page);
  await page.route("**/im/getMyDialogueList?*", (route) =>
    route.fulfill({ json: { code: 666, msg: "已超时,请重新登录" } }),
  );
  await page.goto("/chat");
  await expect(page).toHaveURL(/login/);
  await expect(page.getByRole("alert")).toContainText("已超时");
  expect(
    await page.evaluate((key) => localStorage.getItem(key), prefix + "token"),
  ).toBe("");
});

test("业务失败保留输入并显示错误", async ({ page }) => {
  await authenticate(page);
  await page.route("**/im/sendMsgToChannel", (route) =>
    route.fulfill({ json: { code: 10, msg: "发送失败" } }),
  );
  await page.goto("/chatbox?channelId=10");
  await page.getByRole("textbox", { name: "消息内容" }).fill("保留这条消息");
  await page.getByRole("button", { name: "发送", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("发送失败");
  await expect(page.getByRole("textbox", { name: "消息内容" })).toHaveValue(
    "保留这条消息",
  );
  await expect(page.getByRole("textbox", { name: "消息内容" })).toBeFocused();
});

test("推送去重、隔离其他频道和窄屏布局", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/chatbox?channelId=10");
  await expect(page.getByText("已连接", { exact: true })).toBeVisible();
  await expect(page.getByText("历史消息", { exact: true })).toBeVisible();
  const message = {
    id: "3",
    channelId: "10",
    senderId: "2",
    time: "1700000000002",
    content: { type: "text", text: "实时推送" },
  };
  push(JSON.stringify(message));
  push(JSON.stringify(message));
  await expect(page.getByText("实时推送", { exact: true })).toHaveCount(1);
  push(
    JSON.stringify({
      ...message,
      id: "4",
      channelId: "20",
      content: { type: "text", text: "别的频道" },
    }),
  );
  await expect(page.getByText("别的频道", { exact: true })).toHaveCount(0);
  await page.getByRole("textbox", { name: "消息内容" }).focus();
  await expect(
    page.getByRole("button", { name: "发送", exact: true }),
  ).toBeInViewport();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.screenshot({ path: testInfo.outputPath("chat-mobile.png") });
});

test("页面导航固定，窗口操作只出现在聊天页", async ({ page }) => {
  await authenticate(page);
  await page.goto("/chat");
  const nav = page.getByRole("navigation", { name: "页面导航" });
  const names = ["聊天", "随便聊聊", "发起群聊", "我的"];
  for (const name of ["我的", "随便聊聊", "发起群聊", "聊天"]) {
    await nav.getByRole("link", { name, exact: true }).click();
    await expect(nav.getByRole("link")).toHaveText(names);
    await expect(nav.getByRole("button")).toHaveCount(0);
    await expect(nav.getByRole("link", { name, exact: true })).toHaveClass(/router-link-active/);
    await expect(page.getByRole("group", { name: "聊天窗口操作" })).toHaveCount(name === "聊天" ? 1 : 0);
  }
  await expect(page.getByRole("button", { name: "打开聊天窗口", exact: true })).toBeDisabled();
});

test("个人资料修改及退出", async ({ page }) => {
  await authenticate(page);
  await page.goto("/profile");
  await expect(page.getByText("测试用户", { exact: true })).toBeVisible();
  await page.getByLabel("个性签名").fill("新签名");
  await page.getByRole("button", { name: "保存签名" }).click();
  await expect.poll(() => forms.at(-1)?.get("intro")).toBe("新签名");
  await page.getByRole("button", { name: "退出登录" }).click();
  await expect(page).toHaveURL(/login/);
});

test("会话列表和聊天窗口可独立拖动、关闭、重开", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.goto("/chat");
  const list = page.getByRole("region", { name: "会话列表", exact: true });
  await list.getByRole("button", { name: /测试好友/ }).click();
  const chat = page.getByRole("region", { name: "测试好友", exact: true });
  await expect(chat.getByText("历史消息", { exact: true })).toBeVisible();
  await expect(page).toHaveURL(/chat$/);
  const listBefore = (await list.boundingBox())!,
    chatBefore = (await chat.boundingBox())!;
  await drag(page, list.locator(".window-handle"), 50, 50);
  const listAfter = (await list.boundingBox())!;
  expect(listAfter.x).toBeCloseTo(listBefore.x + 50, 0);
  expect(listAfter.y).toBeCloseTo(listBefore.y + 50, 0);
  expect((await chat.boundingBox())!.x).toBe(chatBefore.x);
  await drag(page, chat.locator(".window-handle"), -100, 60);
  expect((await chat.boundingBox())!.x).toBeCloseTo(chatBefore.x - 100, 0);
  expect((await list.boundingBox())!.x).toBe(listAfter.x);
  // 在输入区域选择文字不会拖动窗口。
  await chat.getByRole("textbox", { name: "消息内容" }).fill("保留草稿");
  const beforeInput = (await chat.boundingBox())!;
  await drag(page, chat.getByRole("textbox", { name: "消息内容" }), 30, 0);
  expect((await chat.boundingBox())!.x).toBe(beforeInput.x);
  await chat.getByRole("button", { name: "关闭测试好友" }).click();
  await expect(chat).toBeHidden();
  await expect(list).toBeVisible();
  await page.getByRole("button", { name: "打开聊天窗口", exact: true }).click();
  await expect(chat).toBeVisible();
  expect((await chat.boundingBox())!.x).toBe(beforeInput.x);
  await expect(chat.getByRole("textbox", { name: "消息内容" })).toHaveValue(
    "保留草稿",
  );
  await list.locator(".window-handle").click({ position: { x: 20, y: 20 } });
  expect(
    Number(await list.evaluate((el) => getComputedStyle(el).zIndex)),
  ).toBeGreaterThan(
    Number(await chat.evaluate((el) => getComputedStyle(el).zIndex)),
  );
  await list.getByRole("button", { name: "关闭会话列表" }).click();
  await expect(list).toBeHidden();
  await expect(chat).toBeVisible();
  await page.getByRole("button", { name: "会话列表", exact: true }).click();
  await expect(list).toBeVisible();
  await page.screenshot({ path: testInfo.outputPath("floating-windows.png") });
});

async function drag(
  page: import("@playwright/test").Page,
  handle: import("@playwright/test").Locator,
  dx: number,
  dy: number,
) {
  const rect = (await handle.boundingBox())!;
  await page.mouse.move(rect.x + 40, rect.y + 12);
  await page.mouse.down();
  await page.mouse.move(rect.x + 40 + dx, rect.y + 12 + dy, { steps: 6 });
  await page.mouse.up();
}

test("组件在无 Router、Pinia 和业务接口的宿主中可用", async ({ page }) => {
  let requests = 0;
  page.on("request", (req) => {
    if (new URL(req.url()).pathname.startsWith("/im/")) requests++;
  });
  const errors: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/tests/fixtures/embed.html");
  const list = page.getByRole("region", { name: "会话列表", exact: true });
  const chat = page.getByRole("region", { name: "嵌入聊天", exact: true });
  await list.getByRole("button", { name: /嵌入好友/ }).click();
  await expect(page.getByTestId("selection")).toHaveText("10");
  await chat.getByRole("textbox", { name: "消息内容" }).fill("独立接入成功");
  await chat.getByRole("button", { name: "发送", exact: true }).click();
  await expect(chat.getByText("独立接入成功", { exact: true })).toBeVisible();
  await expect(page.getByTestId("sent")).toContainText("独立接入成功");
  const before = (await chat.boundingBox())!;
  await drag(page, chat.locator(".window-handle"), -80, 40);
  expect((await chat.boundingBox())!.x).toBeCloseTo(before.x - 80, 0);
  await page.setViewportSize({ width: 390, height: 600 });
  await expect
    .poll(async () => {
      const b = (await chat.boundingBox())!;
      return b.x >= 0 && b.x + b.width <= 390 && b.y + b.height <= 600;
    })
    .toBe(true);
  expect(errors).toEqual([]);
  expect(requests).toBe(0);
});

test("切换会话不重置窗口位置，旧响应不串入新会话", async ({ page }) => {
  await authenticate(page);
  let release: () => void = () => {};
  const pending = new Promise<void>((resolve) => {
    release = resolve;
  });
  let oldRequest = false;
  await page.route("**/im/getMyDialogueList?*", (route) =>
    route.fulfill({
      json: {
        code: 0,
        dialogueList: [
          model,
          { ...model, id: "2", channelId: "20", name: "另一好友" },
        ],
      },
    }),
  );
  await page.route("**/im/getDialogueByChannelId?*", (route) =>
    route.fulfill({
      json: {
        code: 0,
        model:
          new URL(route.request().url()).searchParams.get("channelId") === "20"
            ? { ...model, id: "2", channelId: "20", name: "另一好友" }
            : model,
      },
    }),
  );
  await page.route("**/im/getMsgListByChannelId?*", async (route) => {
    const channelId = new URL(route.request().url()).searchParams.get(
      "channelId",
    )!;
    if (channelId === "10") {
      oldRequest = true;
      await pending;
    }
    await route.fulfill({
      json: {
        code: 0,
        channelMsgList: [
          {
            id: channelId,
            channelId,
            senderId: "2",
            time: "1700000000000",
            content: {
              type: "text",
              text: channelId === "10" ? "旧会话迟到消息" : "新会话消息",
            },
          },
        ],
      },
    });
  });
  await page.goto("/chat");
  const list = page.getByRole("region", { name: "会话列表", exact: true });
  await list.getByRole("button", { name: /测试好友/ }).click();
  await expect.poll(() => oldRequest).toBe(true);
  const oldWindow = page.getByRole("region", { name: "测试好友", exact: true });
  await drag(page, oldWindow.locator(".window-handle"), 20, 40);
  const position = (await oldWindow.boundingBox())!;
  await list.getByRole("button", { name: /另一好友/ }).click();
  const current = page.getByRole("region", { name: "另一好友", exact: true });
  await expect(current.getByText("新会话消息")).toBeVisible();
  const response = page.waitForResponse((res) =>
    res.url().includes("getMsgListByChannelId?channelId=10"),
  );
  release();
  await response;
  await expect(current.getByText("旧会话迟到消息")).toHaveCount(0);
  expect((await current.boundingBox())!.x).toBe(position.x);
  expect((await current.boundingBox())!.y).toBe(position.y);
});

test("打开会话自动确认已读，移除图片链接发送入口", async ({ page }) => {
  await authenticate(page);
  let unread = 2,
    reads = 0;
  await page.route("**/im/getMyDialogueList?*", (route) =>
    route.fulfill({
      json: { code: 0, dialogueList: [{ ...model, unreadCount: unread }] },
    }),
  );
  await page.route("**/im/markRead", (route) => {
    const body = new URLSearchParams(route.request().postData()!);
    expect(body.get("channelId")).toBe("10");
    expect(body.get("messageId")).toBe("1");
    unread = 0;
    reads++;
    return route.fulfill({
      json: { code: 0, model: { ...model, unreadCount: 0 } },
    });
  });
  await page.goto("/chat");
  await page.getByRole("button", { name: /测试好友/ }).click();
  await expect(page.getByText("历史消息", { exact: true })).toBeVisible();
  await expect.poll(() => reads).toBe(1);
  await expect(
    page.getByRole("button", { name: "图片链接", exact: true }),
  ).toHaveCount(0);
  await expect(
    page.getByRole("textbox", { name: "图片链接", exact: true }),
  ).toHaveCount(0);
  await page.goto("/chat");
  await expect(page.locator(".unread")).toHaveCount(0);
});

test("关闭聊天窗口后新消息保持未读，重开才确认", async ({ page }) => {
  await authenticate(page);
  const reads: string[] = [];
  await page.route("**/im/markRead", (route) => {
    reads.push(
      new URLSearchParams(route.request().postData()!).get("messageId")!,
    );
    return route.fulfill({
      json: { code: 0, model: { ...model, unreadCount: 0 } },
    });
  });
  await page.goto("/chatbox?channelId=10");
  await expect.poll(() => reads).toContain("1");
  await page.getByRole("button", { name: "关闭测试好友" }).click();
  push(
    JSON.stringify({
      id: "2",
      channelId: "10",
      senderId: "2",
      time: "1700000000001",
      content: { type: "text", text: "窗口关闭期间的新消息" },
    }),
  );
  await expect(
    page.getByRole("region", { name: "测试好友", exact: true }),
  ).toBeHidden();
  await expect(page.locator(".unread")).toBeVisible();
  expect(reads).not.toContain("2");
  await page
    .getByRole("region", { name: "会话列表", exact: true })
    .getByRole("button", { name: /测试好友/ })
    .click();
  await expect.poll(() => reads).toContain("2");
  await expect(page.locator(".unread")).toHaveCount(0);
});

test("隐藏页面不确认新消息，恢复可见后才清除角标", async ({ page }) => {
  await authenticate(page);
  const reads: string[] = [];
  await page.route("**/im/markRead", (route) => {
    reads.push(
      new URLSearchParams(route.request().postData()!).get("messageId")!,
    );
    return route.fulfill({
      json: { code: 0, model: { ...model, unreadCount: 0 } },
    });
  });
  await page.goto("/chatbox?channelId=10");
  await expect.poll(() => reads).toContain("1");
  await page.evaluate(() => {
    Object.defineProperty(document, "visibilityState", {
      configurable: true,
      get: () => "hidden",
    });
    document.dispatchEvent(new Event("visibilitychange"));
  });
  push(
    JSON.stringify({
      id: "2",
      channelId: "10",
      senderId: "2",
      time: "1700000000001",
      content: { type: "text", text: "隐藏时到达" },
    }),
  );
  await expect(page.getByText("隐藏时到达", { exact: true })).toBeVisible();
  expect(reads).not.toContain("2");
  await page.evaluate(() => {
    Object.defineProperty(document, "visibilityState", {
      configurable: true,
      get: () => "visible",
    });
    document.dispatchEvent(new Event("visibilitychange"));
  });
  await expect.poll(() => reads).toContain("2");
});
