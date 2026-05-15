<template>
  <div class="dialogue-wrap" v-show="visible">
    <chat-icon class="btn-close" type="icon-times" @click="close()"></chat-icon>
    <div class="dialogue-title" draggable="false">会话列表</div>
    <div @click="selectDialogue(index)" :class="['dialogue-item-wrap',{active:index === selectedIndex}]"
         v-for="(data,index) in list" :key="index">
      <chat-item class="left" :config="data"></chat-item>
      <div class="right">{{ formatChatDialogueTime(data.time) }}</div>
    </div>
  </div>
</template>

<script>

import {formatChatDialogueTime} from '../assets/utils'
import ChatItem from "./assembly/chat-item";
import ChatIcon from "./assembly/chat-icon";

let data = {
  time: '1627164366000',
  img: 'http://img.fkzg.net/fzgmall/images/20210726/9fb883f4aa11ce29f39cdd73d22df70a.png',
  name: '官方客服',
  dept: '上海天气 ：当前温度26℃，感冒低发期，天气舒适，请注意多吃蔬菜水果，多喝水哦。',
  num: 5,
};
// let list = [data,data,data,data,data,data,data,data,data];
let list = [];

export default {
  name: "chat-dialogue",
  components: {ChatIcon, ChatItem},
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    dialogueList: Array,
  },

  data() {
    return {
      selectedIndex: -1,
      list: list
    }
  },
  methods: {
    formatChatDialogueTime,
    selectDialogue(index) {
      this.selectedIndex = index;
      this.$emit('select', this.list[index]);
    },
    close() {
      this.$emit('update:visible', false);
    },
  },
  watch: {
    dialogueList(list) {
      this.list = list;
    }
  }
}
</script>

<style lang="less" scoped>
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

.dialogue-wrap {
  position: absolute;
  top: 10%;
  left: 10%;
  width: 250px;
  height: 600px;
  overflow-y: auto;
  background-color: #e3e3e3;
  border-radius: 12px;

  .btn-close {
    position: absolute;
    right: 0;
    top: 0;
    margin-top: 10px;
    margin-right: 10px;
    cursor: pointer;
  }

  .dialogue-title {
    padding: 10px 0;
    font-size: 14px;
    cursor: pointer;
    background-color: #d9d9d9;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }

  .dialogue-item-wrap {
    background-color: #f5f5f5;
    padding: 5px;
    cursor: pointer;
    display: flex;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;

    .left {
      flex: 1;
      margin-right: 20px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .right {
      float: right;
      font-size: 10px;
      height: 41px;
      color: #b9b9b9;
    }
  }

  .active {
    background-color: #c6c6c6;

    .right {
      color: #d8d8d8;
    }
  }

}

</style>
