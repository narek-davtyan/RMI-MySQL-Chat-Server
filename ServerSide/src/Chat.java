import java.rmi.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Chat implements ChatInterface {     
	private String serverName;
	private ServerDB db;
	private List<UserInterface> conclients=null;
	private List<UserInterface> regclients=null;

	public Chat(String n, String USER, String PASS)  throws Exception { 
		db = new ServerDB(USER,PASS);
		conclients = new ArrayList<>();
		regclients = db.getClients();
		this.serverName=n;
	}

	@Override
	public String getServerName() throws RemoteException {
		return this.serverName;
	}

	@Override
	public void addClient(UserInterface c) throws RemoteException {
		conclients.add(c);
		if(!isClient(c.getName(),c.getPwd())){
			try {
				db.createClient(c);
				regclients.add(new User(c.getName(),c.getPwd()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		String msg = "[" + c.getName() + "] got connected";
		System.out.println(msg);
	}

	@Override
	public int getIdClient(String name) throws RemoteException {
		for (UserInterface user : conclients) {
			if(user.getName().equals(name))
				return conclients.indexOf(user);
		}
		return -1;
	}

	@Override
	public void send(int idUser, String s) throws RemoteException {
		try {
			db.storeMsg(conclients.get(idUser).getName(), s);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conclients.get(idUser).send(s);
	}

	@Override
	public List<String> list() throws RemoteException {
		List<String> list = new ArrayList<>();
		for (UserInterface user : conclients) {
			list.add(user.getName());
		}
		return list;
	}
	
	@Override
	public List<String> listReg() throws RemoteException {
		List<String> list = new ArrayList<>();
		for (UserInterface user : regclients) {
			list.add(user.getName());
		}
		return list;
	}

	@Override
	public boolean isClient(String name) throws RemoteException {
		for (UserInterface user : regclients) {
			if(user.getName().equals(name))
				return true;
		}
		return false;
	}

	@Override
	public boolean isClientCon(String name) throws RemoteException {
		if(name.equals("broad"))
			return true;
		for (UserInterface user : conclients) {
			if(user.getName().equals(name))
				return true;
		}
		return false;
	}

	@Override
	public void remClient(String name) throws RemoteException {
		conclients.remove(getIdClient(name));
		String msg = "[" + name + "] got disconnected";
		System.out.println(msg);
	}

	@Override
	public void broad(int idUser, String msg) throws RemoteException {
		try {
			db.storeMsg("broad", msg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for(int i=0;i<conclients.size();i++)
			if(i!=idUser)
				conclients.get(i).send("[broad " + msg.substring(1));	
	}

	@Override
	public boolean isClient(String name, String psw) throws RemoteException {
		for (UserInterface user : regclients) {
			if(user.getName().equals(name) && user.getPwd().equals(psw))
				return true;
		}
		return false;
	}

	@Override
	public UserInterface getLogClient(String name, String psw) throws RemoteException {
		for (UserInterface user : regclients) {
			if(user.getName().equals(name) && user.getPwd().equals(psw)) {
				String msg = "[" + user.getName() + "] got connected";
				System.out.println(msg);
				conclients.add(user);
				return user;
			}
		}
		return null;
	}

	@Override
	public List<String> history(String user1, String user2, int max_msg) throws RemoteException {
		if((isClient(user1) && isClient(user2)) ||  (user1.equals("broad") && user2.equals("broad")))
			try {
				return db.history(user1,user2,max_msg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return null;
	}

	@Override
	public boolean isValid(String name) throws RemoteException {
		if(name.equalsIgnoreCase("hist") || name.equalsIgnoreCase("help") ||
				name.equalsIgnoreCase("exit") || name.equalsIgnoreCase("broad") ||
				name.equalsIgnoreCase("show") || name.equalsIgnoreCase("username"))
			return false;
		return true;
	}	
}