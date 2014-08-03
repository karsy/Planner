package radio;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Radio implements Runnable {

	private static Radio radio = new Radio();
	private static FileResetter resetter = new FileResetter();

	private URL audioStreamURL;
	private InputStream audioStream;
	private FileOutputStream fos;
	private File audioFile;
	private Thread radioThread, resetThread;
	private boolean streamClosed = false, readyToPlay = false;

	@FXML
	private WebView radioContainer;
	private WebEngine webEngine;
	private File index = new File("src/web/index.html");

	@FXML
	private MediaView mediaView;
	private MediaView radioView = new MediaView();
	private Media audio;
	private MediaPlayer radioPlayer;

	@FXML
	private AnchorPane radioRoot;

	public Radio() {
		audioFile = new File("audio.mp3");
		if (audioFile.exists()) {
			audioFile.delete();
		}
	}

	@FXML
	private void initialize() {
		webEngine = radioContainer.getEngine();
		webEngine.load(index.toURI().toString());

		JSObject jsObj = (JSObject) webEngine.executeScript("window");
		jsObj.setMember("radioController", radio);

		this.radioView = mediaView;
	}

	@Override
	public void run() {
		try {
			createAudioFile();
			audioStream = audioStreamURL.openStream();
			fos = new FileOutputStream(audioFile);
			int next;

			for (int i = 0; i < 1000; i++) {
				next = audioStream.read();
				fos.write(next);
				fos.flush();
			}

			readyToPlay = true;

			while (!streamClosed && (next = audioStream.read()) != -1) {
				fos.write(next);
				fos.flush();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void playChannel(String url) {
		try {

			pauseRadio();

			audioStreamURL = new URL(url);
			streamClosed = false;

			radioThread = new Thread(radio, "radioThread");
			radioThread.start();

			while (!readyToPlay) {
				Thread.sleep(50);
			}

			Thread.sleep(100);

			playAudioFromStart();

			resetThread = new Thread(resetter, "resetThread");
			resetThread.start();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void playAudioFromStart() {
		audio = new Media(audioFile.toURI().toString());
		radioPlayer = new MediaPlayer(audio);
		radioView.setMediaPlayer(radioPlayer);
		radioPlayer.play();
	}

	public void pauseRadio() {
		if (radioPlayer != null) {
			radioPlayer.pause();
		}

		if (radioThread != null) {
			try {
				readyToPlay = false;
				streamClosed = true;
				radioThread.join(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setVolume(double volume) {
		if (radioPlayer != null) {
			radioPlayer.setVolume(volume);
		}
	}

	private void createAudioFile() {
		if (audioFile.exists()) {
			audioFile.delete();
		}

		try {
			audioFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cleanUp() {
		try {
			streamClosed = true;
			if (fos != null && audioStream != null && radioThread != null && resetThread != null) {
				resetThread.interrupt();
				resetThread.join();
				radioThread.join(0);
				fos.flush();
				fos.close();

				audioStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public File getAudioFile() {
		return audioFile;
	}

	public URL getAudioStreamURL() {
		return audioStreamURL;
	}

	public static Radio getRadio() {
		return radio;
	}

	public AnchorPane getRootNode() {
		return radioRoot;
	}
}
