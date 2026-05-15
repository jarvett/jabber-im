<template>
  <div class="chat">
    <scroller ref="my_scroller" :on-refresh="refresh" :on-infinite="infinite">
      <div class="chat-wrap">
        <div
          class="chat-item"
          v-for="item in dialogueList"
          :key="item.id"
          @click="chatFrom(item)"
        >
          <div class="chat-item-left">
            <avatar
              class="chat-item-avatar"
              username="匿名"
              :src="item.avatar"
              :rounded="false"
              :inline="true"
            ></avatar>
            <div class="chat-item-avatar-right">
              <div class="chat-item-name">{{ item.name }}</div>
              <div class="chat-item-brief">{{ item.brief }}</div>
            </div>
          </div>
          <div class="chat-item-right">
            <div class="chat-item-time">
              {{ formatChatDialogueTime(item.lastTime) }}
            </div>
            <div class="chat-item-icon">
              <div class="chat-item-icon-num" v-if="item.unreadCount">
                {{ item.unreadCount }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </scroller>
  </div>
</template>

<script>
import Avatar from '../components/chat/Avatar.vue';
import { formatChatDialogueTime,findElem } from '../configs/utils';
import bus from '../configs/vbus'
export default {
  components: {
    Avatar,
  },
  data() {
    return {
      pageIndex: 1,
      dialogueList: [],
      loadEnd: false,
    };
  },
  mounted(){
    bus.$on('ws_msg',this.handleMsg);
  },
  destroyed(){
    bus.$off('ws_msg')
  },
  methods: {
    formatChatDialogueTime,
    refresh(done) {
      this.pageIndex = 1;
      this.loadEnd = false;
      this.getMyDialogueList(done);
    },
    infinite(done) {
      if (!this.loadEnd) {
        this.getMyDialogueList(done);
      }else{
          done(true)
      }
    },
    getMyDialogueList(done) {
      return this.axios
        .get("/im/getMyDialogueList", {
          params: { pageNow: this.pageIndex },
          noLoading: true,
        })
        .then((res) => {
          if (this.pageIndex === 1) {
            this.dialogueList = [];
          }
          this.pageIndex++;
          if (res.code === 0) {
            if (res.dialogueList.length === 10) {
              done();
            } else {
              this.loadEnd = true;
              done(true);
            }
            this.dialogueList = this.dialogueList.concat(res.dialogueList);
          } else {
            this.$toast.show(res.msg);
          }
        }).catch(()=>{
            this.loadEnd = true;
        });
    },
    chatFrom(item) {
      let channelId = item.channelId;
      this.$router.push("/chatbox?channelId=" + channelId);
      let currentChatBoxName = item.name;
      this.$store.commit('setChatBoxName',currentChatBoxName);
    },
    handleMsg(msg){
      this.axios.get('/im/getDialogueByChannelId',{params:{channelId:msg.channelId}}).then(res=>{
        if(res.code === 0){
          let index = findElem(this.dialogueList,'channelId',msg.channelId);
          if(index > -1){
            this.$set(this.dialogueList, index, res.model)
          }else{
            this.dialogueList.unshift(res.model);
          }
        }
      })
    },
  },
};
</script>

<style>
.chat {
  width: 100%;
  height: 100%;
}
.chat ._v-container {
  background-color: #e7e7e7;
}
</style>

<style scoped>
.chat-item {
  display: flex;
  padding: 10px;
  background-color: #ffffff;
  margin-bottom: 3px;
  max-height: 60px;
  overflow: hidden;
  align-items: center;
}

.chat-item-left {
  width: 0;
  flex: 1;
  display: flex;
  height: 50px;
}

.chat-item-avatar {
  margin-right: 10px;
}

.chat-item-avatar-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  max-height: 60px;
  overflow: hidden;
}

.chat-item-name {
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-brief {
  font-size: 70%;
  color: #999999;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-right {
  width: 60px;
  height: 50px;
  min-width: 60px;
  max-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

.chat-item-time {
  font-size: 80%;
}

.chat-item-icon {
  font-size: 50%;
}

.chat-item-icon-num {
  color: #ffffff;
  background-color: red;
  padding: 2px 4px;
  border-radius: 50%;
}
</style>
