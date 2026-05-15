const drag = {
  // 指令的定义
  bind: function (el) {
    let div = el;   //获取当前元素
    div.onmousedown = (e) => {
      //算出鼠标相对元素的位置
      let disX = e.clientX - div.offsetLeft;
      let disY = e.clientY - div.offsetTop;

      document.onmousemove = (e) => {
        //用鼠标的位置减去鼠标相对元素的位置，得到元素的位置
        let left = e.clientX - disX;
        let top = e.clientY - disY;

        //绑定元素位置到positionX和positionY上面
        div.positionX = top;
        div.positionY = left;

        //移动当前元素
        div.style.left = left + 'px';
        div.style.top = top + 'px';
      };
      document.onmouseup = (e) => {
        document.onmousemove = null;
        document.onmouseup = null;
      };
    };
  }
}

export {drag}
