$(document).ready(function () {

    // index.html 页面元素
    const sendMessageButton = $('#send-message-button');
    const inputElement = $('#input-text');
    const messageContent = $('#message-content');

    // 发送消息
    sendMessageButton.click(function () {

        const request = {};
        request.message = inputElement.val();

        const event = new CustomEvent('send-message-event', {
            detail: request,
            bubbles: true,  // 事件是否向上层冒泡
            cancelable: true  // 事件是否是可以取消的
        });
        document.dispatchEvent(event);  // 分派事件

        messageContent.html(messageContent.html() + '<br>[Send To Host:] ' + request.message);
    });

    // 获取消息事件监听器
    document.addEventListener('get-message-event', function (data) {

        const responseContent = data.detail;
        messageContent.html(messageContent.html() + '<br>[From Host:] ' + (responseContent === null ? 'Disconnected!' : responseContent));
    });
});
