import java.rmi.*;
import java.rmi.server.*;

public class User extends UnicastRemoteObject implements UserInterface  {
	private static final long serialVersionUID = UserInterface.serialVersionUID;
	private String name;
	private String pwd;
	
	public ChatInterface client=null;

	public User(String n)  throws RemoteException { 
		this.name=n;   
	}
	
	public User(String n, String pass)  throws RemoteException { 
		this.name=n;
		this.pwd = pass;
	}
	
	public String getName() throws RemoteException {
		return this.name;
	}

	public void setClient(ChatInterface c){
		client=c;
	}

	public ChatInterface getClient(){
		return client;
	}

	public void send(String s) throws RemoteException{
		System.out.println(s);
	}

	@Override
	public String getPwd() throws RemoteException {
		return pwd;
	}	
}