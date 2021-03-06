package CSE310.ChatApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Created by jim on 4/21/17.
 */
public class UserListPane extends JPanel implements UserStatusListener {


    private final ChatClient client;
    public JList<String> userListUI;
    public DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, login);

                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500, 500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 8818);

        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);

        if (client.connect()) {
            try {
                client.login(client.username, client.password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}