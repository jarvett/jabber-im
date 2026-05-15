<template>
  <div class="chatPage" :style="setStyle">
    <div class="taleBox">
      <chat-window-list :list="taleList"
                        @click="$emit('clickTalk', $event)"
                        @loadHistory="loadHistoryHandler"
                        :config="chatListConfig"></chat-window-list>
    </div>
    <div class="toolBox">
      <chat-window-tools :tools="toolConfig" class="tools" @emoji="bindEmoji"></chat-window-tools>
      <chat-window-input @submit="$emit('enter', $event)" v-model="msg" :insert="insert"></chat-window-input>
    </div>
  </div>
</template>

<script>
import ChatWindowList from "./chat-window-list.vue";
import ChatWindowTools from "./chat-window-tools.vue";
import ChatWindowInput from "./chat-window-input.vue";

export default {
  name: "chat-window",
  components: {ChatWindowInput, ChatWindowTools, ChatWindowList},
  props: {
    taleList: {
      type: Array,
      default: () => ([])
    },
    scroll: {
      type: Number,
      default: -1
    },
    height: {
      default: '500px'
    },
    width: {
      default: '550px'
    },
    value: {
      default: ''
    },
    scrollType: {
      default: ''
    },
    toolConfig: {
      type: Object
    },
    config: {},
    quickList: {
      type: Array,
      default: () => ([])
    }
  },
  data() {
    return {
      msg: '',
      insert: ''
    }
  },
  watch: {
    value: {
      handler() {
        this.msg = this.value;
      },
      immediate: true
    },
    msg: {
      handler() {
        this.$emit('input', this.msg);
      },
      immediate: true
    }
  },
  computed: {
    setStyle() {
      let height = this.height
      let width = this.width
      if (`${height}`.match(/\d$/)) {
        height += 'px'
      }
      if (`${width}`.match(/\d$/)) {
        width += 'px'
      }
      const style = {height, width}
      return style
    },
    talkHeight() {
      let height = this.height
      if (`${height}`.match(/\d$/)) {
        height -= 140
      } else
        height = `calc(${height} - 140px)`
      return height
    },
    chatListConfig() {
      const {width, talkHeight: height, scrollToButton, scrollType, config: {historyConfig = {}} = {}} = this
      return {width, height, scrollToButton, scrollType, historyConfig}
    }
  },
  methods: {
    bindEmoji(emoji) {
      this.insert = emoji
      this.$nextTick(() => {
        this.insert = ""
      })
    },
    loadHistoryHandler() {
      const {historyConfig: {callback = null} = {}} = this.chatListConfig
      callback && callback()
    },
    quickSubmit(target) {
      const {text} = target
      this.msg = text
      this.$nextTick(() => {
        this.$emit('enter', target)
        this.msg = ''
      })
    }
  },
}
</script>
<style lang="less" scoped>
.chatPage {
  margin: 0 auto;
  position: relative;
  background: #fff;
  overflow: hidden;

  .taleBox {
    height: calc(100% - 140px);
    min-height: 100px;
    overflow: hidden;
  }

  .toolBox {
    height: 140px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
    position: relative;

    .quickList {
      transform: translateY(-100%);
      background: #fff;
      position: absolute;
      z-index: 5;
    }
  }
}
</style>
