<template>
  <div class="toolsBox">
    <el-popover placement="top-start" trigger="click" ref="popover">
      <div class="emoji-content">
        <a v-for="(emoji) in emojis" :key="emoji.code"
           style="margin: 2px;"
           :title="emoji.cn"
           :style="emoji.style"
           @click="selectEmoji(emoji.cn)"
        ></a>
      </div>
      <chat-icon slot="reference" type="icon-wind-smile" title="表情"/>
    </el-popover>
    <chat-icon type="icon-file-plus" title="文件" @click="openFile('file',...arguments)"/>
  </div>
</template>

<script>
import ChatIcon from "./chat-icon";
import {getEmojis} from 'wechat-emoji-parser'

let emojis = getEmojis({size: 24});
export default {
  name: "chat-window-tools",
  components: {ChatIcon},
  props: {
    tools: {
      type: Object,
      default: () => {
        return {
          callback: () => {
          }
        }
      }
    }
  },
  data() {
    return {
      emojis: emojis,
    }
  },
  methods: {
    selectEmoji(type) {
      this.$emit('emoji', type)
      this.$refs.popover.doClose()
    },
    openFile(type) {
      let callback = this.tools.callback;
      if (!callback) return console.warn('callback not find')
      var input = document.createElement("input")
      input.type = "file"
      input.click();
      input.onchange = function () {
        var file = input.files
        callback(type, file)
      }
    },
  }
}
</script>

<style lang="less" scoped>
.emoji-content {
  width: 300px;
  height: 120px;
  overflow-y: auto;
}

.toolsBox {
  position: relative;
  text-align: left;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  height: 30px;
}

i {
  padding-left: 6px;
  font-size: 20px;
  color: #888a91;
}

i:hover {
  color: #76b1f9;
}

.emjioBox {
  background: #fff;
  height: 150px;
  width: 300px;
  overflow: auto;
  text-align: left;
  padding: 0;

  li {
    display: inline-block;
    width: 28px;
    height: 28px;
    line-height: 28px;
    text-align: center;
    cursor: pointer;
  }
}
</style>
