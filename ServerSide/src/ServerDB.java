import java.rmi.RemoteException;
import java.sql.*; 
import java.util.*;


public class ServerDB {
	private List<UserInterface> clients;
	private Connection conn = null;

	public ServerDB(String USER, String PASS) throws Exception {
		clients = new ArrayList<>();
		// JDBC database URL    
		String DB_URL = "jdbc:mysql://localhost:3306/ChatDB?autoReconnect=true&useSSL=false";

		//Register JDBC driver 
		Class.forName("com.mysql.jdbc.Driver");

		//Open a connection
		System.out.println("Connecting to a selected database..."); 
		conn = DriverManager.getConnection(DB_URL, USER, PASS); 
		System.out.println("Connected database successfully...");

		Statement stmt = conn.createStatement();  
		String sql = "SELECT * FROM User"; 
		ResultSet rs = stmt.executeQuery(sql); 

		//Extract data from result set 
		while(rs.next()){ 
			// Retrieve by column name 
			String name = rs.getString("name"); 
			String ps = rs.getString("password"); 

			// Setting the values 
			clients.add(new User(name,ps));
		}
	}

	public List<UserInterface> getClients() {
		return clients;
	}

	public void createClient(UserInterface c) throws SQLException, RemoteException {
		PreparedStatement pst = conn.prepareStatement("INSERT INTO User VALUES(?,?)");
		pst.setObject(1,c.getName(), Types.VARCHAR);
		pst.setObject(2, c.getPwd(), Types.VARCHAR);
		pst.executeUpdate();
	}

	public void storeMsg(String user2, String query) throws SQLException {
		// Retrieve usernames and the message 
		String user1 = query.substring(1,query.indexOf("]")).trim();
		String msg = query.substring(query.indexOf("]")+1,query.length()).trim();
		String idf;

		// Test if it is a broadcast message
		if(user2.equals("broad")) {
			idf = user2+":"+user2;
		} else {
			idf = (user1.compareToIgnoreCase(user2) < 0)? user1+":"+user2 : user2+":"+user1;
		}

		// Update the total number of messages
		PreparedStatement pst1 = conn.prepareStatement("INSERT INTO Messages (id_msgs,total)"
				+ " VALUES (?,  1) ON DUPLICATE KEY UPDATE total = total + 1");
		pst1.setObject(1, idf, Types.VARCHAR);
		pst1.executeUpdate();
		// Retrieve the updated total
		PreparedStatement pst2 = conn.prepareStatement("SELECT total FROM Messages"
				+ " where id_msgs=?");
		pst2.setObject(1, idf);
		ResultSet res = pst2.executeQuery();
		int id=-1;
		//Extract data from result set
		if(res.next())
			id = res.getInt("total");

		// Store the message
		PreparedStatement pst3 = conn.prepareStatement("INSERT INTO Message (id_msg,msg,sender)"
				+ " VALUES (?,?,?)");
		pst3.setObject(1, (idf+":"+id), Types.VARCHAR);
		pst3.setObject(2, msg, Types.VARCHAR);
		pst3.setObject(3, user1, Types.VARCHAR);
		pst3.executeUpdate();
	}

	List<String> history(String user1, String user2, int max_msg) throws SQLException {
		List<String> hist = new ArrayList<>();
		String idf;
		
		// Test if it is a broadcast history
		if(user2.equals("broad")) {
			idf = user2+":"+user2;
		} else {
			idf = (user1.compareToIgnoreCase(user2) < 0)? user1+":"+user2 : user2+":"+user1;
		}

		// Retrieve the updated total
		PreparedStatement pst1 = conn.prepareStatement("SELECT total FROM Messages"
				+ " where id_msgs=?");
		pst1.setObject(1, idf);
		ResultSet res1 = pst1.executeQuery();
		int size=-1;
		//Extract data from result set
		if(res1.next())
			size = res1.getInt("total");
		
		// If those two users have a history
		if(size!=-1) {
			PreparedStatement pst2 = conn.prepareStatement("SELECT * FROM Message"
					+ " where id_msg=?");
			for(int i=size; i>0 && size-i<max_msg; i--) {
				pst2.setObject(1, idf+":"+i);
				ResultSet res2 = pst2.executeQuery();
				if(res2.next()) {
					String msg = res2.getString("msg");
					String sender = res2.getString("sender");
					if(user2.equals("broad"))
						hist.add("broad "+sender+":"+msg);
					else
						hist.add(sender+":"+msg);
				}
			}
		}
		return hist;
	}
}
