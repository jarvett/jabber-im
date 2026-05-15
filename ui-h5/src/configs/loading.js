import Vue from 'vue'
import Loading from '../components/global/loading'
Vue.use(Loading)


Loading.show = function(msg){
    Loading._toggle(msg,'show');
}

Loading.hide = function(){
    Loading._toggle('','hide');
}

export default Loading
