# Java-RMI-MySQL-Chat
A Chat application using java RMI and MySql database

### A first-year master project :mortar_board: :one:  

This is a project to build a client-server chat application using java RMI and MySql  
The directories included in this repo:  
* ChatServer: The eclipse project directory  
	- ClientSide: All relevant code for the client chat GUI and console mode.  
	- ServerSide: The code for the MySql RMI server.  
* database: ChatDB a MySQL database backup script  

### Features:  
- Clients login to the system with a username and a password (sign in) 
- Clients can create a new account by giving a username and a password (sign up)
- Clients can send a broadcast chat message (chat room discussion)
- Clients can send a private message to connected clients  
- Server maintains a connected user list and registered user list
- Server stores the chat history and account informations of each client
- Online user list is updated on all clients when users join or leave the chat  
- Registred user list is updated on a new client subscription

### Software requirements 
- MySql database
- Java JDK8 (with JavaFX or OpenJFX) and RMI
- Apache Ant(TM)

### Instructions
* Create the MySql database (ChatDB) from the backup script
* Inside ServerSide dir:
	- build the server files using ant: sudo ant
	- launch the chat server:  sudo ant -emacs -Duser="databaseUser" -Dpass="databasePassword" run  	
	<br/> the server is launched on the default LAN(localhost)
	- to launch the server on a precise network you need to provide the ip address:
	<br/> sudo ant -emacs -Duser="databaseUser" -Dpass="databasePassword" -Dip="ip_address" run
* Inside ClientSide dir:
	- build the client files using ant: sudo ant
	- launch the chat client: sudo ant -emacs run
		<br/> the client is launched on the default LAN(localhost)
	- to launch the client on a precise network you have to provide the ip address:
		<br/> sudo ant -emacs -Dip="ip address" run   	
* you have the choice to launch the client on the console mode (run target) or GUI mode (runGUI target):
		<br/> (sudo ant -q -Dip="ip_address" runGUI) or (sudo ant -q runGUI)


<hr />
<img src="https://github.com/narek-davtyan/RMI-MySQL-Chat-Server/blob/master/ClientSide/img/home1.png" width="400">
<HR />
<img src="https://github.com/narek-davtyan/RMI-MySQL-Chat-Server/blob/master/ClientSide/img/home2.png" width="400">
<hr />
<img src="https://github.com/narek-davtyan/RMI-MySQL-Chat-Server/blob/master/ClientSide/img/home3.png" width="400">
<HR />
<img src="https://github.com/narek-davtyan/RMI-MySQL-Chat-Server/blob/master/ClientSide/img/chat.png" width="400">
<hr />
<img src="https://github.com/narek-davtyan/RMI-MySQL-Chat-Server/blob/master/ClientSide/img/console.png" width="400">


