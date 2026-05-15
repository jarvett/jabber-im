import axios from 'axios'
import store from './store'
import router from './router'

axios.defaults.timeout = 60e3;  //请求60秒超时

//请求拦截
axios.interceptors.request.use(config => {
  let token = store.state.token
  if (token) {
    config.headers.token = token
  }
  return config;
});

//响应处理
axios.interceptors.response.use(res => {
  let rd = res.data;
    if(rd && rd.code === 666){
        router.push('login');
    }
    return rd;
}, error => {
  return {'msg': error.message, 'code': -1}
});

export default axios
