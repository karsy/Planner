package application;

import calendar.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../view/PlannerView.fxml"));
        primaryStage.setTitle("Planner");
	    
	    primaryStage.setOnCloseRequest((event) -> {
			if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
				Database.closeConnection();
				Controller.getRadio().cleanUp();
			}
		});

	    primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
	    Database.connect();
        launch(Main.class, args);
    }
}
