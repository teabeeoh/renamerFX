/**
 * Created by Thomas Bolz on 04.10.14.
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Date;

public class Playground extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Button button = new Button("Append Text");
        TextArea textArea = new TextArea();
        button.setOnAction(event -> textArea.appendText("Some text " + new Date() + "\n"));
                root.setCenter(textArea);
        root.setBottom(button);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

    }
}
