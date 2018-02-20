import java.rmi.*;
import java.util.List;

public interface ChatInterface extends Remote{
	public String getServerName() throws RemoteException;
	public void send(int idUser, String msg) throws RemoteException;
	public void broad(int idUser, String msg) throws RemoteException;
	public List<String> list() throws RemoteException;
	public List<String> listReg() throws RemoteException;
	public void addClient(UserInterface c)throws RemoteException;
	public int getIdClient(String name) throws RemoteException;
	public UserInterface getLogClient(String name, String psw) throws RemoteException;
	public void remClient(String name) throws RemoteException;
	public boolean isClient(String name) throws RemoteException;
	public boolean isValid(String name) throws RemoteException;
	public boolean isClientCon(String name) throws RemoteException;
	public boolean isClient(String name, String psw) throws RemoteException;
	public List<String> history(String user1, String user2, int max_msg) throws RemoteException;
}