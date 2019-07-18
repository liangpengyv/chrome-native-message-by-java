/**
 * 监听 background.js
 */
chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {

    console.log(msg);

    if (msg.message === 'start selector') {
        startSelector();
    }
});

/**
 * 启动页面元素选择器
 */
function startSelector() {

    /**
     * 向页面插入浮动块，用来显示当前鼠标位置
     * @type {HTMLElement}
     */
    let showMousePoint = document.createElement('div');
    showMousePoint.style.display = 'none';
    showMousePoint.style.position = 'absolute';
    showMousePoint.style.zIndex = '999';
    showMousePoint.style.top = '10px';
    showMousePoint.style.left = '10px';
    showMousePoint.style.backgroundColor = 'white';
    showMousePoint.style.border = '1px solid black';
    showMousePoint.style.width = '100px';
    showMousePoint.style.height = '100px';
    showMousePoint.style.textAlign = 'center';
    document.body.appendChild(showMousePoint);

    /**
     * 向页面插入浮动块，用来遮罩当前框选的元素
     * @type {HTMLElement}
     */
    let selectorOverlay = document.createElement('div');
    selectorOverlay.style.position = 'absolute';
    selectorOverlay.style.zIndex = '999';
    selectorOverlay.style.border = '5px solid #ffe2ab';
    selectorOverlay.style.boxSizing = 'border-box';
    selectorOverlay.style.backgroundColor = '#c6d6ef';
    selectorOverlay.style.opacity = '0.5';
    selectorOverlay.style.pointerEvents = 'none';
    selectorOverlay.style.display = 'none';
    document.body.appendChild(selectorOverlay);

    /**
     * 鼠标“移动”事件处理
     * @param event
     */
    function mouseMoveEventHandler(event) {

        // 更新当前鼠标位置
        showMousePoint.style.display = 'block';
        showMousePoint.innerText = '\n鼠标位置\n\n' + event.pageX + ', ' + event.pageY;

        // 自定义鼠标样式
        const currentElement = document.elementFromPoint(event.pageX, event.pageY);
        currentElement.style.cursor = 'pointer';

        // 绘制元素框选
        if (currentElement === showMousePoint) {
            // 显示鼠标位置的浮动框移动到对角
            showMousePoint.style.top = document.body.offsetHeight - showMousePoint.offsetHeight - showMousePoint.offsetTop + 'px';
            showMousePoint.style.left = document.body.offsetWidth - showMousePoint.offsetWidth - showMousePoint.offsetLeft + 'px';
        } else {
            // 绘制元素选框
            selectorOverlay.style.width = currentElement.offsetWidth + 'px';
            selectorOverlay.style.height = currentElement.offsetHeight + 'px';
            selectorOverlay.style.top = currentElement.getBoundingClientRect().top + 'px';
            selectorOverlay.style.left = currentElement.getBoundingClientRect().left + 'px';
            selectorOverlay.style.display = 'block';
        }
    }

    /**
     * 鼠标“点击”事件处理
     * @param event
     */
    function mouseClickEventHandler(event) {

        const selectedElement = document.elementFromPoint(event.pageX, event.pageY);
        console.log(selectedElement);

        // 阻止捕获和冒泡阶段中当前事件的进一步传播
        event.stopPropagation();
        // 禁用元素默认动作
        event.preventDefault();

        // 向本地程序发送得到的数据
        const selectedElementData = {};
        selectedElementData.message =
            "title: " + document.title + "\n\n" +
            "url: " + document.documentURI + "\n\n" +
            "position: (" + event.pageX + ", " + event.pageY + ")\n\n" +
            "outHTML: " + selectedElement.outerHTML + "\n\n" +
            "and so on......";
        chrome.runtime.sendMessage(selectedElementData);

        // 销毁选择器创建的各种资源
        document.removeEventListener('mousemove', mouseMoveEventHandler);
        document.body.removeChild(showMousePoint);
        document.body.removeChild(selectorOverlay);
        document.removeEventListener('click', mouseClickEventHandler, true);
    }

    // 添加鼠标移动事件监听器
    document.addEventListener('mousemove', mouseMoveEventHandler);

    // 添加鼠标点击事件监听器
    document.addEventListener('click', mouseClickEventHandler, true);
}
