module.exports = {
  publicPath: './', // 基本路径
  // outputDir: '../chat-spring/src/main/resources/static', // 输出文件目录
  devServer: {
    host: '0.0.0.0',
    port: 18889,
    proxy: {
      "/im": {
        ws: true,
        target: 'http://127.0.0.1:18888',
        //发送请求头host会被设置成target
        changeOrigin: true,
        //不重写请求地址
        pathRewrite: {
          '^/': '/'
        }
      }
    }
  }
}
