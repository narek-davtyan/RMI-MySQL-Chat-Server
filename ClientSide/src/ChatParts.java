import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ChatParts extends BorderPane {

    // Connection elements
    private ChatInterface server;
    private UserInterface client;

    // Newly received messages management
    private BooleanProperty messageReceived = new SimpleBooleanProperty(false);
    private ChangeListener newMessageListener;
    private String newMessage;

    private Stage stage;

    // Start screen
    private VBox startScreenButtons;
    private ToggleButton signIn, signUp;
    private TextField userName, userPassword;
    private boolean isLogin, isAuthentified;
    private int userNameAttempts, userPasswordAttempts;
    private Alert popupError = new Alert(AlertType.ERROR);

    // Common top part
    private BorderPane topPart;
    private Label topText;
    private ToggleButton topButton;

    // Chat screen:
    private List<String> registeredUsers;
    private ListView<TextFlow> chatList;
    private ObservableList<TextFlow> chatItems;
    private ObservableList<String> chatHistList;

    // Contacts screen:
    private ListView<String> contactsList;

    // Conversation screen:
    private ListView<String> conversationList;
    private ScrollBar conversationScrollBar;
    private String conversationUser;
    private TextField messageEntry;
    private Button messageButton;
    private BorderPane messagePane;

    private final ChangeListener<? super Number> scrollListener = (observable, oldValue, newValue) -> {
        double position = newValue.doubleValue();
        if (position == conversationScrollBar.getMin()) {
            int size = conversationList.getItems().size();
            List<String> list;
            try {
                if (conversationUser.equalsIgnoreCase("chat room")) {
                    list = server.history("broad", "broad", 15);
                } else {
                    list = server.history(client.getName(), conversationUser, 15);
                }
                Collections.reverse(list);
                List<String> parsedList = new ArrayList<>();
                if (list != null && !list.isEmpty()) {
                    list.forEach((s) -> {
                        try {
                            String[] sa = s.split(":");
                            if (sa[0].startsWith("broad ")) {
                                sa[0] = sa[0].substring(6);
                            }
                            if (sa[0].equals(client.getName())) {
                                sa[0] = "you: ";
                            } else {
                                sa[0] = sa[0] + ": ";
                            }
                            parsedList.add(sa[0].concat(sa[1]));
                        } catch (RemoteException e) {
                        }
                    });
                    conversationList.setItems(FXCollections.observableList(parsedList));
                }
            } catch (RemoteException e) {
            }
        }
    };

    /**
     * Creates new instance of ChatParts
     *
     * @param stage : stage of chat application
     * @param hostip : host name IP
     */
    ChatParts(Stage stage, String hostip) {
        try {
            // Connection to the server
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            server = (ChatInterface) Naming.lookup("rmi://" + hostip + ":1099/IDS");

            this.stage = stage;

            // Redefining closing behaviour
            this.stage.setOnCloseRequest(event -> {
                if (isAuthentified) {
                    try {
                        server.remClient(client.getName());
                        UnicastRemoteObject.unexportObject(client, true);
                    } catch (NoSuchObjectException e) {
                        popupError.setTitle("Object error");
                        popupError.setContentText("I think that I have "
                                + "some bad news for you...\n"
                                + "Unexporting the object failed!"
                                + "NoSuchObjectException :" + e);
                        popupError.showAndWait();
                    } catch (RemoteException e) {
                        popupError.setTitle("Remote error");
                        popupError.setContentText("I think that I have "
                                + "some bad news for you...\n"
                                + "Removing the client from the remote list failed!"
                                + "RemoteException :" + e);
                        popupError.showAndWait();
                    }
                }
                Platform.exit();
            });

            buildStartScreen();
            triggerStartScreen();
            setCenter(startScreenButtons);

        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            popupError.setTitle("FATAL ERROR");
            popupError.setContentText("I think something went terribly wrong...\n"
                    + "NotBoundException | RemoteException | MalformedURLException\n"
                    + "Connection to the server failed: " + e);
            popupError.showAndWait();
            System.exit(1);
        }
    }

    /**
     * Builds welcome screen buttons and other UI elements
     */
    private void buildStartScreen() {
        // Building sign in/up buttons
        signIn = new ToggleButton();
        signUp = new ToggleButton();
        signIn.setText("SIGN IN");
        signUp.setText("SIGN UP");

        // Building text fields for login/password
        userName = new TextField();
        userPassword = new TextField();
        userName.setPromptText("Type your username here");
        userPassword.setPromptText("Type your password here");

        isAuthentified = false;
        userNameAttempts = 1;
        userPasswordAttempts = 1;

        startScreenButtons = new VBox();
        startScreenButtons.getChildren().addAll(signIn, signUp);
        startScreenButtons.setPadding(new Insets(210, 10, 10, 100));
        startScreenButtons.setSpacing(10);

    }

    /**
     * Defines triggers for welcome screen buttons and text fields
     */
    private void triggerStartScreen() {
        // Pushing sign in button to enter credentials
        signIn.setOnAction((ActionEvent e) -> {
            isLogin = true;
            rebuildStartScreen();
        });
        // Pushing sign up button to enter credentials
        signUp.setOnAction((ActionEvent e) -> {
            isLogin = false;
            rebuildStartScreen();
        });
        // Pressing ENTER to pass to password field
        userName.setOnKeyPressed((KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                userPassword.requestFocus();
            }
        });
        // Pressing ENTER to pass to authentification phase
        userPassword.setOnKeyPressed((KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER
                    && (!userName.getText().isEmpty())
                    && (!userPassword.getText().isEmpty())) {
                authenticationStartScreen();
            }
        });
    }

    /**
     * Rebuilds stage to show username and password fields
     */
    private void rebuildStartScreen() {
        startScreenButtons.getChildren().clear();
        startScreenButtons.getChildren().addAll(userName, userPassword);
        startScreenButtons.setPadding(new Insets(250, 20, 20, 20));
    }

    /**
     * Performs authentication procedure
     */
    private void authenticationStartScreen() {
        try {
            // Signin procedure
            if (isLogin) {
                // Forced signup procedure
                if (userNameAttempts > 3) {
                    popupError.setTitle("Username error");
                    popupError.setHeaderText(server.getServerName());
                    popupError.setContentText("I think that I have "
                            + "some bad news for you...\n"
                            + "3 incorrect username attempts!\n"
                            + "This user with this name doesn't exist!\n"
                            + "YOU don't exist!\n"
                            + "The account with this name and password will be created...");
                    popupError.showAndWait();
                    client = new User(userName.getText(), userPassword.getText(), this);
                    server.addClient(client);
                    isAuthentified = true;
                    // Not registered error
                } else if (!server.isClient(userName.getText())
                        && !server.isClientCon(userName.getText())) {
                    popupError.setTitle("Login error");
                    popupError.setHeaderText(server.getServerName());
                    popupError.setContentText("I think that I have "
                            + "some bad news for you...\n"
                            + "It seems like that name is not registered!\n"
                            + "I guess the misspelling accident has taken place...");
                    popupError.showAndWait();
                    userNameAttempts++;
                    //User is registered
                } else {
                    if (server.isClient(userName.getText())
                            && !server.isClientCon(userName.getText())) {
                        // Checking username and password with the server
                        if (userPasswordAttempts > 3) {
                            popupError.setTitle("Password error");
                            popupError.setHeaderText(server.getServerName());
                            popupError.setContentText("I think that I have "
                                    + "some bad news for you...\n"
                                    + "3 incorrect password attempts!\n"
                                    + "You are not who you say you are!\n"
                                    + "You will have to leave, Mr Imposter...");
                            popupError.showAndWait();
                            System.exit(1);

                            //continue;
                        } // Username and password are correct and
                        // no other connection with the cliens has been established
                        else if (server.isClient(userName.getText(), userPassword.getText())) {
                            client = new User(userName.getText(), userPassword.getText(), this);
                            server.addClient(client);
                            isAuthentified = true;
                            // Password is incorrect three times in a row
                        } else {
                            popupError.setTitle("Password error");
                            popupError.setHeaderText(server.getServerName());
                            popupError.setContentText("I think that I have "
                                    + "some bad news for you...\n"
                                    + "It seems like that name has a different password!\n"
                                    + "I guess the misspelling accident has taken place...");
                            popupError.showAndWait();
                            userPassword.clear();
                            userPasswordAttempts++;
                        }
                        // User is already connected
                    } else if (server.isClientCon(userName.getText())) {
                        popupError.setTitle("Account error");
                        popupError.setHeaderText(server.getServerName());
                        popupError.setContentText("I think that I have "
                                + "some bad news for you...\n"
                                + "This user is already connected!\n"
                                + "You are not who you say you are!\n"
                                + "You will have to leave, Mr Imposter...");
                        popupError.showAndWait();
                        System.exit(1);
                    }
                }
                // Signup procedure
            } else {
                if (server.isClient(userName.getText())) {
                    Random randomNumber = new Random();
                    popupError.setTitle("Signup error");
                    popupError.setHeaderText(server.getServerName());
                    popupError.setContentText("I think that I have "
                            + "some bad news for you...\n"
                            + "It seems like that name is already used!\n"
                            + "You might want to try "
                            + userName.getText()
                            + randomNumber.nextInt(20));
                    popupError.showAndWait();
                } else {
                    client = new User(userName.getText(), userPassword.getText(), this);
                    server.addClient(client);
                    isAuthentified = true;
                }
            }
            // Successful authentification initiales session
            if (isAuthentified) {
                registeredUsers = server.listReg();
                buildSessionInit();
            }
        } catch (RemoteException e) {
            popupError.setTitle("FATAL ERROR");
            popupError.setContentText("I think something went terribly wrong...\n"
                    + "Connection to the server failed!"
                    + "RemoteException:" + e);
            popupError.showAndWait();
            System.exit(1);
        }
    }

    /**
     * Initializes session
     */
    private void buildSessionInit() {
        // Build top part of the screen
        topPart = new BorderPane();
        topText = new Label();
        topText.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        topButton = new ToggleButton();
        try {
            stage.setTitle(client.getName());
        } catch (RemoteException e) {
            popupError.setTitle("FATAL ERROR");
            popupError.setContentText("I think something went terribly wrong...\n"
                    + "Server information could not be retreived!"
                    + "RemoteException:" + e);
            popupError.showAndWait();
            System.exit(1);
        }

        triggerSessionInit();
        buildChatScreen();
        triggerChatScreen();
    }

    /**
     * Defines triggers to manipulate Chats/Contacts button
     */
    private void triggerSessionInit() {
        // Defining triggers for Chats/Contacts button
        topButton.setOnAction((ActionEvent e) -> {
            if (topButton.isSelected() && topText.getText().equalsIgnoreCase("Chats")) {
                topButton.setSelected(false);
                buildContactsScreen();
                triggerContactsScreen();
            } else if (topButton.isSelected()) {
                topButton.setSelected(false);
                buildChatScreen();
                triggerChatScreen();
                setBottom(null);
            }
        });
    }

    /**
     * Builds chat screen using server database
     */
    private void buildChatScreen() {
        try {
            // Build top part
            topText.setText("Chats");
            topPart.setLeft(topText);
            topButton.setText("Contacts");
            topPart.setRight(topButton);

            setTop(topPart);

            // Build center part
            chatList = new ListView();
            chatItems = FXCollections.observableArrayList();
            registeredUsers = server.listReg();

            // Getting all chats from the server
            for (String user : registeredUsers) {
                chatHistList = FXCollections.observableArrayList(server.history(client.getName(), user, 1));
                Text text1, text2;
                TextFlow textFlow;
                if (!chatHistList.isEmpty()) {
                    String s = chatHistList.get(0);
                    chatHistList.set(0, s.substring(0, s.indexOf(":") + 1) + " " + s.substring(s.indexOf(":") + 1));
                    text1 = new Text("[" + user + "] ");
                    text1.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
                    text1.setFill(Color.CORNFLOWERBLUE);
                    text2 = new Text(chatHistList.get(0));
                    textFlow = new TextFlow(text1, text2);

                    chatItems.add(textFlow);
                }
            }
            // Adding common chat
            chatHistList = FXCollections.observableArrayList(server.history("broad", "broad", 1));
            if (!chatHistList.isEmpty()) {
                String s = chatHistList.get(0);
                chatHistList.set(0, s.substring(6, s.indexOf(":") + 1) + " " + s.substring(s.indexOf(":") + 1));
            }
            Text text1, text2;
            TextFlow textFlow;
            text1 = new Text("[" + "chat room" + "] ");
            text1.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
            text1.setFill(Color.ROYALBLUE);
            text2 = new Text(FXCollections.observableList(chatHistList).get(0));
            textFlow = new TextFlow(text1, text2);

            chatItems.add(textFlow);

            chatList.setItems(chatItems);

            setCenter(chatList);
        } catch (RemoteException e) {
            popupError.setTitle("Chats error");
            popupError.setContentText("I think that I have "
                    + "some bad news for you...\n"
                    + "Chats were not loaded!"
                    + "RemoteException :" + e);
            popupError.showAndWait();
        }
    }

    /**
     * Defines triggers for chat list and receiving new messages
     */
    private void triggerChatScreen() {
        // Clicking on a chat redirects to corresponding conversation
        chatList.setOnMouseClicked((MouseEvent event) -> {
            conversationUser = ChatParts.getString((TextFlow) chatList.getSelectionModel().getSelectedItem());
            if (conversationUser != null) {
                conversationUser = conversationUser.split("]")[0];
                conversationUser = conversationUser.substring(1);
                buildConversationScreen();
                triggerConversationScreen();
            }
        });
        // Arrival of new message triggers notification
        newMessageListener = (ChangeListener) (ObservableValue o, Object ov, Object nv) -> {
            if (topText.getText().equalsIgnoreCase("Chats")) {
//                buildChatScreen();
                rebuildChatScreen();
                triggerChatScreen();
            } else if (topText.getText().equalsIgnoreCase(conversationUser)) {
                buildConversationScreen();
                triggerConversationScreen();
            }
            messageReceived.set(false);
        };
        messageReceived.addListener(newMessageListener);

    }

    /**
     * Rebuilds chat screen so that new messages would appear
     */
    private void rebuildChatScreen() {

        boolean isBroadConversation = false;
        String newMessageSender = newMessage.substring(1).split("]")[0];
        if (newMessageSender.startsWith("broad ")) {
            isBroadConversation = true;
            newMessageSender = newMessageSender.substring(6);
        }

        ObservableList<TextFlow> chatsOld = chatList.getItems();
        boolean isFound = false;

        for (TextFlow chatLineText : chatsOld) {

            String chatLineString = ChatParts.getString(chatLineText);
            String name = chatLineString.substring(1).split("]")[0];

            if (isBroadConversation && name.equalsIgnoreCase("chat room")) {
                Text text1, text2;
                TextFlow textFlow;
                chatLineString = newMessage.replace("[" + "broad " + newMessageSender + "]", newMessageSender + ":");
                text2 = new Text(chatLineString);
                text1 = new Text("[" + name + "] ");
                text1.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                text1.setFill(Color.CORNFLOWERBLUE);
                text2.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                text2.setFill(Color.FIREBRICK);
                textFlow = new TextFlow(text1, text2);
                chatsOld.set(chatsOld.indexOf(chatLineText), textFlow);
                isFound = true;
                break;
            } else if (!isBroadConversation && newMessageSender.equalsIgnoreCase(name)) {
                Text text1, text2;
                TextFlow textFlow;
                chatLineString = newMessage.replace("[" + name + "]", newMessageSender + ":");
                text2 = new Text(chatLineString);
                text1 = new Text("[" + name + "] ");
                text1.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                text1.setFill(Color.CORNFLOWERBLUE);
                text2.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                text2.setFill(Color.FIREBRICK);
                textFlow = new TextFlow(text1, text2);
                chatsOld.set(chatsOld.indexOf(chatLineText), textFlow);
                isFound = true;
                break;
            }
        }
        // Conversation doesn't exist => create it
        if (isFound == false) {
            try {
                chatHistList = FXCollections.observableArrayList(server.history(client.getName(), newMessageSender, 1));
                if (!chatHistList.isEmpty()) {
                    Text text1, text2;
                    TextFlow textFlow;
                    String s = chatHistList.get(0);
                    chatHistList.set(0, s.substring(0, s.indexOf(":") + 1) + " " + s.substring(s.indexOf(":") + 1));
                    text1 = new Text("[" + newMessageSender + "] ");
                    text1.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    text1.setFill(Color.CORNFLOWERBLUE);
                    text2 = new Text(chatHistList.get(0));
                    text2.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    textFlow = new TextFlow(text1, text2);
                    chatsOld.add(textFlow);
                }
            } catch (RemoteException e) {
                popupError.setTitle("Chats error");
                popupError.setContentText("I think that I have "
                        + "some bad news for you...\n"
                        + "Chats were not loaded!"
                        + "RemoteException :" + e);
                popupError.showAndWait();
            }
        }
        chatList.setItems(chatsOld);

        setCenter(chatList);
    }

    /**
     * Builds contacts screen using server database
     */
    private void buildContactsScreen() {
        try {
            // Build top part
            topText.setText("Contacts");
            topPart.setLeft(topText);
            topButton.setText("Chats");
            topPart.setRight(topButton);

            setTop(topPart);

            // Build center part
            contactsList = new ListView();
            ObservableList<String> list = FXCollections.observableList(registeredUsers);
            // Chat with self is forbidden
            list.remove(client.getName());
            contactsList.setItems(list);

            setCenter(contactsList);
        } catch (RemoteException e) {
            popupError.setTitle("Phonebook error");
            popupError.setContentText("I think that I have "
                    + "some bad news for you...\n"
                    + "Contacs were not loaded!"
                    + "RemoteException :" + e);
            popupError.showAndWait();
        }
    }

    /**
     * Defines trigger to manipulate contacts list
     */
    private void triggerContactsScreen() {
        // Clicking on a username redirects to corresponding conversation
        // If it doesn't exist, new one is created
        contactsList.setOnMouseClicked((MouseEvent event) -> {
            conversationUser = contactsList.getSelectionModel().getSelectedItem();
            buildConversationScreen();
            triggerConversationScreen();
        });
    }

    /**
     * Builds conversation screen
     */
    private void buildConversationScreen() {
        try {
            // Build top part
            topText.setText(conversationUser);
            topPart.setLeft(topText);
            topButton.setText("Chats");
            topPart.setRight(topButton);

            setTop(topPart);

            // Build buttom part
            messageEntry = new TextField();
            messageEntry.setPromptText("New Message");

            messageButton = new Button();
            messageButton.setText("Send");

            messagePane = new BorderPane();
            messagePane.setCenter(messageEntry);
            messagePane.setRight(messageButton);

            setBottom(messagePane);

            // Build center part
            conversationList = new ListView();
            List<String> list;
            if (conversationUser.equalsIgnoreCase("chat room")) {
                list = server.history("broad", "broad", 15);
            } else {
                list = server.history(client.getName(), conversationUser, 15);
            }
            Collections.reverse(list);
            List<String> parsedList = new ArrayList<>();
            if (list != null && !list.isEmpty()) {
                list.forEach((s) -> {
                    try {
                        String[] sa = s.split(":");
                        if (sa[0].startsWith("broad ")) {
                            sa[0] = sa[0].substring(6);
                        }
                        if (sa[0].equals(client.getName())) {
                            sa[0] = "you: ";
                        } else {
                            sa[0] = sa[0] + ": ";
                        }
                        parsedList.add(sa[0].concat(sa[1]));
                    } catch (RemoteException e) {
                    }
                });
                conversationList.setItems(FXCollections.observableList(parsedList));

                // Scroll automatically to the end of conversation list (last message)
                conversationList.scrollTo(conversationList.getItems().size());

            }

            setCenter(conversationList);

            conversationScrollBar = getListViewScrollBar(conversationList, Orientation.VERTICAL);
        } catch (RemoteException e) {
            popupError.setTitle("History error");
            popupError.setContentText("I think that I have "
                    + "some bad news for you...\n"
                    + "History was not loaded!"
                    + "RemoteException :" + e);
            popupError.showAndWait();
        }
    }

    /**
     * Defines triggers for sending new message
     */
    private void triggerConversationScreen() {
        // Pressing ENTER sends message
        messageEntry.setOnKeyPressed((KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                messageButton.fire();
            }
        });
        // Clicking on send button sends message
        messageButton.setOnAction((ActionEvent e) -> {
            sendMessage();
        });

        // Adds scrollbar listener
        conversationList.itemsProperty().addListener((ob, ov, nv) -> {
            if (conversationScrollBar != null) {
                conversationScrollBar.valueProperty().removeListener(scrollListener);
            }
            this.conversationScrollBar = getListViewScrollBar(conversationList, Orientation.VERTICAL);
            if (conversationScrollBar != null) {
                conversationScrollBar.valueProperty().addListener(scrollListener);
            }
        });
    }

    /**
     * Sends message to corresponding and relevant user(s)
     */
    private void sendMessage() {
        try {
            // Message and user are verified => message is sent
            if (!messageEntry.getText().isEmpty()
                    && (conversationUser.equalsIgnoreCase("chat room")
                    || server.isClientCon(conversationUser))) {
                ObservableList<String> items = conversationList.getItems();
                String messageToSend = "you: " + messageEntry.getText();
                items.add(messageToSend);
                conversationList.setItems(items);

                messageToSend = "[" + client.getName() + "] " + messageEntry.getText();
                if (conversationUser.equalsIgnoreCase("chat room")) {
                    server.broad(server.getIdClient(client.getName()), messageToSend);
                } else {
                    server.send(server.getIdClient(conversationUser), messageToSend);
                }
                // Remote user is offline => message is not sent
            } else if (!messageEntry.getText().isEmpty()
                    && !server.isClientCon(conversationUser)) {
                popupError.setTitle("Connection error");
                popupError.setHeaderText(server.getServerName());
                popupError.setContentText("I think that I have "
                        + "some bad news for you...\n"
                        + conversationUser
                        + " seems to be offline...\n"
                        + "Message was therefore not sent!");
                popupError.showAndWait();
            }
            messageEntry.clear();
        } catch (RemoteException e) {
            popupError.setTitle("Postal error");
            popupError.setContentText("I think that I have "
                    + "some bad news for you...\n"
                    + "Message was not sent!"
                    + "RemoteException :" + e);
            popupError.showAndWait();
        }
    }

    /**
     * Treats new messages arrival
     *
     * @param message : newly arrived message
     */
    public void newMessageReceived(String message) {
        // Receiving new message changes the state
        // so that corresponding UI elements would be updated
        Platform.runLater(() -> {
            this.newMessage = message;
            ChatParts.this.messageReceived.set(true);
        });
    }

    /**
     * Extracts String from given TextFlow object
     *
     * @param tf : TextFlow GUI object
     * @return corresponding String
     */
    private static String getString(TextFlow tf) {
        StringBuilder sb = new StringBuilder();
        tf
                .getChildren().stream()
                .filter(t -> Text.class
                .equals(t.getClass()))
                .forEach(t -> sb.append(((Text) t).getText()));
        return sb.toString();
    }

    /**
     * Gets ScrollBar of corresponding ListView
     *
     * @param listView
     * @param orientation : vertical or horizontal scrollbar orientation
     * @return
     */
    private ScrollBar getListViewScrollBar(ListView<String> listView, Orientation orientation) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar
                    && ((ScrollBar) node).getOrientation() == orientation) {
                return (ScrollBar) node;
            }
        }
        return null;
    }

}

