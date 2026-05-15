<template>
  <div>
    <keyboard v-if="!step" title="请输入手机号" :maxLength="11" @confirmEvent="_confirmPhone" @cancelEvent="_cancelPhone"></keyboard>
    <keyboard v-else title="请输入验证码" :maxLength="6" @confirmEvent="_confirmCode" @cancelEvent="_cancelCode"></keyboard>
  </div>
</template>

<script>
import Keyboard from '../components/main/Keyboard.vue'
export default {
  components: {
    Keyboard,
  },
  data(){
    return {
        step:0,
        phone:'',
        code:''
    }
  },
  methods: {
    _confirmPhone(res) {
      if(!/^1[3|4|5|7|8][0-9]{9}$/.test(res)){
          this.$toast.show('请输入正确的手机号');
      }else{
        this.phone = res;
        this.axios.get('/im/getPhoneCode',{params:{phone:this.phone}}).then(res=>{
            if(res.code === 0){
                this.step = 1;
            }
        })
      }
    },
    _cancelPhone(){
        this.$router.push('chat')
    },
    _confirmCode(res) {
      if(!/^[0-9]{6}$/.test(res)){
          this.$toast.show('请输入6位数验证码');
      }else{
          this.code = res;
          this.axios.get('/im/getToken',{params:{phone:this.phone,code:this.code}}).then(res=>{
              if(res.code === 0){
                this.$store.dispatch('login',res)
                this.$router.push('chat')
              }else {
                  this.$toast.show(res.msg||'请求错误')
              }
          })
      }
    },
    _cancelCode(){
      this.step = 0
    }
  },
};
</script>

<style scoped>
.title {
  color: orange;
  width: 100%;
  text-align: center;
}
</style>
