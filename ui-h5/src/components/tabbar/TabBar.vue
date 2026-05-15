<template>
  <div class="tab-bar">
    <slot></slot>
  </div>
</template>

<script>
  export default {
    name: "TabBar",
  };
</script>

<style>
.tab-bar {
  display: flex;
  position: fixed;
  right: 0;
  left: 0;
  bottom: 0;
  height: 50px;
  box-shadow: 0px -2px 1px rgba(204,204,204,0.5);
  background-color: #ffffff;
}

</style>
