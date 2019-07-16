/**
 * 设置 send-message-event 监听事件
 * 将消息数据发送到 background.js
 */
document.addEventListener('send-message-event', function (data) {

    const request = data.detail;
    console.log(request);

    // 发送消息到 background.js
    chrome.runtime.sendMessage(request);
});

/**
 * 监听后台脚本，将收到的消息通过 get-message-event 事件发送给前端页面
 */
chrome.runtime.onMessage.addListener(function (response, sender, sendResponse) {

    console.log(response);

    // 发送响应到前端页面
    const event = new CustomEvent('get-message-event', {
        detail: response.message,
        bubbles: true,  // 事件是否向上层冒泡
        cancelable: true  // 事件是否是可取消的
    });
    document.dispatchEvent(event);  // 分派事件
});
