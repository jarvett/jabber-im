<template>
    <div class="profile">
    <scroller ref="my_scroller" :on-refresh="refresh">
        <div class="profile-wrap">
            <div class="profile-head">
                <avatar class="profile-head-left" username="匿名" :src="avatarUrl" :rounded="false" :inline="true"></avatar>
                <div class="profile-head-right">
                    <div class="profile-head-nickname">{{nickName}}</div>
                    <div class="profile-head-unique-id">聊天号:{{uniqueId}}</div>
                </div>
                <div class="profile-head-opt" @click="editShow">
                  <span>换个马甲</span>
                  <svg class="icon" aria-hidden="true">
                    <use xlink:href="#icon-refresh"></use>
                  </svg>
                </div>
            </div>
            <div class="profile-intro" @click="editIntro">
              <span>{{intro || '这个家伙很懒,什么都没说~'}}</span>
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-brush"></use>
              </svg>
            </div>
            <div class="profile-item-divider"></div>
            <div class="profile-item" @click="hangout">随便聊聊</div>
            <div class="profile-item-divider"></div>
            <div class="profile-item" @click="group">发起群聊</div>
            <div class="profile-item-divider"></div>
            <div class="profile-item" @click="search">查找好友</div>
            <div class="profile-item-divider"></div>
            <div class="profile-item" @click="logout">退出</div>
            <div class="profile-item-bg"></div>
        </div>
    </scroller>
    </div>
</template>

<script>

import Avatar from '../components/chat/Avatar.vue'

export default {
  components:{
      Avatar
  },
  data(){
      return {
          avatarUrl:'',
          nickName:'匿名',
          uniqueId:'123456',
          intro:''
      }
  },
  created() {
    this.getProfile();
  },
  methods: {
    refresh(done) {
      this.$loading.show('玩命加载中')
      setTimeout(() => {
          this.$loading.hide();
          done();
      }, 200);
    },
    getProfile() {
      this.axios.get("/im/getMyProfile").then((res) => {
          if(res.code === 0){
              this.avatarUrl = res.profile.avatarUrl;
              this.nickName = res.profile.nickName;
              this.uniqueId = res.profile.uniqueId;
              this.intro = res.profile.intro;
              this.$store.commit('addProfile',res.profile);
              this.$store.commit('setSid',res.profile.sid)
          }else{
              this.$toast.show(res.msg);
          }
      });
    },
    hangout(){
      this.$router.push('stranger')
    },
    group(){
      this.$router.push('group')
    },
    logout(){
        this.$store.dispatch('logout')
        this.$router.push('login')
    },
    editIntro(){
        this.$msgBox.showMsgBox({
            title: '个性签名',
            content: '请填写个性签名',
            isShowInput: true
        }).then((val) => {
            if(val){
              this.axios.post('/im/changeIntro',this.qs.stringify({intro:val})).then(res=>{
                if(res.code === 0){
                  this.getProfile()
                }
              })
            }
        });
    },
    editShow(){
       this.$msgBox.showMsgBox({
            title: '换个马甲',
            content: '是否需要换了头像和昵称',
        }).then((val) => {
            if(val){
              this.axios.post('/im/changeAvatarAndNickName').then(res=>{
                if(res.code === 0){
                  this.getProfile()
                }
              })
            }
        });
    },
    search(){
      this.$toast.show('模块开发中')
    }
  },
};
</script>

<style>
.profile{
    width: 100%;
    height: 100%;
}

.profile ._v-container{
    background-color: #e7e7e7;
}

</style>

<style scoped>
.profile-wrap{
    height: 100%;
    display: flex;
    flex-direction: column;
}

.profile-head{
    padding: 30px 15px 15px 15px;
    display: flex;
    background-color: #ffffff;
}

.profile-head-right{
    margin-left: 10px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.profile-head-nickname{
    font-weight: bolder;
}

.profile-head-unique-id{
    font-size: 80%;
    color: #999999;
}

.profile-head-opt{
    align-items: center;
    display: flex;
    flex: 1;
    justify-content: center;
    color: #999999;
}

.profile-head-opt span{
  font-size: 80%;
}

.profile-intro{
    padding: 0 15px 15px 15px;
    background: #ffffff;
    font-size: 80%;
    color: #999999;
}

.profile-item-divider{
    height: 3px;
    width: 100%;
}

.profile-item{
    width: 100%;
    text-align: center;
    padding-top: 10px;
    padding-bottom: 10px;
    background-color: #ffffff;
}

.profile-item-bg{
    flex: 1;
    background-color: #e7e7e7;
}

</style>
