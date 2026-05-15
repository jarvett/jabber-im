import msgboxVue from './msgbox.vue';

// 定义插件对象
const MsgBox = {};
// vue的install方法，用于定义vue插件
MsgBox.install = function (Vue) {
  const MsgBoxInstance = Vue.extend(msgboxVue);
  let currentMsg;
  const initInstance = () => {
    // 实例化vue实例
    currentMsg = new MsgBoxInstance();
    let msgBoxEl = currentMsg.$mount().$el;
    document.body.appendChild(msgBoxEl);
  };
  // 在Vue的原型上添加实例方法，以全局调用
  Vue.prototype.$msgBox = {
    showMsgBox (options) {
      if (!currentMsg) {
        initInstance();
      }
      if (typeof options === 'string') {
        currentMsg.content = options;
      } else if (typeof options === 'object') {
        Object.assign(currentMsg, options);
      }
      return currentMsg.showMsgBox()
        .then(val => {
          currentMsg = null;
          return Promise.resolve(val);
        })
        .catch(() => {
          currentMsg = null;
          // return Promise.reject();
        });
    }
  };
};
export default MsgBox;
