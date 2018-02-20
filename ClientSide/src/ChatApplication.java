import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Background;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class ChatApplication extends Application {

    static String hostip = "localhost";

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(280);
        stage.setMaxWidth(280);
        stage.setMinHeight(400);
        stage.setMaxHeight(400);
        stage.setTitle("ChatServer");
        stage.getIcons().add(new Image("chatLogo.png"));
        BackgroundImage img = new BackgroundImage(new Image("front.png",280,280,false,true),
            BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);
        ChatParts cp = new ChatParts(stage, hostip);
        cp.setBackground(new Background(img));
        stage.setScene(new Scene(cp));
        stage.show();

    }

    public static void main(String[] args) {
        if (args.length == 1) {
            hostip = args[0];
        }
        launch(args);
    }

}