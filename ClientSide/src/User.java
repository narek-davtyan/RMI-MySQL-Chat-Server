import java.rmi.*;
import java.rmi.server.*;

public class User extends UnicastRemoteObject implements UserInterface {

    private static final long serialVersionUID = UserInterface.serialVersionUID;
    private final boolean hasGUI;
    ChatParts chatApplication;
    private String name;
    private String pwd;

    public ChatInterface client = null;

    public User(String n, ChatParts chatApplication) throws RemoteException {
        this.chatApplication = chatApplication;
        this.name = n;
        hasGUI = true;
    }

    public User(String n, String pass, ChatParts ch) throws RemoteException {
        this.chatApplication = ch;
        this.name = n;
        this.pwd = pass;
        hasGUI = true;
    }

    public User(String n) throws RemoteException {
        this.name = n;
        hasGUI = false;
    }

    public User(String n, String pass) throws RemoteException {
        this.name = n;
        this.pwd = pass;
        hasGUI = false;
    }

    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    public void setClient(ChatInterface c) {
        client = c;
    }

    public ChatInterface getClient() {
        return client;
    }

    @Override
    public void send(String s) throws RemoteException {
        if (hasGUI) {
            chatApplication.newMessageReceived(s);
        } else {
            System.out.println(s);
        }
    }

    @Override
    public String getPwd() throws RemoteException {
        return pwd;
    }
}