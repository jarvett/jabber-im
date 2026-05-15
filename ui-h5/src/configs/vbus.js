import Vue from 'vue'
import loading from './loading'
var bus = new Vue()  // 创建一个事件总线

bus.$on('show',function(){
    loading.show()    // loading出现
 })
 bus.$on('hide',function(){
    loading.hide()   // loading消失
 })

export default bus
