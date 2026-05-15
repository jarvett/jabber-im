<template>
  <div class="main-window-wrap" v-if="visible">
    <chat-icon class="btn-close" type="icon-times" @click="close()"></chat-icon>
    <div class="main-window-title" draggable="false">官方客服</div>
    <chat-window width="798" height="580" :taleList="list" @enter="bindEnter" @clickTalk="talkEvent" v-model="inputMsg"
      :toolConfig="tool" :config="chatConfig"></chat-window>
  </div>
</template>

<script>

import { getEmojis } from 'wechat-emoji-parser'
import { formatChatMainWindowTime, jsonParse } from '../assets/utils'
import ChatIcon from "./assembly/chat-icon.vue";
import ChatWindow from "./assembly/chat-window.vue";

let emojis = getEmojis({ size: 24 });

export default {
  name: "chat-main-window",
  components: { ChatWindow, ChatIcon },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    historyList: {
      type: Array,
      default: () => ([])
    },
    incomeMessage: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      inputMsg: undefined,
      list: [],
      tool: {
        showEmoji: false,
        callback: this.toolEvent
      },
      chatConfig: {
        historyConfig: {
          show: true,
          tip: '加载更多',
          callback: this.bindLoadHistory
        }
      },
      emojis: emojis,
    }
  },
  methods: {
    bindEnter(e) {
      this.$emit('enter', e);
    },
    toolEvent(type, obj) {
      // console.log('tools', type, obj)
      this.$emit(type, obj)
    },
    talkEvent(play) {
      // console.log(play)
    },
    selectEmoji(code) {
      this.inputMsg += code;
    },
    bindLoadHistory() {
      console.log("加载更多回调")
    },
    close() {
      this.$emit('update:visible', false);
    },
    convertToText(item) {
      let contextType = item.contextType || 'text';//消息类型 text-纯文本 image-纯图片 event-事件
      switch (contextType) {
        case 'image':
          return { "text": "<img data-src='" + item.imageUrl + "'/>" }
        case 'video':
          return { "text": "<video data-src='" + item.imageUrl + "' />" }
        default:
          return { "text": item.context }
      }
    },
    convertItem(item) {
      let pusher = jsonParse(item.pushUser);
      return {
        name: pusher.nickName,
        img: pusher.avatarUrl,
        date: formatChatMainWindowTime(item.createTime),
        text: this.convertToText(item),
        mine: item.mine
      }
    },
    convertToChat(list) {
      return list.map(item => {
        return this.convertItem(item);
      });
    }
  },
  mounted() {
  },
  watch: {
    historyList(list) {
      this.list = this.convertToChat(list);
    },
    incomeMessage(message) {
      let im = this.convertItem(message);
      this.list.push(im)
    }
  }
}

// 历史消息列表的数据结构
function getInitData() {
  const img = 'https://www.baidu.com/img/flexible/logo/pc/result.png'
  const list = [
    {
      "date": "2020/04/25 21:19:07",
      "text": { "text": "起床不" },
      "mine": false,
      "name": "留恋人间不羡仙",
      "img": "/image/one.jpeg"
    },
    {
      "date": "2020/04/25 21:19:07",
      "text": { "text": "<audio data-src='https://www.w3school.com.cn/i/horse.mp3'/>" },
      "mine": false,
      "name": "只盼流星不盼雨",
      "img": "/image/two.jpeg"
    },
    {
      "date": "2020/04/25 21:19:07",
      "text": { "text": "<img data-src='" + img + "'/>" },
      "mine": false,
      "name": "只盼流星不盼雨",
      "img": "/image/two.jpeg"
    },
    {
      "date": "2020/04/25 21:19:07",
      "text": { "text": "<img data-src='/image/three.jpeg'/>" },
      "mine": false,
      "name": "只盼流星不盼雨",
      "img": "/image/two.jpeg"
    },
    {
      "date": "2020/04/16 21:19:07",
      "text": { "text": "<video data-src='https://www.w3school.com.cn/i/movie.mp4' />" },
      "mine": true,
      "name": "JwChat",
      "img": "/image/three.jpeg"
    },
    {
      "date": "2021/03/02 13:14:21",
      "mine": false,
      "name": "留恋人间不羡仙",
      "img": "/image/one.jpeg",
      "text": {
        system: {
          title: '在接入人工前，智能助手将为您首次应答。',
          subtitle: '猜您想问:',
          content: [
            {
              id: `system1`,
              text: '组件如何使用'
            },
            {
              id: `system2`,
              text: '组件参数在哪里查看'
            },
            {
              id: 'system',
              text: '我可不可把组件用在商业'
            }
          ]
        }
      }
    },
    {
      "date": "2020/04/25 21:19:07",
      "text": {
        "text": "<i class='el-icon-document-checked' style='font-size:2rem;'/>",
        "subLink": {
          "text": "a.txt",
          "prop": {
            underline: false
          }
        },
      },
      "mine": false,
      "name": "留恋人间不羡仙",
      "img": "../image/one.jpeg"
    },
  ];
  return list
}
</script>

<style lang="less">
* {
  box-sizing: border-box;
}

@media (-webkit-max-device-pixel-ratio: 1) {
  ::-webkit-scrollbar {
    width: 4px;
    height: 4px;
  }

  ::-webkit-scrollbar-thumb {
    background-color: #c2c2c2;
    background-clip: padding-box;
    min-height: 28px;
  }

  ::-webkit-scrollbar-thumb:hover {
    background-color: #A0A0A0;
  }
}

.main-window-wrap {
  position: absolute;
  width: 800px;
  height: 600px;
  top: 10%;
  left: calc(10% + 300px);
  ;
  overflow: hidden;
  background-color: #e3e3e3;
  border-radius: 32px;
  border: 1px solid #e3e3e3;

  .btn-close {
    position: absolute;
    right: 0;
    top: 0;
    margin-top: 10px;
    margin-right: 10px;
    cursor: pointer;
  }

  .main-window-title {
    padding: 10px 0;
    font-size: 14px;
    cursor: pointer;
    background-color: #d9d9d9;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }

  .tools {
    i {
      padding-left: 6px;
      font-size: 20px;
      color: #888a91;
    }
  }

}
</style>
