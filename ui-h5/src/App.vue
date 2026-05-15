<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>

export default {
  name: "App",
};
</script>

<style>
#app {
  width: 100%;
  height: 100%;
  padding: 0;
  margin: 0;
}
* {
  box-sizing: border-box;
}
.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>
