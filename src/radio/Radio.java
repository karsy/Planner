package radio;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class Radio implements Runnable {

	private static final File channelFile = new File("src/radio/channels.txt");
	private static final FileResetter resetter = new FileResetter();

	private HashMap<Integer, HashMap<String, String>> channels = new HashMap<>();
	private int channelID = 0;
	private URL audioStreamURL;
	private InputStream audioStream;
	private FileOutputStream fos;
	private File audioFile;
	private Thread radioThread, resetThread;
	private boolean streamClosed = false, readyToPlay = false;

	@FXML
	private MediaView radioView;
	private Media audio;
	private MediaPlayer radioPlayer;

	@FXML
	private Slider volumeSlider;

	@FXML
	private Canvas canvas;
	private GraphicsContext g;
	private Image image = new Image("radio/ikoner.png");

	@FXML
	private AnchorPane radioRoot;

	public Radio() {
		audioFile = new File("audio.mp3");
		if (audioFile.exists()) {
			audioFile.delete();
		}

		readChannelFile();
	}

	private void readChannelFile() {
		try {
			BufferedReader b = new BufferedReader(new FileReader(channelFile));
			int id = 0;
			String in;
			while ((in = b.readLine()) != null) {
				HashMap<String, String> channel = new HashMap<>();
				channel.put("name", in);
				channel.put("url", b.readLine());
				channels.put(id++, channel);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void initialize() {
		volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (radioPlayer != null) {
				radioPlayer.setVolume((Double) newValue);
			}
		});

		g = canvas.getGraphicsContext2D();
		canvas.setWidth(160.0);
		canvas.setHeight(70.0);
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
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void playChannel(String url) {
		try {
			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			g.drawImage(image, channelID % 2 == 0 ? 0 : 160, 0, 160, 70, 0, 0, 160, 70);

			pauseRadio();

			audioStreamURL = new URL(url);
			streamClosed = false;

			radioThread = new Thread(this, "radioThread");
			radioThread.start();

			while (!readyToPlay) {
				Thread.sleep(50);
			}

			Thread.sleep(100);

			playAudioFromStart();

			resetThread = new Thread(resetter, "resetThread");
			resetThread.start();

		} catch (MalformedURLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void playAudioFromStart() {
		audio = new Media(audioFile.toURI().toString());
		radioPlayer = new MediaPlayer(audio);
		radioView.setMediaPlayer(radioPlayer);
		radioPlayer.play();
	}

	@FXML
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

	@FXML
	private void playRadio() {
		playChannel(channels.get(channelID).get("url"));
	}

	@FXML
	private void previousChannel() {
		channelID = channelID == 0 ? channels.size() - 1 : channelID - 1;
		playChannel(channels.get(channelID).get("url"));
	}

	@FXML
	private void nextChannel() {
		channelID = channelID == channels.size() ? 0 : channelID + 1;
		playChannel(channels.get(channelID).get("url"));
	}

	public void createAudioFile() {
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
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public URL getAudioStreamURL() {
		return audioStreamURL;
	}

	public AnchorPane getRootNode() {
		return radioRoot;
	}
}
