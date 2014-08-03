package calendar;

import application.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by Vegard on 14.07.14.
 */
public class NoteComposer {

	private static int noteID = 0;

	@FXML
	private ScrollPane composerRoot;

	@FXML
	private VBox noteContainer;

	@FXML
	private Label dayLabel;

	@FXML
	private void initialize() {
		if (noteID == 0) {
			noteID = Database.getHighestID();
		}

		dayLabel.setUserData(LocalDate.now());

		showNotesFor(LocalDate.now());
	}

	@FXML
	private void createEmptyNote() {
		createNewNote("", "", ++noteID);
	}

	/*
	<Pane prefHeight="240.0" prefWidth="322.0">
        <children>
            <VBox spacing="5.0">
                <children>
                    <TextField prefHeight="26.0" prefWidth="345.0" promptText="Tittel" />
                    <TextArea layoutY="26.0" prefHeight="186.0" prefWidth="345.0" promptText="Notat" wrapText="true" />
                    <HBox spacing="5.0">
                        <children>
                            <Button mnemonicParsing="false" text="Lagre" />
                            <Button mnemonicParsing="false" text="Slett" />
                        </children>
                    </HBox>
                </children>
            </VBox>
        </children>
    </Pane>
	*/

	private void createNewNote(String title, String content, int id) {

		Pane newNotePane = new Pane();
		newNotePane.setPrefSize(322.0, 251.0);
		newNotePane.setUserData(id);

		VBox vbox = new VBox(5);

		TextField titleField = new TextField();
		titleField.setPrefSize(322.0, 26.0);
		if (title.equals("")) {
			titleField.setPromptText("Tittel");
		} else {
			titleField.setText(title);
		}

		TextArea noteField = new TextArea();
		noteField.setPrefSize(322.0, 186.0);
		if (content.equals("")) {
			noteField.setPromptText("Notat");
		} else {
			noteField.setText(content);
		}
		noteField.setWrapText(true);

		HBox buttonContainer = new HBox(5);

		Button saveButton = new Button("Lagre");
		saveButton.setMnemonicParsing(false);
		saveButton.setOnAction(saveNote);
		if (id == noteID) {
			// Create a new note in the database
			saveButton.setUserData(false);
		} else {
			// Update the note in the database
			saveButton.setUserData(true);
		}

		Button deleteButton = new Button("Slett");
		deleteButton.setMnemonicParsing(false);
		deleteButton.setOnAction(deleteNote);

		buttonContainer.getChildren().addAll(saveButton, deleteButton);
		vbox.getChildren().addAll(titleField, noteField, buttonContainer);
		newNotePane.getChildren().add(vbox);
		noteContainer.getChildren().add(newNotePane);
	}

	private javafx.event.EventHandler<ActionEvent> saveNote = event -> {
		Button source = (Button) event.getSource();
		Pane parent = (Pane) source.getParent().getParent().getParent();

		int id = (int) parent.getUserData();
		String title = ((TextField) source.getParent().getParent().getChildrenUnmodifiable().get(0)).getText();
		String content = ((TextArea) source.getParent().getParent().getChildrenUnmodifiable().get(1)).getText();
		Note note = new Note(id, title, content, (LocalDate) dayLabel.getUserData());
		Boolean shouldUpdate = (Boolean) source.getUserData();

		if (shouldUpdate) {
			Database.updateNote(note);
		} else {
			Database.saveNote(note);
			// Update instead of saving next time
			source.setUserData(true);
			Controller.getCalendar().updateNoteCount(1, note);
		}
	};

	private javafx.event.EventHandler<ActionEvent> deleteNote = event -> {
		Button source = (Button) event.getSource();
		Pane parent = (Pane) source.getParent().getParent().getParent();
		int id = (int) parent.getUserData();
		Database.deleteNote(id);
		noteContainer.getChildren().remove(parent);
		if (noteContainer.getChildren().size() <= 1) {
			createEmptyNote();
		}
		Controller.getCalendar().decreaseNoteCount(id);
	};

	public void showNotesFor(LocalDate date) {
		LocalDate today = LocalDate.now();
		if (today.getYear() == date.getYear() && today.getDayOfYear() == date.getDayOfYear()) {
			dayLabel.setText("I dag");
		} else {
			dayLabel.setText(date.format(DateTimeFormatter.ofPattern("dd.MM.yy")));
		}

		List<Note> notes = Database.getNotes(date);
		noteContainer.getChildren().remove(1, noteContainer.getChildren().size());
		if (notes.size() == 0) {
			createEmptyNote();
		} else {
			for (Note note : notes) {
				createNewNote(note.getTitle(), note.getContent(), note.getId());
			}
		}
	}

	public ScrollPane getRootNode() {
		return composerRoot;
	}
}
