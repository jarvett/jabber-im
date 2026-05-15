<template>
  <div class="item">
    <div class="badge" v-show="config.num" v-if="config.num < 99">{{ config.num }}</div>
    <div class="badge" v-show="config.num" v-else>99+</div>
    <img v-if="config.img" :src="config.img" :style="coverSize" class="cover"
         @click="bindClick({ key:'cover', value: config })"/>
    <div class="info">
      <p class="name" @click="bindClick({ key:'name', value: config })">{{ config.name }}</p>
      <p class="dept" @click="bindClick({ key:'dept', value: config })">{{ config.dept }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "chat-item",
  props: {
    config: {
      type: Object,
      default: () => ({
        img: '',
        name: '张三',
        dept: '',
        num: 0,
      }),
      required: true
    },
    size: {
      default: 35
    }
  },
  computed: {

    coverSize() {
      let size = this.size
      if (`${size}`.match(/\d$/)) {
        size += 'px'
      }
      return {
        width: size,
        height: size,
      }
    }
  },
  methods: {
    bindClick(type) {
      this.$emit('click', type)
    }
  },
}
</script>

<style lang='less' scoped>
.item {
  display: flex;
  align-items: center;
  position: relative;

  .badge {
    position: absolute;
    top: 0;
    left: 32px;
    color: white;
    background-color: red;
    height: 18px;
    width: 18px;
    border-radius: 9px;
    font-size: 8px;
    transform: scale(0.7)
  }

  .cover {
    border-radius: 50%;
    margin-right: 12px;
    box-shadow: 0 3px 3px 0 rgba(0, 0, 0, 0.1);
    cursor: pointer;
  }

  .info {
    display: flex;
    flex-direction: column;
    justify-content: center;

    .dept {
      color: #999999;
    }

    p {
      margin: 0;
      padding: 0;
      text-overflow: ellipsis;
      overflow: hidden;
      text-align: left;
      white-space: nowrap;
      font-size: 13px;
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }

      &:last-child {
        font-size: 12px;
      }
    }
  }
}

</style>
