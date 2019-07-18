package ink.laoliang.chrome;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;

public class Main extends JFrame {

    private static Main jFrame;
    private static JTextArea messageShowText;

    private static ObjectMapper mapper = new ObjectMapper();

    public Main() {

        super("Native Message Host");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setVisible(true);

        // 设置窗体布局
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);

        // 添加按钮，及按钮事件
        JButton selectorButton = new JButton("选取 Chrome 浏览器页面元素");
        selectorButton.addActionListener(new ButtonListener());
        panel.add(selectorButton);

        // 添加文本显示区
        messageShowText = new JTextArea();
        messageShowText.setLineWrap(true);
        panel.add(new JScrollPane(messageShowText));
    }

    public static void main(String[] args) throws IOException {

        SwingUtilities.invokeLater(() -> jFrame = new Main());

        while (true) {

            // 读取消息
            String messageStr = readMessage(System.in);
            MessageObject messageJson = mapper.readValue(messageStr, MessageObject.class);

            // 本地应用消息框展示读取到的消息
            messageShowText.setText(messageJson.getMessage());

            // 恢复应用窗口
            jFrame.setExtendedState(JFrame.NORMAL);
        }
    }

    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {

            try {
                // 构建并发送消息
                MessageObject messageJson = new MessageObject();
                messageJson.setMessage("start selector");
                String messageStr = mapper.writeValueAsString(messageJson);
                sendMessage(messageStr);

                // 最小化应用窗口
                jFrame.setExtendedState(JFrame.ICONIFIED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readMessage(InputStream in) throws IOException {

        byte[] messageLength = new byte[4];
        in.read(messageLength); // 读取消息大小

        int size = getInt(messageLength);
        if (size == 0) {
            throw new InterruptedIOException("Blocked communication");
        }

        byte[] messageContent = new byte[size];
        in.read(messageContent);

        return new String(messageContent, StandardCharsets.UTF_8);
    }

    private static void sendMessage(String message) throws IOException {
        System.out.write(getBytes(message.length()));
        System.out.write(message.getBytes(StandardCharsets.UTF_8));
        System.out.flush();
    }

    private static int getInt(byte[] bytes) {
        return (bytes[3] << 24) & 0xff000000 |
                (bytes[2] << 16) & 0x00ff0000 |
                (bytes[1] << 8) & 0x0000ff00 |
                (bytes[0]) & 0x000000ff;
    }

    private static byte[] getBytes(int length) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (length & 0xFF);
        bytes[1] = (byte) ((length >> 8) & 0xFF);
        bytes[2] = (byte) ((length >> 16) & 0xFF);
        bytes[3] = (byte) ((length >> 24) & 0xFF);
        return bytes;
    }
}
