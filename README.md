# Java-RMI-MySQL-Chat
A Chat application built with java RMI and MySql database

### A first-year master project :mortar_board: :one:  

This is a project to build a client-server chat application using java RMI and MySql  
The directories included in this repo:  
* ChatServer: The eclipse project directory  
	- ClientSide: all code for the client chat (GUI and console mode)  
	- ServerSide: all code for the MySql RMI server  
* database: a MySQL database backup script (ChatDB.sql)  

### Features:  
- Clients login to the system with a username and a password (sign in) 
- Clients can create a new account by giving a username and a password (sign up)
- Clients can send a broadcast chat message (chat room discussion)
- Clients can send a private message to connected clients  
- Server maintains a connected users list and registered users list
- Server stores the chat history and account informations of each client
- Online users list is updated for all clients when users join or leave the chat  
- Registred users list is updated on a new client subscription
- Users receive real-time notifications about new incoming messages

### Software requirements 
- MySql database
- Java JDK8 (with JavaFX or OpenJFX) and RMI
- Apache Ant(TM)

### Instructions
* Create the MySql database from the backup script ChatDB.sql in database directory
* Inside ServerSide directory:
	- build the server files using ant: 
	```sh
	sudo ant
	```
	- launch the chat server on the default LAN (localhost):  
	```sh
	sudo ant -emacs -Duser="databaseUser" -Dpass="databasePassword" run  	
	```
	- to launch the server on a precise network you need to provide [the ip address][1]:
	```sh
	sudo ant -emacs -Duser="databaseUser" -Dpass="databasePassword" -Dip="ip_address" run
	```
* Inside ClientSide directory:
	- build the client files using ant: 
	```sh
	sudo ant
	```
	- launch the chat client in console mode on the default LAN (localhost): 
	```sh
	sudo ant -emacs run
	```
	- to launch the client in console mode on a precise network you need to provide [the ip address][1]:  
	```sh
	sudo ant -emacs -Dip="ip address" run   	
	```
	- launch the chat client in GUI mode on the default LAN (localhost):
	```sh 
	sudo ant -q runGUI
	```
	- to launch the client in GUI mode on a precise network you need to provide [the ip address][1]:  
	```sh
	sudo ant -q -Dip="ip_address" runGUI
	``` 
[1]: The ip address has to correspond to the server machine's ip address on the common to server and all clients network
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


