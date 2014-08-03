package calendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Vegard on 13.07.14.
 */
public class Note {

	public static final DateTimeFormatter NOTE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MM yyyy");

	private String content, title;
	private LocalDate date;
	private int id;

	public Note(int id, String title, String content, LocalDate date) {
		this.id = id;
		setContent(content);
		setTitle(title);
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public boolean setContent(String content) {
		if (content.length() > 500) {
			return false;
		}

		this.content = content;
		return true;
	}

	public String getTitle() {
		return title;
	}

	public boolean setTitle(String title) {
		if (title.length() > 40) {
			return false;
		}

		this.title = title;
		return true;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDateFormatted() {
		return date.format(NOTE_DATE_FORMAT);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
