<template>
  <div class="message-box" v-show="isShowMessageBox">
    <div class="mask" @click="cancel"></div>
    <div class="message-content">
      <div class="title">{{ title }}</div>
      <div class="content">{{ content }}</div>
      <div>
        <input
          type="text"
          v-model="inputValue"
          v-if="isShowInput"
          ref="input"
          @keyup.enter="confirm"
        />
      </div>
      <div class="btn-group">
        <button class="btn-default" @click="cancel" v-show="isShowCancelBtn">
          {{ cancelBtnText }}
        </button>
        <button
          class="btn-primary btn-confirm"
          @click="confirm"
          :disabled="isShowInput && !inputValue"
          v-show="isShowConfimrBtn"
        >
          {{ confirmBtnText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: "标题",
    },
    content: {
      type: String,
      default: "这是弹框内容",
    },
    isShowInput: {
      type: Boolean,
      default: false,
    },
    inputValue: {
      type: String,
      default: "",
    },
    isShowCancelBtn: {
      type: Boolean,
      default: true,
    },
    isShowConfimrBtn: {
      type: Boolean,
      default: true,
    },
    cancelBtnText: {
      type: String,
      default: "取消",
    },
    confirmBtnText: {
      type: String,
      default: "确定",
    },
  },
  data() {
    return {
      isShowMessageBox: false,
      resolve: "",
      reject: "",
      promise: "", // 保存promise对象
    };
  },
  methods: {
    // 确定,将promise断定为resolve状态
    confirm: function () {
      this.isShowMessageBox = false;
      if (this.isShowInput) {
        this.resolve(this.inputValue);
      } else {
        this.resolve("confirm");
      }
      this.remove();
    },
    // 取消,将promise断定为reject状态
    cancel: function () {
      this.isShowMessageBox = false;
      this.reject();
      this.remove();
    },
    // 弹出messageBox,并创建promise对象
    showMsgBox: function () {
      this.isShowMessageBox = true;
      this.promise = new Promise((resolve, reject) => {
        this.resolve = resolve;
        this.reject = reject;
      });
      // 返回promise对象
      return this.promise;
    },
    remove: function () {
      setTimeout(() => {
        this.destroy();
      }, 300);
    },
    destroy: function () {
      this.$destroy();
      document.body.removeChild(this.$el);
    },
  },
};
</script>

<style scoped>

.message-box {
  position: relative;
}

.mask {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  z-index: 50000;
  background: rgba(0, 0, 0, 0.5);
}
.message-content {
  position: fixed;
  box-sizing: border-box;
  padding: 1em;
  min-width: 90%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 0.4em;
  background: #fff;
  z-index: 50001;
}

.title {
  font-size: 1.2em;
  font-weight: 600;
  margin-bottom: 10px;
}
.content {
  font-size: 1em;
  line-height: 2em;
  color: #555;
}

input {
  width: 100%;
  margin: 10px 0;
  background-color: #fff;
  border-radius: 0.4em;
  border: 1px solid #d8dce5;
  box-sizing: border-box;
  color: #5a5e66;
  display: inline-block;
  font-size: 14px;
  height: 3em;
  line-height: 1;
  outline: none;
  padding: 0 1em;
}
input:focus {
  border-color: #2d8cf0;
}

.btn-group {
  margin-top: 1em;
  float: right;
  overflow: hidden;
}
.btn-default {
  padding: 0.5em 1em;
  font-size: 1em;
  color: #555;
  border: 1px solid #d8dce5;
  border-radius: 0.2em;
  cursor: pointer;
  background-color: #fff;
  outline: none;
}

.btn-default:hover {
  color: #2d8cf0;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-primary {
  padding: 0.5em 1em;
  font-size: 1em;
  color: #fff;
  border-radius: 0.2em;
  cursor: pointer;
  border: 1px solid #45ad25;
  background-color: #45ad25;
  outline: none;
}
.btn-primary:hover {
  opacity: 0.8;
}
.btn-confirm {
  margin-left: 1em;
}
</style>
