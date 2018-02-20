import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserInterface extends Remote{
	static final long serialVersionUID = 1L;
	public String getName() throws RemoteException;
	public String getPwd() throws RemoteException;
	public void send(String msg) throws RemoteException;
}