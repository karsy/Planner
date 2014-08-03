package calendar;

import application.Controller;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vegard on 11.07.14.
 */
public class Calendar {

	private static final int ROWS = 6, COLUMNS = 7;
	private static final String[] monthNames = {"Januar", "Februar", "Mars", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember"};

	private static LocalDate displayDate;
	private static LocalDate chosenDate;
	private static List<Note> notes = new ArrayList<>();

	@FXML
	private AnchorPane calendarRoot;

	@FXML
	private Label yearLabel;

	@FXML
	private Label monthLabel;

	@FXML
	private GridPane calendar;
	private StackPane[] dayContainers = new StackPane[42];
	private Label[] days = new Label[42];
	private Label[] noteLabels = new Label[42];

	@FXML
	private GridPane weekPane;
	private Label[] weeks = new Label[6];

	@FXML
	private void initialize() {
		notes = Database.getAllNotes();

		// Get current date
		displayDate = LocalDate.now();
		chosenDate = LocalDate.now();

		displayMonth(displayDate);
	}

	private void displayMonth(LocalDate date) {
		Month month = date.getMonth();
		int year = date.getYear();

		calendar.getChildren().remove(0, calendar.getChildren().size());
		weekPane.getChildren().remove(0, weekPane.getChildren().size());

		// Set the default text of the labels for choosing month and year
		monthLabel.setText(monthNames[month.getValue() - 1]);
		yearLabel.setText(String.valueOf(year));

		// Get the first day of the month and the day offset since the first day is rarely a monday
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int dayOffset = LocalDate.of(year, month, 1).getDayOfWeek().getValue() - 1;

		// The date to start counting from
		LocalDate startDate;
		try {
			startDate = LocalDate.ofYearDay(year, firstDayOfMonth.getDayOfYear() - dayOffset);
		} catch (DateTimeException e) {
			// December last year
			String[] parts = e.getMessage().split(" ");
			int offset = Integer.parseInt(parts[parts.length - 1]);

			Year lastYear = Year.of(year - 1);
			int daysInYear = lastYear.isLeap() ? 366 : 365;
			startDate = LocalDate.ofYearDay(year - 1, daysInYear + offset);
		}

		for (int i = 0; i < ROWS; i++) {
			weeks[i] = new Label();

			for (int j = 0; j < COLUMNS; j++) {
				int index = j + i * 7;
				dayContainers[index] = new StackPane();
				days[index] = new Label();
				noteLabels[index] = new Label();

				LocalDate currentDay = getCurrentDay(startDate, index, Math.abs(dayOffset));

				if (currentDay.getDayOfWeek().getValue() == 1) {
					weeks[i].setText(String.valueOf(currentDay.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));
				}

				days[index].setText(String.valueOf(currentDay.getDayOfMonth()));
				days[index].setOnMousePressed(handleLabelClick);

				// Faster than streams on my computer
				int amountOfNotes = 0;
				for (Note note : notes) {
					if (note.getDate().getDayOfYear() == currentDay.getDayOfYear() && note.getDate().getYear() == currentDay.getYear()) {
						amountOfNotes++;
					}
				}

				if (amountOfNotes > 0) {
					noteLabels[index].setText(amountOfNotes == 1 ? "1 notat" : String.valueOf(amountOfNotes) + " notater");
				}
				noteLabels[index].setUserData(amountOfNotes);
				noteLabels[index].setOnMousePressed(handleLabelClick);

				StackPane.setAlignment(days[index], Pos.TOP_CENTER);
				StackPane.setAlignment(noteLabels[index], Pos.BOTTOM_CENTER);
				dayContainers[index].getChildren().addAll(days[index], noteLabels[index]);

				dayContainers[index].setUserData(currentDay);
				dayContainers[index].setOnMousePressed(handleStackPaneClick);

				// Container styling
				if (currentDay.getMonth() == month && currentDay.getDayOfWeek().getValue() == 7) {
					dayContainers[index].getStyleClass().add("sunday");
				}

				// Blue background for today, and remove the red one if today is a sunday
				if (currentDay.getDayOfYear() == LocalDate.now().getDayOfYear() && currentDay.getYear() == LocalDate.now().getYear()) {
					dayContainers[index].setId("today");
					dayContainers[index].getStyleClass().remove("sunday");
				}

				// Cyan background for the chosen date
				if (chosenDate.getDayOfYear() == currentDay.getDayOfYear() && chosenDate.getYear() == currentDay.getYear()) {
					dayContainers[index].getStyleClass().add("chosenDate");
				}

				// Grey background for dates that are not in the current month
				if (currentDay.getMonth() != month) {
					dayContainers[index].getStyleClass().add("notCurrentMonth");
				}

				dayContainers[index].getStyleClass().add("dayContainer");
				dayContainers[index].setPadding(new Insets(20, 0, 20, 0));

				// Container constraints
				GridPane.setConstraints(dayContainers[index], j, i);
				GridPane.setHalignment(dayContainers[index], HPos.CENTER);
				GridPane.setValignment(dayContainers[index], VPos.CENTER);
			}

			GridPane.setConstraints(weeks[i], 0, i);
			GridPane.setHalignment(weeks[i], HPos.CENTER);
			GridPane.setValignment(weeks[i], VPos.CENTER);
		}

		calendar.getChildren().addAll(dayContainers);
		weekPane.getChildren().addAll(weeks);
	}

	private LocalDate getCurrentDay(LocalDate startDate, int index, int dayOffset) {
		if (startDate.getMonth().getValue() == 12 && displayDate.getMonth().getValue() == 1) {
			// December last year
			if (index < dayOffset) {
				return LocalDate.ofYearDay(startDate.getYear(), startDate.getDayOfYear() + index);
			} else {
				// January or february
				return LocalDate.ofYearDay(startDate.getYear() + 1, index - dayOffset + 1);
			}
		} else if (Year.isLeap(startDate.getYear()) && startDate.getDayOfYear() + index > 366) {
			// January next year, current year is a leap year
			return LocalDate.ofYearDay(startDate.getYear() + 1, startDate.getDayOfYear() + index - 366);
		} else if (!Year.isLeap(startDate.getYear()) && startDate.getDayOfYear() + index > 365) {
			// January next year, current year is not a leap year
			return LocalDate.ofYearDay(startDate.getYear() + 1, startDate.getDayOfYear() + index - 365);
		}
		// Not a special date
		return LocalDate.ofYearDay(startDate.getYear(), startDate.getDayOfYear() + index);
	}

	// Event handling for the stackpane and for the labels inside the stackpanes

	private javafx.event.EventHandler<MouseEvent> handleStackPaneClick = event -> {
		StackPane source = (StackPane) event.getSource();
		changeDate(source);
	};

	private javafx.event.EventHandler<MouseEvent> handleLabelClick = event -> {
		Label source = (Label) event.getSource();
		StackPane parent = (StackPane) source.getParent();

		changeDate(parent);
	};

	// Change the displayed date to the one the user chose
	private void changeDate(StackPane pane) {
		chosenDate = (LocalDate) pane.getUserData();
		if (chosenDate.getMonthValue() != displayDate.getMonthValue()) {
			displayDate = chosenDate;
			displayMonth(displayDate);
		} else {
			for (StackPane stackPane : dayContainers) {
				if (stackPane.getStyleClass().contains("chosenDate")) {
					stackPane.getStyleClass().remove("chosenDate");
				}
			}

			pane.getStyleClass().add("chosenDate");
		}

		Controller.getComposer().showNotesFor(chosenDate);
	}

	// Updates the label with the note count for the current day
	public void updateNoteCount(int diff, Note note) {
		Arrays.stream(dayContainers).filter(container -> container.getStyleClass().contains("chosenDate")).forEach(container -> {
			Label noteLabel = (Label) container.getChildren().get(1);
			int amountOfNotes = (int) noteLabel.getUserData();
			amountOfNotes = amountOfNotes + diff < 0 ? 0 : amountOfNotes + diff;
			if (amountOfNotes > 0) {
				noteLabel.setText(amountOfNotes == 1 ? "1 notat" : String.valueOf(amountOfNotes) + " notater");
				noteLabel.setUserData(amountOfNotes);
			} else {
				noteLabel.setText("");
				noteLabel.setUserData(0);
			}
		});

		if (note != null) {
			notes.add(note);
		}
	}

	// Removes the deleted note from the note array and updates the note count
	public void decreaseNoteCount(int id) {
		Note toRemove = notes.stream().filter(note -> note.getId() == id).findFirst().get();
		notes.remove(toRemove);
		updateNoteCount(-1, null);
	}

	@FXML
	private void nextMonth() {
		displayDate = displayDate.plusMonths(1);
		displayMonth(displayDate);
	}

	@FXML
	private void previousMonth() {
		displayDate = displayDate.minusMonths(1);
		displayMonth(displayDate);
	}

	@FXML
	private void nextYear() {
		displayDate = displayDate.plusYears(1);
		displayMonth(displayDate);
	}

	@FXML
	private void previousYear() {
		displayDate = displayDate.minusYears(1);
		displayMonth(displayDate);
	}

	@FXML
	private void showToday() {
		displayDate = LocalDate.now();
		chosenDate = LocalDate.now();
		displayMonth(displayDate);
		Controller.getComposer().showNotesFor(LocalDate.now());
	}

	public AnchorPane getRootNode() {
		return calendarRoot;
	}
}
