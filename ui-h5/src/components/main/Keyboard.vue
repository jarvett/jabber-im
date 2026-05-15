<template>
  <div class="key-container">
    <div class="key-title">{{ title }}</div>
    <div class="input-box">{{ value }}</div>
    <div class="keyboard" @click.stop="_handleKeyPress">
      <div class="key-row">
        <div class="key-cell" data-num="7">7</div>
        <div class="key-cell" data-num="8">8</div>
        <div class="key-cell" data-num="9">9</div>
        <div class="key-cell" data-num="D">C</div>
      </div>
      <div class="key-row">
        <div class="key-cell" data-num="4">4</div>
        <div class="key-cell" data-num="5">5</div>
        <div class="key-cell" data-num="6">6</div>
        <div class="key-cell" data-num="C">清空</div>
      </div>
      <div class="key-row">
        <div class="key-cell" data-num="1">1</div>
        <div class="key-cell" data-num="2">2</div>
        <div class="key-cell" data-num="3">3</div>
        <div class="key-cell" data-num="-1"></div>
      </div>
      <div class="key-row">
        <div class="key-cell" data-num="B">返回</div>
        <div class="key-cell" data-num="0">0</div>
        <div class="key-cell" :class="{ disabled: !isAmount }" data-num=".">
          .
        </div>
        <div class="key-cell" data-num="-1"></div>
      </div>
      <div class="key-confirm" data-num="S">确认</div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: "",
    },
    isAmount: {
      type: Boolean,
      default: false,
    },
    maxLength: {
      type: Number,
      default: 8,
    },
  },
  data() {
    return {
      value: "",
    };
  },
  methods: {
    //处理按键
    _handleKeyPress(e) {
        e.srcElement.classList.add("clicked")
        setTimeout(() => {
            e.srcElement.classList.remove("clicked")
        }, 100);
      let num = e.target.dataset.num;
      //不同按键处理逻辑
      // -1 代表无效按键，直接返回
      if (num == -1) return false;

      switch (String(num)) {
        //小数点
        case ".":
          if (this.isAmount) {
            this._handleDecimalPoint();
          }
          break;
        //返回键
        case "B":
          this._handleBackKey();
          break;
        //删除键
        case "D":
          this._handleDeleteKey();
          break;
        //清空键
        case "C":
          this._handleClearKey();
          break;
        //确认键
        case "S":
          this._handleConfirmKey();
          break;
        default:
          this._handleNumberKey(num);
          break;
      }
    },
    //处理返回键
    _handleBackKey(){
      this.$emit("cancelEvent");
    },
    //处理小数点函数
    _handleDecimalPoint() {
      if (this.value.length >= this.maxLength) return false;
      //如果包含小数点，直接返回
      if (this.value.indexOf(".") > -1) return false;
      //如果小数点是第一位，补0
      if (!this.value.length) this.value = "0.";
      //如果不是，添加一个小数点
      else this.value = this.value + ".";
    },
    //处理删除键
    _handleDeleteKey() {
      let S = this.value;
      //如果没有输入，直接返回
      if (!S.length) return false;
      //否则删除最后一个
      this.value = S.substring(0, S.length - 1);
    },
    //处理清空键
    _handleClearKey() {
      this.value = "";
    },
    //处理数字
    _handleNumberKey(num) {
      if (this.value.length >= this.maxLength) return false;
      let S = this.value;
      //如果有小数点且小数点位数不小于2
      if (S.indexOf(".") > -1 && S.substring(S.indexOf(".") + 1).length < 2)
        this.value = S + num;

      //没有小数点
      if (!(S.indexOf(".") > -1)) {
        //如果第一位是0，只能输入小数点
        if (this.isAmount && num == 0 && S.length == 0) this.value = "0.";
        else {
          if (this.isAmount && S.length && Number(S.charAt(0)) === 0) return;
          this.value = S + num;
        }
      }
    },
    //提交
    _handleConfirmKey() {
      let S = this.value;
      //未输入
      if (!S.length) {
        return false;
      }
      if (this.isAmoun) {
        //将 8. 这种转换成 8.00
        if (S.indexOf(".") > -1 && S.indexOf(".") == S.length - 1){
            S = Number(S.substring(0, S.length - 1)).toFixed(2);
        }
        //保留两位
        S = Number(S).toFixed(2);
      }
      this.value = "";
      this.$emit("confirmEvent", S);
    },
  },
};
</script>

<style scoped>
.key-container {
  margin-top: 80px;
  width: 100%;
  display: flex;
  display: -webkit-flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.input-box {
  margin-top: 30px;
  font-size: xx-large;
  font-weight: bolder;
  height: 50px;
  border-bottom: 1px solid #aaa;
  padding: 10px 15px;
  text-align: center;
  width: 90%;
}

.keyboard {
  position: fixed;
  bottom: 0;
  left: 0;
  height: 240px;
  width: 100%;
}

.keyboard .key-row {
  display: flex;
  display: -webkit-flex;
  position: relative;
  height: 60px;
  line-height: 60px;
}

.keyboard .key-row::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  height: 1px;
  border-top: 1px solid #d5d5d6;
  color: #d5d5d6;
  -webkit-transform-origin: 0 0;
  transform-origin: 0 0;
  -webkit-transform: scaleY(0.5);
  transform: scaleY(0.5);
}

.keyboard .key-cell {
  flex: 1;
  -webkit-box-flex: 1;
  text-align: center;
  position: relative;
  font-weight: bolder;
  font-size: larger;
}

.keyboard .key-cell::after {
  content: "";
  position: absolute;
  overflow: hidden;
  top: 0;
  right: 0;
  bottom: 0;
  height: 200%;
  border-right: 1px solid #d5d5d6;
  color: #d5d5d6;
  -webkit-transform-origin: 0 0;
  transform-origin: 0 0;
  -webkit-transform: scaleY(0.5);
  transform: scaleY(0.5);
}

.keyboard .key-cell:nth-last-child(1)::after {
  border-right: 0;
}

.keyboard .disabled {
  background: rgba(0, 0, 0, 0.2);
}

.keyboard .key-confirm {
  font-weight: bolder;
  font-size: larger;
  position: absolute;
  text-align: center;
  height: 118px;
  width: 25%;
  line-height: 118px;
  background: #fff;
  z-index: 5;
  right: 0;
  bottom: 1px;
}
.keyboard .clicked{
    background: #d5d5d6;
}
</style>
