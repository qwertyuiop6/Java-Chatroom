package MainChat;

import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
    private Socket socket;
    private ClientGUI cg;
    private String server, username;
    private int port;

    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

    /*
     * 开始连接
     */
    public boolean start() {
        // 连接server
        try {
            socket = new Socket(server, port);
        }
        // 失败
        catch(Exception ec) {
            display("连接到Server异常:" + ec);
            return false;
        }

        String msg = "连接到 " + socket.getInetAddress() + ":" + socket.getPort()+"成功";
        display(msg);

        /* new 两个数据流 */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("创建I/O流异常: " + eIO);
            return false;
        }

        // 创建线程监听服务器信息
        new ListenFromServer().start();
        // 发送一个username的String信息，
        // 其他信息以ChatMessage对象形式发送
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("连接时发生错误: " + eIO);
            disconnect();
            return false;
        }
        // 通知GUI连接成功
        return true;
    }


    //展示信息到GUI里
    private void display(String msg) {
        cg.append(msg + "\n");
    }

    /*
     * 发送信息到Server
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("向服务器发送信息异常" + e);
        }
    }

    /*
     *
     * 断开连接和I/O流
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}
        if(cg != null)
            cg.connectionFailed();
    }
//    /*
//     * To start the Client in console mode use one of the following command
//     * > java Client
//     * > java Client username
//     * > java Client username portNumber
//     * > java Client username portNumber serverAddress
//     * at the console prompt
//     * If the portNumber is not specified 1500 is used
//     * If the serverAddress is not specified "localHost" is used
//     * If the username is not specified "Anonymous" is used
//     * > java Client
//     * is equivalent to
//     * > java Client Anonymous 1500 localhost
//     * are eqquivalent
//     *
//     * In console mode, if an error occurs the program simply stops
//     * when a GUI id used, the GUI is informed of the disconnection
//     */

//    public static void main(String[] args) {
//        // default values
//        int portNumber = 2333;
//        String serverAddress = "localhost";
//        String userName = "Anonymous";
//
////        // depending of the number of arguments provided we fall through
////        switch(args.length) {
////            // > javac Client username portNumber serverAddr
////            case 3:
////                serverAddress = args[2];
////                // > javac Client username portNumber
////            case 2:
////                try {
////                    portNumber = Integer.parseInt(args[1]);
////                }
////                catch(Exception e) {
////                    System.out.println("Invalid port number.");
////                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
////                    return;
////                }
////                // > javac Client username
////            case 1:
////                userName = args[0];
////                // > java Client
////            case 0:
////                break;
////            // invalid number of arguments
////            default:
////                System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
////                return;
////        }
//
//        // create the Client object
//        Client client = new Client(serverAddress, portNumber, userName);
//        // test if we can start the connection to the Server
//        // if it failed nothing we can do
//        if(!client.start())
//            return;
//
//        // wait for messages from user
//        Scanner scan = new Scanner(System.in);
//        // loop forever for message from the user
//        while(true) {
//            System.out.print("> ");
//            // read message from user
//            String msg = scan.nextLine();
//            // logout if message is LOGOUT
//            if(msg.equalsIgnoreCase("LOGOUT")) {
//                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
//                // break to do the disconnect
//                break;
//            }
//            // message WhoIsIn
//            else if(msg.equalsIgnoreCase("WHOISIN")) {
//                client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
//            }
//            else {              // default to ordinary message
//                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
//            }
//        }
//        // done disconnect
//        client.disconnect();
//    }

    /*
     * 等待服务端消息并发送到GUI展示
     *
     */
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    //int o_num=sInput.read();
                    //cg.append2(o_num);
                    cg.append(msg);
                }
                catch(IOException e) {
                    display("您已经与服务器断开了连接。 ");
                    cg.connectionFailed();
                    break;
                }
                //虽然传的是String 但必须捕获类异常
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}
