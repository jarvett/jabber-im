<template>
  <div class="chat-wrap">
    <chat-dialogue :visible.sync="showDialogue" @select="selectChat" :dialogue-list="msgList" v-drag></chat-dialogue>

    <chat-main-window :visible.sync="showMainWindow" :income-message="msg" :history-list="historyList"
      @enter="sendMessage" @file="sendFile" v-drag></chat-main-window>
  </div>
</template>

<script>

import { isEmpty, jsonParse, WS } from '../../assets/utils';

import ChatDialogue from "../../components/chat-dialogue.vue";
import ChatMainWindow from "../../components/chat-main-window.vue";

export default {
  name: "chat",
  components: { ChatMainWindow, ChatDialogue },
  data() {
    return {
      token: '',
      msgList: [],
      msg: {},
      historyList: [],
      showDialogue: true,
      showMainWindow: false,
      channelId: 0,//选中的会话ID
    }
  },
  methods: {
    isEmpty,
    getProfile() {
      this.axios.get("/im/getMyProfile").then((res) => {
        if (res.code === 0) {
          this.avatarUrl = res.profile.avatarUrl;
          this.nickName = res.profile.nickName;
          this.uniqueId = res.profile.uniqueId;
          this.intro = res.profile.intro;
          this.$store.commit('addProfile', res.profile);
          this.$store.commit('setSid', res.profile.sid)
        } else {
          this.$toast.show(res.msg);
        }
      });
    },
    getDialogList() {//获取会话列表
      this.axios.get('/im/getMyDialogueList').then(res => {
        this.msgList = res.dialogueList.map(item => {
          // 做字段转换映射
          return {
            id: item.id,
            name: item.name,
            dept: item.brief,
            img: item.avatar,
            time: item.lastTime,
            num: item.unreadCount,
            channelId: item.channelId,
          }
        });
      })
    },
    selectChat(item) {
      this.showMainWindow = true;
      this.channelId = item.channelId;
      this.getProfileList().then(() => {
        this.getDialogHistory();
      });
    },
    getProfileList() {
      return this.axios
        .get('/im/getSubscriberModelListByChannelId', {
          params: { channelId: this.channelId }
        })
        .then((res => {
          if (res.code === 0) {
            res.subscriberList.forEach(profile => {
              this.$store.commit('addProfile', profile)
            })
          }
        }))
    },
    getDialogHistory() {//获取会话聊天记录
      this.axios.get('/im/getMsgListByChannelId', { params: { channelId: this.channelId } }).then(res => {
        // TODO 转换数据成组件需要的结构
        this.historyList = res.channelMsgList.map(item => {
          let profile = this.$store.state.profileMap[item.senderId];
          return {
            id: item.id,
            createTime: item.time,
            mine: item.senderId === this.$store.state.sid,
            channelId: item.channelId,
            pushUser: JSON.stringify({ nickName: profile.nickName, avatarUrl: profile.avatarUrl }),
            contextType: item.content.type,
            context: item.content.text,
            imageUrl: item.content.imageUrl,
          }
        }).reverse();
      })
    },
    sendRaw(messageContent = '', messageType = 1) {
      if (!messageContent || !this.channelId) {
        return false;
      }
      this.axios
        .post(
          "/im/sendMsgToChannel",
          this.qs.stringify({
            channelId: this.channelId,
            type: "text",
            text: messageContent,
          })
        )
        .then((res) => {
          if (res.code === 0) {

          } else {
            this.$toast.show(res.msg || "出现错误");
          }
        });
    },
    sendMessage(msg) {//发送文本消息
      this.sendRaw(msg)
    },
    sendFile(file) {
      file = file[0];
      let type = file.type;
      let messageType = 1;//消息类型 1-文本 2-图片 3-视频
      if (type.indexOf('image') === 0) {
        messageType = 2;
      } else if (type.indexOf('video') === 0) {
        messageType = 3;
      } else {
        console.log('不支持的文件类型');
        return;
      }
      var formData = new FormData();
      formData.append("file", file);
      this.axios.post('/file/uploadOne', formData).then(res => {
        if (res.data.url) {
          this.sendRaw(res.data.url, messageType)
        }
      })
    },
    handleMessage(msg) {
      let item = jsonParse(msg);
      let profile = this.$store.state.profileMap[item.senderId];
      this.msg = {
        id: item.id,
        createTime: item.time,
        mine: item.senderId === this.$store.state.sid,
        channelId: item.channelId,
        pushUser: JSON.stringify({ nickName: profile.nickName, avatarUrl: profile.avatarUrl }),
        contextType: item.content.type,
        context: item.content.text,
        imageUrl: item.content.imageUrl,
      };
    }
  },
  mounted() {
    this.getProfile();
    this.getDialogList();
    let url = location.origin.replace("http", 'ws') + '/im/webSocket';
    url = url.replace("http", 'ws');
    WS.connectServer(url, this.$store.state.token, this.handleMessage)
  },
}

</script>

<style scoped>
.chat-wrap {
  width: 60%;
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}
</style>
