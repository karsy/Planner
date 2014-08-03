package calendar;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vegard on 12.07.14.
 */

public class Database {

	private static final String INSERT_NOTE_STRING = "insert into planner.NOTES (ID, TITLE, NOTE, DAY) VALUES (?, ?, ?, ?)";
	private static final String UPDATE_NOTE_STRING = "UPDATE planner.NOTES SET TITLE = ?, NOTE = ? WHERE ID = ?";

	private static File dbFolder = new File("planner");
	private static Connection conn;

	public static void connect() {
		try {
			if (!dbFolder.exists()) {
				conn = DriverManager.getConnection("jdbc:derby:planner;create=true");
				createTable();
			} else {
				conn = DriverManager.getConnection("jdbc:derby:planner;");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveNote(Note note) {
		try {
			PreparedStatement insertNote = conn.prepareStatement(INSERT_NOTE_STRING);
			insertNote.setInt(1, note.getId());
			insertNote.setString(2, note.getTitle());
			insertNote.setString(3, note.getContent());
			insertNote.setString(4, note.getDateFormatted());
			insertNote.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteNote(int id) {
		String sql = "DELETE FROM planner.NOTES WHERE ID = " + String.valueOf(id);

		try {
			Statement delete = conn.createStatement();
			delete.execute(sql);
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateNote(Note note) {
		try {
			PreparedStatement updateNote = conn.prepareStatement(UPDATE_NOTE_STRING);
			updateNote.setString(1, note.getTitle());
			updateNote.setString(2, note.getContent());
			updateNote.setInt(3, note.getId());
			updateNote.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<Note> getNotes(LocalDate date) {
		List<Note> result = new ArrayList<>();
		String formattedDate = date.format(Note.NOTE_DATE_FORMAT);
		String sql = "SELECT * FROM planner.NOTES WHERE DAY = '" + formattedDate + "'";

		try {
			Statement statement = conn.createStatement();
			if (statement.execute(sql)) {
				ResultSet results = statement.getResultSet();
				while (results.next()) {
					int id = results.getInt("ID");
					String title = results.getString("TITLE");
					String content = results.getString("NOTE");
					Note note = new Note(id, title, content, date);
					result.add(note);
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static List<Note> getAllNotes() {
		List<Note> result = new ArrayList<>();
		String sql = "SELECT * FROM planner.NOTES";

		try {
			Statement statement = conn.createStatement();
			if (statement.execute(sql)) {
				ResultSet results = statement.getResultSet();
				while (results.next()) {
					int id = results.getInt("ID");
					String title = results.getString("TITLE");
					String content = results.getString("NOTE");
					String date = results.getString("DAY");
					Note note = new Note(id, title, content, LocalDate.parse(date, Note.NOTE_DATE_FORMAT));
					result.add(note);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static void createTable() {
		String tableSQL = "create table planner.NOTES" +
						  "(ID int NOT NULL PRIMARY KEY," +
						  "TITLE varchar(40) NOT NULL," +
						  "NOTE varchar(500) NOT NULL," +
						  "DAY varchar(10) NOT NULL)";
		try {
			Statement statement = conn.createStatement();
			statement.execute(tableSQL);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static int getHighestID() {
		String sql = "SELECT * FROM planner.NOTES ORDER BY ID DESC FETCH FIRST ROW ONLY";
		int highest = 0;

		try {
			Statement statement = conn.createStatement();
			if (statement.execute(sql)) {
				ResultSet set = statement.getResultSet();
				set.next();
				highest = set.getInt("ID");
			}
			statement.close();
		} catch(SQLException e) {
			// Nothing in the database
			return 0;
		}

		return highest;
	}

	public static void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
