<template>
  <i :class="_class" @click="clickIcon"></i>
</template>

<script>
export default {
  name: 'chat-icon',
  props: {
    type: {
      type: String,
      required: true
    }
  },
  methods: {
    clickIcon() {
      this.$emit('click', this.type);
    }
  },
  computed: {
    _class() {
      return {
        [this.$style.iconfont]: true,
        [this.$style[this.type]]: true
      }
    }
  }
}
</script>

<style module>
@font-face {
  font-family: "iconfont"; /* Project id  */
  src: url('./fonts/iconfont.ttf?t=1627534704278') format('truetype');
}

.iconfont {
  font-family: "iconfont" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/*表情*/
.icon-wind-smile:before {
  content: "\e7f9";
}

/*选择文件*/
.icon-file-plus:before {
  content: "\e808";
}

/*关闭*/
.icon-times:before {
  content: "\e82a";
}

/*向下*/
.icon-arrow-to-bottom:before {
  content: "\e83c";
}


</style>
