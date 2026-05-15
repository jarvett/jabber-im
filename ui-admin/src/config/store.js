import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const sid_key = 'admin_im_sid'
const token_key = 'admin_im_token'

export default new Vuex.Store({
  state:{
        token:localStorage.getItem(token_key) || '',//当前登录用户的token
        sid:localStorage.getItem(sid_key) || '',//当前登录用户的id
        currentChatBoxName:'',//当前聊天详情的title
        profileMap:{},
    },
    mutations:{
        setSid(state,value){
            state.sid = value;
            localStorage.setItem(sid_key,value);
        },
        unsetSid(state){
          localStorage.removeItem(sid_key)
          state.sid = ''
        },
        setToken(state,value){
            localStorage.setItem(token_key,value);
            state.token = value;
        },
        unsetToken(state){
            localStorage.removeItem(token_key)
            state.token=''
        },
        setChatBoxName(state,value){
            state.currentChatBoxName = value;
            document.title = value;
        },
        addProfile(state,profile){
          state.profileMap[profile.sid] = profile;
        }
    },
    actions:{
        login(context,data){
            context.commit('setToken',data.token)
            context.commit('setSid',data.sid)
        },
        logout(context){
            context.commit('unsetSid')
            context.commit('unsetToken')
        }
    }
});
