package CSE310.ChatApplication;

public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);
}
