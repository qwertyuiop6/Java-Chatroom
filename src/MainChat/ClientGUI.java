package MainChat;

import MainChat.ChatMessage;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;


public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JLabel label;
    private JTextField tf;

    private JTextField tfServer, tfPort;
    private JButton login, logout, submit,onlineb;

    private JTextArea ta;
    private JTextField tb;
    private JLabel online_num;

    private boolean connected;

    private Client client;
    private int defaultPort;
    private String defaultHost;


    ClientGUI(String host, int port) {

        super("Swing简易聊天室");
        defaultPort = port;
        defaultHost = host;

        // 顶部Server和端口面板
        JPanel northPanel = new JPanel(new GridLayout(2,1,1,5));
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 5, 3));

        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server地址:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("端口地址:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        northPanel.add(serverAndPort);
        northPanel.add(new JLabel("<html><body><div style='color:#4b5154;font-size:11px'>欢迎来到Swing聊天室!</div></body></html>", SwingConstants.CENTER));

        add(northPanel, BorderLayout.NORTH);


        // 中间的分割面板
        JPanel centerPanel = new JPanel(new GridLayout(1,1));

        ta = new JTextArea("", 80, 50);
        ta.setEditable(false);
        centerPanel.add(new JScrollPane(ta));
        //信息展示框在左边面板
//        centerPanel.setLeftComponent(new JScrollPane(ta));
//
//        JPanel onlinePanel = new JPanel();
//        JLabel online=new JLabel("在线人员:");
//        online_num=new JLabel("0");
//        JTextArea onlinel=new JTextArea("wwww",20,23);
//        onlinel.setEditable(false);
//        onlinePanel.add(online);
//        onlinePanel.add(online_num);
//        onlinePanel.add(new JScrollPane(onlinel));
//
//        //在线情况加到右边面板
//        centerPanel.setRightComponent(onlinePanel);

//        centerPanel.setDividerLocation(530);
//        centerPanel.setOneTouchExpandable(true);
        add(centerPanel, BorderLayout.CENTER);


        // 昵称和登陆面板
        JPanel namepanel=new JPanel(new GridLayout(1,7, 7, 7));
        label = new JLabel("<html><body><div style='color:#ca1d12'>请输入昵称:</div></body></html>", SwingConstants.CENTER);
        //label.setForeground(Color.BLUE);
        double d = Math.random();
        final int i = (int)(d*100);
        tf = new JTextField("匿名者"+i);
        tf.setBackground(Color.WHITE);
        namepanel.add(label);
        namepanel.add(tf);
        namepanel.add(new JLabel(""));

        submit = new JButton("<html><body><div style='color:#14a53b'>发送</div></body></html>");
        submit.addActionListener(this);
        onlineb= new JButton("<html><body><div style='color:#1289c3'>在线人员</div></body></html>");
        onlineb.addActionListener(this);
        login = new JButton("<html><body><div style='color:#1289c3'>连接</div></body></html>");
        login.addActionListener(this);
        logout = new JButton("<html><body><div style='color:#e22518'>断开</div></body></html>");
        logout.addActionListener(this);
        //禁用一些按钮在没登录之前
        onlineb.setEnabled(false);
        submit.setEnabled(false);
        logout.setEnabled(false);

        namepanel.add(submit);
        namepanel.add(onlineb);
        namepanel.add(login);
        namepanel.add(logout);
        //信息输入框
        tb = new JTextField("请先输入昵称连接后聊天...");
        tb.setEditable(false);

        //底部面板
        JPanel southPanel =new JPanel(new GridLayout(2,1, 5, 3));
        southPanel.add(tb);
        southPanel.add(namepanel);
        add(southPanel, BorderLayout.SOUTH);

        //窗口设置
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 520);
        setVisible(true);
        setLocationRelativeTo(null);
        tf.requestFocus();
    }

    // 信息展示框展示消息
    void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    void append2(int o_num){
        online_num.setText(Integer.toString(o_num));
    }

    //连接失败组件变化函数
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        submit.setEnabled(false);
        onlineb.setEnabled(false);

        //whoIsIn.setEnabled(false);
        label.setText("请输入昵称:");
        tf.setText("");
        tb.setEditable(false);

        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        tfServer.setEditable(false);
        tfPort.setEditable(false);

        tf.removeActionListener(this);
        connected = false;
    }


    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        // 点击到登出的事件
        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            tb.setEditable(false);
            tf.setEditable(true);
            submit.setEnabled(false);
            onlineb.setEnabled(false);
            tb.removeActionListener(this);
            tb.setText("请先输入昵称连接后聊天...");
            return;
        }
        // 查看在线人员事件
        if(o == onlineb) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            return;
        }
        // 默认发送的事件
        if(connected) {
            String tbt = tb.getText().trim();
            if(tbt.length()==0){
                ta.append("消息不能为空!\n");
                return;
            }
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tb.getText()));
            tb.setText("");
            return;
        }

        //连接按钮事件触发
        if(o == login) {
            String username = tf.getText().trim();
            if(username.length() == 0){
                ta.append("昵称不能为空!\n");
                return;
            }
            String server = tfServer.getText().trim();
            if(server.length() == 0){
                ta.append("Server地址不能为空!\n");
                return;
            }
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0){
                ta.append("端口不能为空!\n");
                return;
            }
            //端口号判断
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                ta.append("端口号不合法!\n");
                return;
            }

            // 创建GUI客户端连接
            client = new Client(server, port, username, this);
            //是否启动成功
            if(!client.start())
                return;

            tf.setText(username);
            tf.setEditable(false);
            label.setText("您的聊天昵称：");
            tb.setEditable(true);
            tb.setText("输入消息...");
            connected = true;

            // 禁用连接按钮
            login.setEnabled(false);
            // 启用3按钮
            submit.setEnabled(true);
            logout.setEnabled(true);
            onlineb.setEnabled(true);

            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // 监听信息发送事件
            tb.addActionListener(this);
        }

    }

    // 客户端GUI入口
    public static void main(String[] args) {
        try
        {
            //windows组件外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            InitGlobalFont(new Font("", Font.ROMAN_BASELINE, 13));

        }catch(Exception e) {

        }
        //构造客户端GUI
        new ClientGUI("localhost", 2333);
    }
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}

