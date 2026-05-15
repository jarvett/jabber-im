<template>
  <div class="stranger">
    <div class="stranger-wrap">
        <div class="stranger-item" v-for="item in strangerList" :key="item.sid">
          <div class="stranger-item-left">
            <avatar class="stranger-item-avatar" username="匿名" :src="item.avatarUrl" :rounded="false" :inline="true" ></avatar>
            <div class="stranger-item-avatar-right">
                <div class="stranger-item-name">{{ item.nickName }}</div>
                <div class="stranger-item-brief">{{ item.intro || '这个家伙很懒,什么都没说~' }}</div>
            </div>
          </div>
          <div class="stranger-item-right">
            <div class="stranger-action-text" @click="chatOne(item.sid)">撩一下</div>
          </div>
        </div>
    </div>
    <div class="stranger-opt">
      <div class="back" @click="back">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-undo"></use>
        </svg>
      </div>
      <div class="refresh" @click="refresh">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-refresh"></use>
        </svg>
      </div>
    </div>
  </div>
</template>

<script>
import Avatar from '../components/chat/Avatar.vue'

export default {
  components: {
      Avatar,
  },
  data() {
    return {
        strangerList:[]
    };
  },
  created(){
    this.getStrangerList();
  },
  methods: {
    back() {
      this.$router.go(-1);
    },
    refresh() {
      this.$loading.show("玩命加载中");
      this.getStrangerList().then(()=>{
        this.$loading.hide();
      })
    },
    getStrangerList(){
        return this.axios.get('/im/getRandomStranger').then(res=>{
            if(res.code === 0){
                this.strangerList = res.subscriberList
            }
        })
    },
    chatOne(id){
        this.axios.post('/im/createPrivateDialogue',this.qs.stringify({sid:id})).then(res=>{
            if(res.code === 0){
                let channelId = res.model.channelId;
                this.$router.replace('/chatbox?channelId='+channelId);
                let currentChatBoxName = res.model.name;
                this.$store.commit('setChatBoxName',currentChatBoxName);
            }
        })
    },
  },
};
</script>

<style>
.stranger {
  width: 100%;
  height: 100%;
  background-color: #e7e7e7;
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
}

</style>

<style scoped>
.stranger-item {
  display: flex;
  padding: 10px;
  background-color: #ffffff;
  margin-bottom: 3px;
  max-height: 60px;
  overflow: hidden;
  align-items: center;
}

.stranger-item-left {
  width: 0;
  flex: 1;
  display: flex;
  height: 50px;
}

.stranger-item-avatar{
    min-width: 60px;
}

.stranger-item-avatar-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  max-height: 60px;
  overflow: hidden;
}

.stranger-item-name {
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stranger-item-brief {
  font-size: 70%;
  color: #999999;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stranger-item-right{
    line-height: 50px;
    font-size: small;
    color: #666666;
}

.stranger-opt{
  display: flex;
  align-items: center;
  padding: 10px;
  background: #ffffff;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.stranger-opt .back {
  flex: 1;
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
}

.stranger-opt .refresh {
  flex: 1;
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
}

</style>
