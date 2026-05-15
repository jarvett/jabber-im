<template>
  <div class="chatbox">
    <div class="msgbox">
      <scroller ref="my_scroller" :on-refresh="refresh" refresh-text="查看更多">
      <div class="msgbox-item" :class="$store.state.sid === item.senderId ? 'move-from-right' :'move-from-left'"
        v-for="(item) in msgList" v-bind:key="item.id">
        <div class="time">
          <span>{{formatChatMainWindowTime(item.time)}}</span>
        </div>
        <div :class="{mine:$store.state.sid === item.senderId}">
          <avatar
            class="content-avatar"
            username="匿名"
            :src="getAvatar(item)"
            :size="40"
            :rounded="false"
            :inline="true"
          ></avatar>
          <div class="content">
            <!-- {{item.content.text}} -->
            <template v-if="item.content.type === 'text'">
              <template v-for="(item, index) in getTextList(item.content.text)">
                <span class="text-box" :key="index" v-if="item.name === 'text'">{{ item.text }}</span>
                <img v-else-if="item.name === 'img'" :src="item.src" width="20px" height="20px" :key="item.id"/>
              </template>
            </template>
          </div>
        </div>
      </div>
    </scroller>
    </div>
    <div class="toolbox" :class="showFunction?'bottom150':'bottom0'">
      <div class="back" @click="back">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-undo"></use>
        </svg>
      </div>
      <div class="input">
        <input v-model="inputValue" type="text" @keyup.enter="sendText"/>
      </div>
      <div class="emoji" @click="emoji">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-emoji"></use>
        </svg>
      </div>
      <div class="other" @click="other">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-add"></use>
        </svg>
      </div>
    </div>

    <div class="function" v-show="showFunction">
        <div class="function-emoji" v-show="func === 'emoji'">
          <div v-for="item in emojiName" class="emoji" :key="item" @click="chooseEmoji(item)">
            <img :src="emojiUrl + emojiMap[item]" style="width:30px;height:30px" />
          </div>
        </div>
        <div class="function-other"></div>
    </div>
  </div>
</template>

<script>
import Avatar from '../components/chat/Avatar.vue';
import { mapState } from 'vuex';
import { isEmpty,formatChatMainWindowTime } from '../configs/utils';
import { emojiMap, emojiName, emojiUrl,decodeText} from '../configs/emoji'
import bus from '../configs/vbus'
export default {
  data() {
    return {
      channelId: "",
      pageNow: 1,
      loadEnd: false,
      msgList: [],
      inputValue: "",
      showFunction:false,
      func:'',
      emojiMap,
      emojiName,
      emojiUrl
    };
  },
  components: {
    Avatar,
  },
  computed: {
    ...mapState({
      currentChatBoxName: (state) => state.currentChatBoxName,
      getAvatar(){
        return function(item){
          let profile = this.$store.state.profileMap[item.senderId];
          return profile ? profile.avatarUrl : '';
        }
      },
      getTextList(){
        return function(text){
          return decodeText(text);
        }
      },
    }),
  },
  watch:{
    channelId(){
      this.loadEnd = false;
    }
  },
  created() {
    if (!this.$route.query.channelId || isEmpty(this.currentChatBoxName)) {
      this.$router.push("/chat");
    } else {
      this.channelId = this.$route.query.channelId;
      this.getProfileList().then(()=>{
        this.getMsgList();
      })
    }
  },
  mounted(){
    bus.$on('ws_msg',this.handleMsg);
    this.scrollToButtom();
  },
  destroyed(){
    bus.$off('ws_msg')
  },
  methods: {
    formatChatMainWindowTime,
    back() {
      this.$router.go(-1);
    },
    emoji(){
      this.showFunction = true;
      this.func = 'emoji';
    },
    chooseEmoji(item){
      this.inputValue += item;
    },
    other(){
      this.showFunction = true;
      this.func = 'other';
    },
    scrollToButtom(){
      setTimeout(() => {
        if(this.$refs.my_scroller){
          let h = this.$refs.my_scroller.content.offsetHeight;
          this.$refs.my_scroller.scrollTo(0,h,true);
        }
      }, 200);
    },
    handleMsg(msg){
      if(msg.channelId === this.channelId){
        this.msgList.push({
          channelId:this.channelId,
          time:msg.time,
          content:msg.content,
          senderId:msg.senderId
        });
        this.scrollToButtom()
      }
    },
    sendText() {
      this.showFunction = false;
      this.axios
        .post(
          "/im/sendMsgToChannel",
          this.qs.stringify({
            channelId: this.channelId,
            type: "text",
            text: this.inputValue,
          })
        )
        .then((res) => {
          if (res.code === 0) {
            this.inputValue = "";
          } else {
            this.$toast.show(res.msg || "出现错误");
          }
        });
    },
    getProfileList(){
      return this.axios
        .get('/im/getSubscriberModelListByChannelId',{
          params:{channelId: this.channelId}
        })
        .then((res=>{
          if(res.code === 0){
            res.subscriberList.forEach(profile => {
              this.$store.commit('addProfile',profile)
            })
          }
        }))
    },
    getMsgList() {
      return this.axios
        .get("/im/getMsgListByChannelId", {
          params: { channelId: this.channelId, pageNow: this.pageNow },
        })
        .then((res) => {
          this.pageNow ++;
          if (res.code === 0) {
            let appendList = res.channelMsgList;
            if(appendList.length < 10){
              this.loadEnd = true;
            }
            this.msgList = appendList.reverse().concat(this.msgList);
          } else {
            this.$toast.show(res.msg || "出现错误");
          }
        });
    },
    refresh(done){
      if(!this.loadEnd){
        this.getMsgList().then(()=>{
          done()
        })
      }else{
        done(true)
      }
    },
  },
};
</script>

<style scoped>
.chatbox {
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  position: absolute;
  background-color: #e7e7e7;
  overflow: hidden;
}

.msgbox .msgbox-item {
  margin-bottom: 15px;
  position: relative;
  display: block;
  margin-bottom: 10px;
}
.msgbox .msgbox-item .time {
  margin-bottom: 10px;
  text-align: center;
}

.msgbox .msgbox-item .time span {
  display: inline-block;
  padding: 0 5px;
  font-size: 12px;
  color: #fff;
  border-radius: 2px;
  background-color: #c7c7c7;
}

.content {
  display: inline-block;
  position: relative;
  padding: 0 10px;
  max-width: calc(100% - 75px);
  min-height: 35px;
  line-height: 2.0rem;
  font-size: 15px;
  padding: 6px 10px;
  text-align: left;
  word-break: break-all;
  background-color: #fff;
  color: #000;
  border-radius: 4px;
  box-shadow: 0 1px 7px -5px #000;
}

.content:before{
  content: " ";
  position: absolute;
  top: 9px;
  right: 100%;
  border: 6px solid transparent;
  border-right-color: #fff;
}

.content-avatar{
  margin: 0 10px;
  float: left;
}

.content span{
  display: inline;
  vertical-align:middle
}
.content img{
  display: inline;
  vertical-align:middle
}

.mine {
  text-align: right;
}

.mine .content-avatar {
  float: right;
}

.mine .content{
  background-color: #9EEA6A;
}
.mine .content:before{
  right: inherit;
  left: 100%;
  border-right-color: transparent;
  border-left-color: #9EEA6A;
}

.msgbox .move-from-left {
  left: 0;
  animation: moveLeft 0.7s ease;
  -webkit-animation: moveLeft 0.7s ease;
}
.msgbox .move-from-right {
  left: 0;
  animation: moveRight 0.7s ease;
  -webkit-animation: moveRight 0.7s ease;
}

@keyframes moveRight {
  0% {
    left: -20px;
    opacity: 0;
  }
  100% {
    left: 0;
    opacity: 1;
  }
}
@-webkit-keyframes moveRight {
  0% {
    left: -20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}
@keyframes moveLeft {
  0% {
    left: 20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}
@-webkit-keyframes moveLeft {
  0% {
    left: 20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}

.chatbox .toolbox {
  display: flex;
  position: absolute;
  right: 0;
  left: 0;
  height: 50px;
  line-height: 50px;
  box-shadow: 0px -2px 1px rgba(204, 204, 204, 0.5);
  background-color: #f1f1f1;
  align-items: center;
}

.bottom0{
  bottom: 0;
}

.bottom150{
  bottom: 150px;
}

.toolbox .back {
  align-items: center;
  display: flex;
  font-size: 2rem;
}

.toolbox .input {
  flex: 1;
  width: 0;
}

.toolbox .input input {
  width: 100%;
  height: 40px;
}

.toolbox .emoji {
  align-items: center;
  display: flex;
  font-size: 2rem;
}

.toolbox .other {
  align-items: center;
  display: flex;
  font-size: 2.5rem;
}

.chatbox .function{
  display: flex;
  position: absolute;
  right: 0;
  left: 0;
  bottom: 0;
  height: 150px;
  box-shadow: 0px -2px 1px rgba(204, 204, 204, 0.5);
  background-color: #f1f1f1;
  align-items: center;
}

.function-emoji{
  height: 150px;
  padding: 10px;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  overflow-y: scroll;
}

</style>
