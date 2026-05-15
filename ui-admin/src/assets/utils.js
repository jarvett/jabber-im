export const isEmpty = function (val) {
  // null or undefined
  if (val == null) return true;
  if (typeof val === 'boolean') return false;
  if (typeof val === 'number') return !val;
  if (val instanceof Error) return val.message === '';
  switch (Object.prototype.toString.call(val)) {
    // String or Array
    case '[object String]':
    case '[object Array]':
      return !val.length;
    // Map or Set or File
    case '[object File]':
    case '[object Map]':
    case '[object Set]': {
      return !val.size;
    }
    // Plain Object
    case '[object Object]': {
      return !Object.keys(val).length;
    }
  }
  return false;
};

/*
对Date的扩展，将 Date 转化为指定格式的String
月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q)可以用1-2个占位符
年(y)可以用1-4个占位符，毫秒(S)只能用1个占位符号(是1-3为的数字)
例子：
(new Date()).Format("yyyy-MM-dd hh:mm:ss.S")	==> 2006-07-02 08:09:04.423
(new Date()).Format("yyyy-M-d h:m:s.S")			==> 2006-7-2 8:9:4.18
*/
Date.prototype.format = function (fmt) {
  const o = {
    "y+": this.getFullYear(),
    "M+": this.getMonth() + 1,
    "d+": this.getDate(),
    "H+": this.getHours(),
    "m+": this.getMinutes(),
    "s+": this.getSeconds(),
    "S+": this.getMilliseconds(),
    "q+": Math.floor(this.getMonth() / 3) + 1,
    "h+": (() => {
      const hour = this.getHours() % 12;
      return hour == 0 ? 12 : hour;
    })(),
    "E+": (() => {
      const week = {
        "0": "Sunday",
        "1": "Monday",
        "2": "Tuesday",
        "3": "Wednesday",
        "4": "Thursday",
        "5": "Friday",
        "6": "Saturday"
      };
      return week[this.getDay() + ""];
    })(),
    "x1": (() => {
      const week = {"0": "周日", "1": "周一", "2": "周二", "3": "周三", "4": "周四", "5": "周五", "6": "周六"};
      return week[this.getDay() + ""];
    })(),
    "x2": (() => {
      const hour = ["凌晨", "早上", "下午", "晚上"];
      const h = this.getHours();
      if (h === 12) return "中午";
      return hour[parseInt(h / 6)];
    })(),
  }
  for (var k in o) {
    if (new RegExp("(" + k + ")", "g").test(fmt)) {
      const len = RegExp.$1.length;
      fmt = fmt.replace(RegExp.$1, len == 1 ? o[k] : ("00" + o[k]).substr(-len));
    }
  }
  return fmt;
}

Date.prototype.toWeiXinString = function (shortShow = true) {
  let str;
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1);
  const monday = new Date(today);
  monday.setDate(today.getDate() - (today.getDay() ? today.getDay() - 1 : 6));
  //注意：date初始化默认是按本地时间初始的，但打印默认却是按GMT时间打印的，也就是说打印出的不是本地现在的时间
  //LocaleString的打印也有点问题，"0点"会被打印为"上午12点"
  if (this.getTime() > today.getTime()) {
    str = this.format("HH:ss");
  } else if (this.getTime() > yesterday.getTime()) {
    str = "昨天";
  } else if (this.getTime() > monday.getTime()) {
    const week = {"0": "周日", "1": "周一", "2": "周二", "3": "周三", "4": "周四", "5": "周五", "6": "周六"};
    str = week[this.getDay() + ""];
  } else {
    str = this.format("yy/MM/dd");
  }
  if (!shortShow && this.getTime() <= today.getTime()) {
    str += this.format("HH:ss");
  }
  return str;
};

export const formatChatDialogueTime = function (time) {
  return new Date(Number(time)).toWeiXinString(true);
};

export const formatChatMainWindowTime = function (time) {
  return new Date(Number(time)).toWeiXinString(true);
};

export const jsonParse = function (str) {
  try {
    return JSON.parse(str)
  } catch (e) {
    return {}
  }
}

export const WS = {
  url: null,
  token: null,
  callback: null,
  socket: null,//socket对象
  pptt: null,//心跳机制的延时函数
  lockReconnect: false,//避免重复连接
  connectServer(url, token, callback) {
    let _this = this;
    _this.url = url;
    _this.token = token;
    _this.callback = callback;
    try {
      _this.socket = new WebSocket(url + '?token=' + token);
      _this.initSocket(callback);
    } catch (e) {
      _this.reconnect();
    }
  },
  initSocket(callback) {
    let _this = this;
    _this.socket.onopen = function () {
      console.log('连接成功')
      _this.socket.send('ping');
    };
    _this.socket.onmessage = function (event) {
      if ('pong' === event.data) {//发送字符串ping,服务器会返回pong,间隔30s做心跳
        _this.pptt = setTimeout(function () {
          _this.socket.send('ping');
        }, 30000);
        return;
      }
      if (callback) {
        console.log('type of callback', typeof callback);
        callback(event.data);
      }
    };
    _this.socket.onclose = function (e) {
      //当客户端收到服务端发送的关闭连接请求时，触发onclose事件
      _this.socket.close();
      console.log("服务器关闭了");
      _this.reconnect();
    };
    _this.socket.onerror = function (e) {
      //如果出现连接、处理、接收、发送数据失败的时候触发onerror事件
      console.log('出现异常' + e);
      _this.reconnect();
    };
  },
  reconnect() {
    let _this = this;
    if (_this.lockReconnect) {       // 是否已经执行重连
      return;
    }
    _this.pptt && clearTimeout(_this.pptt);
    _this.lockReconnect = true;//没连接上会一直重连，设置延迟避免请求过多
    console.log('连接已断开,重新连接', _this.url, _this.token);
    setTimeout(function () {
      _this.lockReconnect = false;
      _this.connectServer(_this.url, _this.token, _this.callback);
    }, 10000);
  }
};

import CryptoJS from 'crypto-js'
import SparkMD5 from 'spark-md5'

export const fileMd5Sum = (file) => {
  // return new Promise(resolve => {
  //     const fileReader = new FileReader();
  //     fileReader.onloadend = ev => {
  //         debugger
  //         resolve(
  //             CryptoJS.MD5(CryptoJS.enc.Latin1.parse(ev.target.result)).toString(
  //                 CryptoJS.enc.Hex
  //             )
  //         );
  //     };
  //     fileReader.readAsBinaryString(file);
  // });
  //上面这种方法，当文件小于200M的时候没太大问题，大过就崩了

  // 指定块的大小，这里设置为5MB,可以根据实际情况进行配置
  const alog = CryptoJS.algo.MD5.create();
  const chunkSize = 5 * 1024 * 1024
  let promise = Promise.resolve()
  // 使用promise来串联hash计算的顺序。因为FileReader是在事件中处理文件内容的，必须要通过某种机制来保证update的顺序是文件正确的顺序
  for (let index = 0; index < file.size; index += chunkSize) {
    promise = promise.then(() => hashBlob(file.slice(index, index + chunkSize)))
  }

  /**
   * 更新文件块的hash值
   */
  function hashBlob(blob) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = ({target}) => {
        const wordArray = CryptoJS.lib.WordArray.create(target.result)
        // 增量更新计算结果
        alog.update(wordArray)
        resolve()
      }
      reader.readAsArrayBuffer(blob)
    })
  }

  // 使用promise返回最终的计算结果
  return promise.then(() => alog.finalize())
};

export const fileSparkMD5 = (file) => {
  let chunkSize = 5 * 1024 * 1024;
  return new Promise((resolve, reject) => {
    let blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
    let chunks = Math.ceil(file.size / chunkSize);
    let currentChunk = 0;
    let spark = new SparkMD5.ArrayBuffer();
    let fileReader = new FileReader();

    fileReader.onload = function (e) {
      spark.append(e.target.result);
      currentChunk++;
      if (currentChunk < chunks) {
        loadNext();
      } else {
        let md5 = spark.end();
        resolve(md5);
      }
    };
    fileReader.onerror = function (e) {
      reject(e);
    };

    function loadNext() {
      let start = currentChunk * chunkSize;
      let end = start + chunkSize;
      if (end > file.size) {
        end = file.size;
      }
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
    }

    loadNext();
  });
}
