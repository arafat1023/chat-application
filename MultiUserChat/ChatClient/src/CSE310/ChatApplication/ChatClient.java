package CSE310.ChatApplication;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String username, password;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public ChatClient(String serverName, int serverPort, String username, String password) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.username = username;
        this.password = password;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener((fromLogin, msgBody) -> System.out.println("You got a message from " + fromLogin + " ===>" + msgBody));

        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            System.out.println("Connect successful");
            if (client.login(client.username, client.password)) {
                System.out.println("Login successful");
                client.msg(client.username, "Hello World!");
            } else {
                System.err.println("Login failed");
            }
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        if("logoff".equalsIgnoreCase(msgBody)){
            //System.out.println("Arafat ke mari ai");
            logoff();
            getUserStatusListener(this.username);
        }
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        String sender = this.username;
        serverOut.write(cmd.getBytes());
        new Thread(() -> {
            connection conn = new connection();
            Connection connectClass = null;
            try {
                connectClass = conn.getconnection();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            String sql = "INSERT INTO `message` (sender_username, receiver_username, messages, sent) VALUES ('" +
                    sender + "', '" +
                    sendTo + "', '" +
                    msgBody + "', " +
                    "UTC_TIME()" +
                    ");";
            System.out.print("Addign msg to db: ");
            System.out.println(sql);
            Statement statement = null;
            try {
                statement = connectClass.createStatement();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                statement.executeUpdate(sql);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }



    public boolean login(String login, String password) throws IOException {
        if (!this.credentialCheck(login, password))
            return false;

        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public boolean credentialCheck(String username, String password) {
        connection conn = new connection();
        Connection connectClass = null;
        try {
            connectClass = conn.getconnection();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        String sql = "SELECT * FROM `user` WHERE username='" + username + "' AND password='" + password + "';";
        System.out.println(sql);
        Statement statement = null;
        try {
            statement = connectClass.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        ResultSet result = null;
        try {
            result = statement.executeQuery(sql);
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public void getUserStatusListener(String user)
    {

        for (UserStatusListener users :userStatusListeners
             ) {

                if(users.equals(user)){removeUserStatusListener(users);

                }

        }

        }

    }

