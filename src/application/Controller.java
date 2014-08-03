package application;

import calendar.Calendar;
import calendar.NoteComposer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import radio.Radio;

import java.io.File;
import java.io.IOException;

public class Controller {

	private static Radio radio;
	private static File radioFXML = new File("src/view/Radio.fxml");

	private static Calendar calendar;
	private static File calendarFXML = new File("src/view/Calendar.fxml");

	private static NoteComposer composer;
	private static File composerFXML = new File("src/view/Composer.fxml");

	@FXML
	private SplitPane splitPane;

	@FXML
	private SplitPane calendarParent;

	@FXML
	private AnchorPane composerParent;

	@FXML
	private void initialize() {
		radio = (Radio) loadFXML(radioFXML);
		calendar = (Calendar) loadFXML(calendarFXML);
		composer = (NoteComposer) loadFXML(composerFXML);

		splitPane.getItems().add(0, radio.getRootNode());
		calendarParent.getItems().add(calendar.getRootNode());
		composerParent.getChildren().add(composer.getRootNode());
	}

	// Returns the controller instance for this fxml file
	private static Object loadFXML(File fxmlFile) {
		Object instance = null;

		try {
			FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
			loader.load();
			instance = loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return instance;
	}

	public static Radio getRadio() {
		return radio;
	}

	public static Calendar getCalendar() {
		return calendar;
	}

	public static NoteComposer getComposer() {
		return composer;
	}
}
