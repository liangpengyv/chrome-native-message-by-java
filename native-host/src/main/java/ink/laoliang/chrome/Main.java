package ink.laoliang.chrome;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;

public class Main extends JFrame {

    private static Main jframe;
    private static JTextField messageInputText;
    private static JTextArea messageShowText;

    private static ObjectMapper mapper = new ObjectMapper();

    public Main() {

        super("Native Message Host");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setVisible(true);

        // 设置窗体布局　
        setLayout(new FlowLayout());

        // 添加标签
        JLabel label = new JLabel("Input Message: ");
        add(label);

        // 添加文本输入框
        messageInputText = new JTextField("Hey, I am Host.", 10);
        add(messageInputText);

        // 添加按钮，及按钮事件
        JButton sendButton = new JButton("Send");
        add(sendButton);
        sendButton.addActionListener(new ButtonListener());

        // 添加文本显示区
        messageShowText = new JTextArea("[Message Content...]\n");
        add(messageShowText);
    }

    public static void main(String[] args) throws IOException {

        SwingUtilities.invokeLater(() -> jframe = new Main());

        while (true) {

            // 读取消息
            String messageStr = readMessage(System.in);
            MessageObject messageJson = mapper.readValue(messageStr, MessageObject.class);

            // 本地应用消息框展示读取到的消息
            messageShowText.setText(messageShowText.getText() + "\n[From Chrome:] " + messageJson.getMessage());
        }
    }

    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {

            try {
                // 构建并发送消息
                MessageObject messageJson = new MessageObject();
                messageJson.setMessage(messageInputText.getText());
                messageShowText.setText(messageShowText.getText() + "\n[Send To Chrome:] " + messageJson.getMessage());
                String messageStr = mapper.writeValueAsString(messageJson);
                sendMessage(messageStr);
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
