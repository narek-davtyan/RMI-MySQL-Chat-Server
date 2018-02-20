import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.*;

public class ChatServer {
	private static Scanner s;

	public static void main (String[] argv) {
		try {
			if (argv.length != 3) {
                System.out.println("Usage: java ChatServer <database user> <database password> <ip address>");
                return;
            }
			String user = argv[0];
			String pass = argv[1];
			String ip = argv[2];
			s = new Scanner(System.in);
			System.out.println("Enter Your name and press Enter:");
			String name=s.nextLine().trim();
			Chat server = new Chat(name,user,pass);		
			ChatInterface serv_stub = (ChatInterface) UnicastRemoteObject.exportObject(server, 0);
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			Naming.bind("rmi://"+ip+":1099/IDS", serv_stub);
			System.out.println("\n"+"["+server.getServerName()+"]"+" Chat server is ready:");
		}catch (Exception e) {
			System.out.println("[System] Server failed: " + e);
			e.printStackTrace();
		}
	}
}