import { test, expect } from "@playwright/test";
const prefix = "h5_im_";
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
let socketReady = false;

test.beforeEach(async ({ page }) => {
  forms = [];
  socketReady = false;
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
    socketReady = true;
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
  for (const digit of "13800138000")
    await page.getByRole("button", { name: digit, exact: true }).click();
  await page.getByRole("button", { name: "确认", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "请输入验证码" }),
  ).toBeVisible();
  for (const digit of "123456")
    await page.getByRole("button", { name: digit, exact: true }).click();
  await page.getByRole("button", { name: "确认", exact: true }).click();
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
  await page.getByRole("textbox", { name: "消息内容" }).press("Enter");
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
    page.getByRole("textbox", { name: "消息内容" }),
  ).toBeInViewport();
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("chat.png"),
  });
});

test("群聊按重复 sidList 提交", async ({ page }) => {
  await authenticate(page);
  await page.goto("/group");
  await page.getByRole("checkbox", { name: "选择甲" }).click();
  await page.getByRole("checkbox", { name: "选择乙" }).click();
  await page.getByRole("button", { name: "创建群聊" }).click();
  await page.getByRole("textbox", { name: "设置群名称" }).fill("测试群");
  await page.getByRole("button", { name: "确定", exact: true }).click();
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
  await page.getByRole("textbox", { name: "消息内容" }).press("Enter");
  await expect(page.getByRole("alert")).toContainText("发送失败");
  await expect(page.getByRole("textbox", { name: "消息内容" })).toHaveValue(
    "保留这条消息",
  );
});

test("推送去重、隔离其他频道和窄屏布局", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/chatbox?channelId=10");
  await expect.poll(() => socketReady).toBe(true);
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
    page.getByRole("textbox", { name: "消息内容" }),
  ).toBeInViewport();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("chat-mobile.png"),
  });
});

test("个人资料修改及退出", async ({ page }) => {
  await authenticate(page);
  await page.goto("/profile");
  await expect(page.getByText("测试用户", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "修改个性签名" }).click();
  await page.getByRole("textbox", { name: "个性签名" }).fill("新签名");
  await page.getByRole("button", { name: "确定", exact: true }).click();
  await expect.poll(() => forms.at(-1)?.get("intro")).toBe("新签名");
  await page.getByRole("button", { name: "退出", exact: true }).click();
  await expect(page).toHaveURL(/login/);
});

test("数字键盘限制、删除、清空及返回", async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 320, height: 568 });
  await page.goto("/login");
  await expect(
    page.getByRole("button", { name: "确认", exact: true }),
  ).toBeDisabled();
  for (const digit of "138001380009")
    await page.getByRole("button", { name: digit, exact: true }).click();
  const display = page.getByRole("textbox", { name: "请输入手机号" });
  await expect(display).toHaveText("13800138000");
  await page.getByRole("button", { name: "删除一位" }).click();
  await expect(display).toHaveText("1380013800");
  await page.getByRole("button", { name: "清空" }).click();
  await expect(display).toBeEmpty();
  await display.focus();
  await page.keyboard.type("abc13800138000");
  await expect(display).toHaveText("13800138000");
  await page.getByRole("button", { name: "确认", exact: true }).click();
  const code = page.getByRole("textbox", { name: "请输入验证码" });
  for (const digit of "0123456")
    await page.getByRole("button", { name: digit, exact: true }).click();
  await expect(code).toHaveText("012345");
  await page.getByRole("button", { name: "返回", exact: true }).click();
  await expect(display).toHaveText("13800138000");
  await expect(page.locator("input")).toHaveCount(0);
  await expect(
    page.getByRole("button", { name: "小数点不可用" }),
  ).toBeDisabled();
  await expect(
    page.getByRole("button", { name: "确认", exact: true }),
  ).toBeInViewport();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("number-keyboard.png"),
  });
});

test("验证码请求失败不推进步骤，登录失败保留验证码", async ({ page }) => {
  await page.goto("/login");
  for (const digit of "13800138000")
    await page.getByRole("button", { name: digit, exact: true }).click();
  await page.route(
    "**/im/getPhoneCode?*",
    (route) => route.fulfill({ json: { code: 10, msg: "验证码发送失败" } }),
    { times: 1 },
  );
  await page.getByRole("button", { name: "确认", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("验证码发送失败");
  await expect(page.getByRole("textbox", { name: "请输入手机号" })).toHaveText(
    "13800138000",
  );
  await page.getByRole("button", { name: "确认", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "请输入验证码" }),
  ).toBeVisible();
  for (const digit of "123456")
    await page.getByRole("button", { name: digit, exact: true }).click();
  await page.route("**/im/getToken?*", (route) =>
    route.fulfill({ json: { code: 10, msg: "验证码错误" } }),
  );
  await page.getByRole("button", { name: "确认", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("验证码错误");
  await expect(page.getByRole("textbox", { name: "请输入验证码" })).toHaveText(
    "123456",
  );
});

test("原版底部导航、资料菜单和图标布局", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.goto("/chat");
  await expect(page.getByRole("navigation", { name: "主导航" })).toBeVisible();
  await expect(page.locator(".app-header")).toHaveCount(0);
  await expect(page.locator(".chat-item")).toHaveCount(1);
  await expect(page.locator(".chat")).toHaveCSS(
    "background-color",
    "rgb(231, 231, 231)",
  );
  await expect(page.locator("#icon-interactive_fill")).toHaveCount(1);
  const nav = await page.locator(".tab-bar").boundingBox();
  expect(nav?.y).toBe(794);
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("chat-list.png"),
  });
  await page.getByRole("link", { name: "我的", exact: true }).click();
  await expect(page.getByText("聊天号:13800138000")).toBeVisible();
  await expect(page.getByRole("button", { name: "查找好友" })).toBeVisible();
  await expect(
    page.getByRole("button", { name: "退出", exact: true }),
  ).toBeVisible();
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("profile.png"),
  });
  await page.getByRole("button", { name: "换个马甲" }).click();
  await expect(page.getByRole("dialog", { name: "换个马甲" })).toBeVisible();
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("dialog.png"),
  });
  await page.getByRole("button", { name: "取消", exact: true }).click();
  await page.getByRole("button", { name: "随便聊聊" }).click();
  await expect(
    page.getByRole("button", { name: "刷新", exact: true }),
  ).toBeVisible();
  await expect(page.getByRole("button", { name: "撩一下" })).toHaveCount(2);
  await expect(page.locator(".tab-bar")).toHaveCount(0);
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("stranger.png"),
  });
  await page.getByRole("button", { name: "返回", exact: true }).click();
  await page.getByRole("button", { name: "发起群聊" }).click();
  await page.getByRole("checkbox", { name: "选择甲" }).click();
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("group.png"),
  });
});

test("原版聊天工具栏和表情面板", async ({ page }, testInfo) => {
  await authenticate(page);
  await page.goto("/chatbox?channelId=10");
  await expect(page.getByText("历史消息", { exact: true })).toBeVisible();
  await expect(page.locator(".tab-bar")).toHaveCount(0);
  await expect(page.locator("textarea")).toHaveCount(0);
  await expect(page.locator(".pane-header")).toHaveCount(0);
  await expect(page.locator(".toolbox")).toHaveCSS("height", "50px");
  await page.getByRole("button", { name: "表情", exact: true }).click();
  await expect(page.locator(".function-emoji")).toBeVisible();
  await expect(page.locator(".toolbox")).toHaveCSS("bottom", "150px");
  await page.getByRole("button", { name: "[OK]", exact: true }).click();
  await expect(page.getByRole("textbox", { name: "消息内容" })).toHaveValue(
    "[OK]",
  );
  await page.screenshot({
    animations: "disabled",
    path: testInfo.outputPath("emoji.png"),
  });
  await page.getByRole("textbox", { name: "消息内容" }).press("Enter");
  await expect(page.locator('.content img[alt="[OK]"]')).toBeVisible();
});

test("列表下拉刷新及聊天历史下拉加载", async ({ page }) => {
  await authenticate(page);
  let lists = 0,
    historyPages: string[] = [];
  await page.route("**/im/getMyDialogueList?*", (route) => {
    lists++;
    return route.fulfill({ json: { code: 0, dialogueList: [model] } });
  });
  await page.route("**/im/getMsgListByChannelId?*", (route) => {
    const pageNow =
      new URL(route.request().url()).searchParams.get("pageNow") || "1";
    historyPages.push(pageNow);
    return route.fulfill({
      json: {
        code: 0,
        channelMsgList:
          pageNow === "1"
            ? Array.from({ length: 10 }, (_, i) => ({
                id: String(i + 1),
                channelId: "10",
                senderId: "2",
                time: String(1700000000000 + i),
                content: { type: "text", text: "记录" + i },
              }))
            : [],
      },
    });
  });
  await page.goto("/chat");
  await expect.poll(() => lists).toBe(1);
  await page.locator(".chat").evaluate((el) => {
    for (const [type, y] of [
      ["touchstart", 20],
      ["touchmove", 100],
      ["touchend", 100],
    ] as const) {
      const touches =
        type === "touchend"
          ? []
          : [new Touch({ identifier: 1, target: el, clientY: y })];
      el.dispatchEvent(new TouchEvent(type, { touches, bubbles: true }));
    }
  });
  await expect.poll(() => lists).toBe(2);
  await page.getByRole("button", { name: /测试好友/ }).click();
  await expect(page.getByText("记录0", { exact: true })).toBeVisible();
  await page.locator(".msgbox").evaluate((el) => {
    el.scrollTop = 0;
  });
  await page.locator(".msgbox").evaluate((el) => {
    for (const [type, y] of [
      ["touchstart", 20],
      ["touchmove", 100],
      ["touchend", 100],
    ] as const) {
      const touches =
        type === "touchend"
          ? []
          : [new Touch({ identifier: 1, target: el, clientY: y })];
      el.dispatchEvent(new TouchEvent(type, { touches, bubbles: true }));
    }
  });
  await expect.poll(() => historyPages.includes("2")).toBe(true);
  await page.getByRole("button", { name: "返回", exact: true }).click();
  await expect(page).toHaveURL(/chat$/);
});

test("资料弹窗取消不提交，短屏弹窗和工具栏不溢出", async ({ page }) => {
  await authenticate(page);
  await page.setViewportSize({ width: 320, height: 568 });
  await page.goto("/profile");
  await page.getByRole("button", { name: "修改个性签名" }).click();
  await page.getByRole("textbox", { name: "个性签名" }).fill("不保存");
  await page.getByRole("button", { name: "取消", exact: true }).click();
  expect(forms).toHaveLength(0);
  await page.getByRole("button", { name: "发起群聊" }).click();
  await page.getByRole("button", { name: "创建群聊" }).click();
  await expect(page.getByRole("alert")).toContainText("请至少选择2个用户");
  await expect(
    page.getByRole("button", { name: "返回", exact: true }),
  ).toBeInViewport();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
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
  await expect(page.locator(".chat-item-icon-num")).toHaveCount(0);
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
