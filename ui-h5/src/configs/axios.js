import axios from 'axios'
import store from './store'
import router from './router'
import bus from './vbus'

axios.defaults.timeout = 30e3;  //请求30秒超时
//请求拦截
axios.interceptors.request.use(config => {
    if(!config.noLoading){
        bus.$emit('show')
     }
    let token = store.state.token
    if(token) {
        config.headers.token = token
    }
    return config;
});

//响应处理
axios.interceptors.response.use(res => {
    bus.$emit('hide')
    let rd = res.data;
    if(rd && rd.code === 666){
        router.push('login');
    }
    return rd;
},err=>{
    bus.$emit('hide')
    console.error(err)
});

export default axios
