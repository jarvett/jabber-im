<template>
    <span class="item_msg" @click="itemCallback">
    <span v-if="getTag === 'span'" v-html="parseText()"/>
    <img
        class="web__msg--img"
        v-if="getTag === 'img'"
        :src="getData.src"
        alt
        @click="showDialog({tag:'img'})"
        @load="load('img')"
    />
    <video
        class="web__msg--video"
        v-if="getTag === 'video'"
        :src="getData.src"
        controls="controls"
        @click="showDialog({tag:'video'})"
        @canplaythrough="load('video')"
    />
    <audio
        class="web__msg--audio"
        v-if="getTag === 'audio'"
        style="width:20rem;height:20px;"
        :src="getData.src"
        controls="controls"
        @canplaythrough="load('audio')"
    />

      <!-- 查看区域 -->
    <el-dialog
        v-if="['video','img'].includes(getTag)"
        :visible.sync="show"
        width="40%"
        append-to-body
        :before-close="handleClose"
        class="web__dialog"
    >
      <img :src="imgSrc" v-if="imgSrc" style="width:100%;object-fit: cover;"/>
      <video
          :src="videoSrc"
          v-if="videoSrc"
          style="width:100%;object-fit: cover;"
          controls="controls"
      ></video>
      <audio
          :src="audioSrc"
          v-if="audioSrc"
          style="width:100%;object-fit: cover;"
          controls="controls"
      ></audio>
    </el-dialog>
  </span>
</template>

<script>

import {configParseEmoji, parseEmoji} from 'wechat-emoji-parser'

configParseEmoji({size: 24});
export default {
  name: "talk-item",
  props: {
    text: String,
  },
  data() {
    return {
      tags: ['img', 'video', 'audio'],
      /* 查看文件详情 */
      show: false,
      imgSrc: '',
      videoSrc: '',
      audioSrc: '',
      loadState: false
    }
  },
  computed: {
    getTag() {
      const str = this.text || ""
      let tag = "span"
      let type = ""
      if (str.match(/<\/?[^>]+>/)) {
        type = str.split(" ")[0].replace(/<|>/, "") || ""
        if (this.tags.includes(type)) {
          tag = type
        }
      }
      return tag
    },
    getData() {
      const str = this.text || ""
      const dom = document.createElement('div')
      dom.innerHTML = str
      const target = dom.firstChild

      const src = target.getAttribute('data-src')
      const controls = target.getAttribute('controls') || false
      return {
        src,
        controls
      }
    },
  },
  methods: {
    load(type) {
      if (this.loadState) return
      this.loadState = true
      this.$emit('loadDone', {type, target: this.text})
    },
    handleClose(done) {
      this.imgSrc = undefined;
      this.videoSrc = undefined;
      this.audioSrc = undefined;
      done();
    },
    showDialog({tag}) {
      const {src} = this.getData
      const callback = () => {
        if (tag === 'img') {
          this.imgSrc = src;
          this.show = true;
        } else if (tag === 'video') {
          this.videoSrc = src;
          this.show = true;
        } else if (tag === 'audio') {
          this.audioSrc = src;
          this.show = true;
        } else if (tag === 'FILE') {
          window.open(src)
        }
      }
      if (typeof this.beforeOpen === 'function') {
        this.beforeOpen({tag, src}, callback)
      } else {
        callback();
      }
    },
    itemCallback() {
      this.$emit('systemEvent', this.text)
    },
    parseText() {
      let {text} = this
      if (!text) return
      text = text.replace(/\n/g, '<br/>')
      const html = parseEmoji(text).replace(/<img src/g, '<img data-class="iconBox" src')
      return html
    },
  },
  mounted() {
    const type = this.getTag
    if (this.tags.includes(type)) return
    this.$nextTick(() => {
      this.$emit('loadDone', {type, target: this.text})
    })
  }
}
</script>
<style lang="less" scoped>
.item_msg {
  padding: 0px;
  margin: 0px;
  display: inline-block;
  display: flex;

  .web__msg--img,
  .web__msg--video,
  .web__msg--file {
    max-width: 250px;
    min-width: 50px;
    width: 100%;
    margin: 10px 0;
    border: 1px solid #eee;
    overflow: hidden;
    border-radius: 5px;
    cursor: pointer;
    display: block;
  }

  .web__msg--img[data-class="iconBox"] {
    max-width: 24px;
    min-width: unset;
    border: none;
    margin: 0;
    vertical-align: bottom;
    display: inline-block;
  }
}
</style>
