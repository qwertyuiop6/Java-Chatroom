package MainChat;

import MainChat.ChatMessage;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static int uniqueId;
    private ArrayList<ClientThread> al;
    private int onum;
    private SimpleDateFormat sdf;
    private SimpleDateFormat sdf2;
    private int port;
    private boolean keepGoing;

    //服务端构造
    public Server(int port) {
        this.port = port;
        sdf = new SimpleDateFormat("a hh:mm:ss");
        sdf2=new SimpleDateFormat("E ahh:mm");
        // 客户端列表动态数组
        al = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        try
        {
            // 服务端用的socket
            ServerSocket serverSocket = new ServerSocket(port);
            display("服务端正在监听端口：" + port + ".");

            // 一直等待连接
            while(keepGoing)
            {

                Socket socket = serverSocket.accept();      // 接受连接
                ClientThread t = new ClientThread(socket);  // 新建一个线程
                al.add(t);                                  // 将其加入数组
                onum=al.size();
                t.start();
                broadcast(t.username+" 进入了房间\n当前在线"+onum+"人");

            }
        }

        catch (IOException e) {
            String msg = sdf.format(new Date()) + " 创建新的ServerSocket异常: " + e + "\n";
            display(msg);
        }
    }


    /*
     * 控制台展示事件(非消息)
     */
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);

    }

    /*
     *  向所有客户端广播消息
     */
    private synchronized void broadcast(String message) {
        // 添加时间
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        // 控制台展示消息
        System.out.print(messageLf);


        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            // 向客户端发消息,失败则删除
            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("客户端 " + ct.username + " 丢失连接，已从列表移除");
            }
        }
    }


    synchronized void remove(int id) {
        // 扫描数组寻找退出的ID
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);

            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    //主函数
    public static void main(String[] args) {
        // 默认端口2333 除非新指定
        int portNumber = 2333;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("端口不合法！");
                    System.out.println("用法: > java Server [端口]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("用法: > java Server [端口]");
                return;

        }
        //创建服务端对象并启动
        Server server = new Server(portNumber);
        server.start();
    }



    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        int id;
        // 客户端的名称
        String username;
        //
        ChatMessage cm;
        // 连接日期
        String date;

        // 构造
        ClientThread(Socket socket) {
            // 唯一ID
            id = ++uniqueId;
            this.socket = socket;
            System.out.println("尝试创建I/O流");
            try
            {
                // 首先创建输出
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // 输入读用户名
                username = (String) sInput.readObject();
                display(username + " 已连接");

            }
            catch (IOException e) {
                display("创建I/O流异常: " + e);
                return;
            }
            // 捕捉ClassNotFoundException，虽然不会发生
            catch (ClassNotFoundException e) {
            }
            date = sdf2.format(new Date()) + "\n";
        }

        // 永远运行
        public void run() {
            // 循环直到登出
            boolean keepGoing = true;
            while(keepGoing) {
                // 读取一个字符串（对象）
                try {
                    cm = (ChatMessage) sInput.readObject();

                }
                catch (IOException e) {
                    display(username + " 读取输入流失败: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // ChatMessage的message
                String message = cm.getMessage();

                // 选择信息类型
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(username + " 说: \n" + message);
                        break;
                    case ChatMessage.LOGOUT:
                        //display(username + " 退出了房间");
                        //broadcast(username+"离开了房间");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg(sdf.format(new Date()) +" 当前在线用户:" +  "\n");

                        for(int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg("("+(i+1) + ") " + ct.username + " 连接时间:" + ct.date);
                        }
                        break;
                }
            }
            // 从所在列表删除
            remove(id);
            broadcast(username+" 离开了房间\n当前在线"+al.size()+"人");
            close();
        }

        // 关闭所有
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * 写一个字符串到输出流
         */
        private boolean writeMsg(String msg) {
            // 如果客服端连接则发消息给它
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // 发消息到流
            try {
                sOutput.writeObject(msg);
            }
            // 如果发生错误，展示事件
            catch(IOException e) {
                display("发送消息给" + username+"时产生异常");
                display(e.toString());
            }
            return true;
        }
    }
}
