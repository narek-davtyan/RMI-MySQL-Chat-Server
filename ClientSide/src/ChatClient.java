import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ChatClient {
	// Class used to have a fancy output
	public static class Log {

	    public static final String ANSI_RESET = "\u001B[0m";
	    public static final String ANSI_RED = "\u001B[31m";
	    public static final String ANSI_GREEN = "\u001B[32m";
	    public static final String ANSI_YELLOW = "\u001B[33m";
	    public static final String ANSI_BLUE = "\u001B[34m";
	    public static final String ANSI_PURPLE = "\u001B[35m";
	    public static final String ANSI_CYAN = "\u001B[36m";
	    public static final String ANSI_WHITE = "\u001B[37m";

	    //info
	    public static void i(String message) {
	        System.out.println(ANSI_CYAN + message + ANSI_RESET);
	    }

	    //error
	    public static void e(String message) {
	        System.out.println(ANSI_RED + message + ANSI_RESET);
	    }

	    //debug
	    public static void d(String message) {
	        System.out.println(ANSI_BLUE + message + ANSI_RESET);
	    }

	    //warning
	    public static void w(String message) {
	        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
	    }

	    //result
	    public static void r(String message) {
	        System.out.println(ANSI_PURPLE + message + ANSI_RESET);
	    }

	}

	public static void main(String[] argv) {
		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			String ip = "localhost";
			if(argv.length == 1) {
				ip = argv[0];
			}
			ChatInterface server = (ChatInterface) Naming.lookup("rmi://"+ip+":1099/IDS");
			UserInterface client = null;
			Scanner s = new Scanner(System.in);
			String wel = "\t***************************************************************************\n"+
					"\t\t\t\tWelcome to "+server.getServerName()+" chat server \n"+
					"\t\t\t\t\tSign up[0]\n"+
					"\t\t\t\t\tSign in[1]\n";
			boolean connected = false;
			String name=null;
			Log.i(wel);
			String nm = s.nextLine().trim();
			while(!connected) {
				if(nm.equals("0")) {
					Log.i("\tEnter your username:");
					name = s.nextLine().trim();
					while(server.isClient(name) || !server.isValid(name)) {
						if(server.isClient(name))
							Log.w("\n"+"\t["+server.getServerName()+"] "+"Please enter another username\n"+
									"'"+name+"'"+" is already used");
						else
							Log.w("\n"+"\t["+server.getServerName()+"] "+"Please enter another username\n"+
									"'"+name+"'"+" is a reserved keyword");
						name = s.nextLine().trim();
					}
					Log.i("\tEnter your password:");
					String pass = s.nextLine().trim();
					client = new User(name,pass);
					server.addClient(client);
					connected = true;
				}else if(nm.equals("1")){
					Log.i("\tEnter your username to login:");
					name = s.nextLine().trim();
					int attempts = 0;
					while(attempts < 3 && (!server.isClient(name) || server.isClientCon(name))) {
						Log.w("\n"+"\t["+server.getServerName()+"] "+
								"Enter another username\n"+
								"'"+name+"'"+" is not valid");
						name = s.nextLine().trim();
						attempts++;
					}
					if(server.isClient(name)  && !server.isClientCon(name)) {
						Log.i("\n\tEnter your password:");
						String psw = s.nextLine().trim();
						attempts = 0;
						while(attempts < 3 && !server.isClient(name, psw)) {
							Log.w("\n"+"\t["+server.getServerName()+"] "+"Sorry, try again.\n");
							psw = s.nextLine().trim();
							attempts++;
						}
						if(server.isClient(name, psw)) {
							connected = true;
							client = new User(name,psw);
							server.addClient(client);
						} else {
							Log.e("\n\t"+"["+server.getServerName()+"] "+"4 incorrect password attempts.\n");
							Log.i(wel);
							nm = s.nextLine().trim();
						}
					} else if(server.isClientCon(name)){
						Log.w("\n\t"+"["+server.getServerName()+"] "+"Sorry, this user is already connected.\n");
						Log.i(wel);
						nm = s.nextLine().trim();
					} else {
						Log.e("\n\t"+"["+server.getServerName()+"] "+"4 incorrect username attempts.\n");
						Log.i(wel);
						nm = s.nextLine().trim();
					}
				} else {
					Log.d("\t"+nm+" : command not found\n\n");
					Log.i(wel);
					nm = s.nextLine().trim();
				}
			}

			Log.i("\n\tConnected with the username "+name);
			String menu = "\n\t*********************************** Menu ***********************************\n"+
					"\tshow : show the list of connected users\n"+
					"\tusername : show your username\n"+
					"\thist broad <max>: show the last <max> broadcasted history messages\n"+
					"\thist <username> <max>: show the last <max> history messages with <username>\n"+
					"\t<username>:<message>  : send the message to username\n"+
					"\tbroad:<message>  : send the message to everyone\n"+
					"\thelp : show the menu\n"+
					"\texit : disconnect from the chat server\n"+
					"\t****************************************************************************\n\n";
			Log.i(menu);
			String userInput;
			while(connected){	
				userInput = s.nextLine().trim();
				if(userInput.equalsIgnoreCase("help")) {
					Log.r(menu);
				} else if(userInput.equalsIgnoreCase("show")) {
					Log.r("\t"+server.list().toString());
				} else if(userInput.equalsIgnoreCase("username")) {
					Log.r("\t"+client.getName());
				} else if(userInput.equalsIgnoreCase("exit")) {
					server.remClient(client.getName());
					UnicastRemoteObject.unexportObject(client,connected);
					s.close();
					connected = false;
				} else if(userInput.contains(":")) {
					String user = userInput.substring(0,userInput.indexOf(":")).trim();
					if(!server.isClientCon(user)) {
						Log.e("\t"+user+" is not a valid username");
					} else {
						String msg = userInput.substring(userInput.indexOf(":")+1).trim();
						msg = "[" + client.getName() + "] " + msg;
						if(user.equalsIgnoreCase("broad"))
							server.broad(server.getIdClient(client.getName()), msg);
						else
							server.send(server.getIdClient(user), msg);
					}		
				} else if(userInput.substring(0,(userInput.indexOf(" "))==-1?
						userInput.length():userInput.indexOf(" ")).equals("hist")) {
					String[] input = userInput.split("\\s+");
					List<String> list=null;
					if(input.length ==3 && input[1].equals("broad")) {
						try {
							if(Integer.parseInt(input[2])<=0)
								throw new NumberFormatException();
							list = server.history("broad", "broad", Integer.parseInt(input[2]));
							for(String str:list)
								list.set(list.indexOf(str),str.substring(6));
						}catch(NumberFormatException e) {
							Log.e("\n\t"+"["+server.getServerName()+"] "+"usage :\n"+
									"<max> has to be a positive number");
						}
					} else if(input.length ==3){
						try {
							if(Integer.parseInt(input[2])<=0)
								throw new NumberFormatException();
							list = server.history(client.getName(), input[1], Integer.parseInt(input[2]));
						}catch(NumberFormatException e) {
							Log.e("\n\t"+"["+server.getServerName()+"] "+"usage :\n"+
									"\t<max> has to be a positive number");
						}
					}
					if(list == null) {
						Log.w("\n\t"+"["+server.getServerName()+"] "+"usage :\n"+
								"\thist <username> <max>: show the last <max> history messages with <username>\n"+
								"\thist broad <max>: show the last <max> broadcasted history messages\n");
					} else {
						for(int i=list.size()-1; i>=0;--i)
							Log.r(list.get(i));
						System.out.println();
					}
				} else {
					Log.e("\n\t"+"["+server.getServerName()+"] "+userInput + " : command not found\n\n");
				}
			}
		} catch (Exception e) {
			Log.e("\t[System] Server failed: " + e);
			e.printStackTrace();
		}
	}
}