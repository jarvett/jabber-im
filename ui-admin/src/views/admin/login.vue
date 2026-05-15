<template>
  <div class="center">
    <el-card class="login-card">
      <h3>登录系统</h3>
      <el-form >
        <!-- 手机号输入行 -->
        <el-form-item label="">
          <el-input v-model="phone" placeholder="请输入手机号" type="text" maxlength="11"></el-input>
        </el-form-item>

        <!-- 验证码行 -->
        <el-form-item label="">
          <div class="code-container">
            <el-input v-model="code" placeholder="请输入6位验证码" type="text" maxlength="6"></el-input>
            <el-button
              type="primary"
              :disabled="isCounting || !isValidPhone"
              @click="getVerificationCode">
              {{ countdownText }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button type="success" class="full-width" @click="submitLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { Message } from 'element-ui';
export default {
  name: "login",
  components: {},
  data() {
    return {
      phone: '',
      code: '',
      countdownTime: 0,
      isCounting: false
    }
  },
  computed: {
    isValidPhone() {
      return /^1[3|4|5|7|8][0-9]{9}$/.test(this.phone);
    },
    countdownText() {
      return this.isCounting ? `${this.countdownTime}秒后重发` : '获取验证码';
    }
  },
  methods: {
    getVerificationCode() {
      if (!this.isValidPhone) {
        Message.error('请输入正确的手机号');
        return;
      }

      // 启动倒计时
      this.isCounting = true;
      this.countdownTime = 60;
      const timer = setInterval(() => {
        this.countdownTime--;
        if (this.countdownTime <= 0) {
          clearInterval(timer);
          this.isCounting = false;
        }
      }, 1000);

      // 请求验证码
      this.axios.get('/im/getPhoneCode', { params: { phone: this.phone } })
        .then(res => {
          if (res.code === 0) {
            Message.success('验证码已发送');
          }
        })
        .catch(() => {
          Message.error('验证码获取失败');
          clearInterval(timer);
          this.isCounting = false;
        });
    },
    submitLogin() {
      if (!this.isValidPhone) {
        Message.error('请输入正确的手机号');
        return;
      }

      if (!/^[0-9]{6}$/.test(this.code)) {
        Message.error('请输入6位数字验证码');
        return;
      }

      this.axios.get('/im/getToken', {
        params: { phone: this.phone, code: this.code }
      }).then(res => {
        if (res.code === 0) {
          debugger
          this.$store.dispatch('login', res);
          this.$router.push('chat');
        } else {
          Message.error(res.msg || '请求错误');
        }
      }).catch(() => {
        Message.error('登录失败');
      });
    }
  }
}
</script>

<style scoped>
.center {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.full-width {
  width: 100%;
  margin-top: 10px;
}
.code-container {
  display: flex;
  gap: 10px;
}
.code-container .el-input {
  flex: 1;
}
</style>
