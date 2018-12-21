package CSE310.ChatApplication;

public interface UserStatusListener {
    public void online(String login);
    public void offline(String login);
}
