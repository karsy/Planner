package radio;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.SVGPath;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class Radio implements Runnable {

	private static final File channelFile = new File("src/radio/channels.txt");
	private static final FileResetter resetter = new FileResetter();

	private HashMap<Integer, Channel> channels = new HashMap<>();
	private int channelID = 0;
	private URL audioStreamURL;
	private InputStream audioStream;
	private FileOutputStream fos;
	private File audioFile = new File("audio.mp3");
	private Thread radioThread, resetThread;
	private boolean streamClosed = false, readyToPlay = false, resetFile = false;

	@FXML
	private MediaView radioView;
	private Media audio;
	private MediaPlayer radioPlayer;

	@FXML
	private Slider volumeSlider;

	@FXML
	private HBox radioContainer;

	@FXML
	private SVGPath playButton;

	@FXML
	private SVGPath pauseButton;

	@FXML
	private Canvas canvas;
	private GraphicsContext g;
	private Image image = new Image("radio/ikoner.png");

	@FXML
	private AnchorPane radioRoot;

	public Radio() {
		createAudioFile();
		readChannelFile();
	}

	private void readChannelFile() {
		try {
			BufferedReader b = new BufferedReader(new FileReader(channelFile));
			int id = 0;
			String in;
			while ((in = b.readLine()) != null) {
				if (in.startsWith("#")) {
					continue;
				}

				String[] info = in.split(",");
				Channel channel = new Channel(info[0], info[1], Double.parseDouble(info[2]), Double.parseDouble(info[3]));
				channels.put(id++, channel);
			}
			b.close();
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

		radioContainer.getChildren().remove(pauseButton);

		canvas.setWidth(160.0);
		canvas.setHeight(70.0);
		g = canvas.getGraphicsContext2D();
		g.drawImage(image, 0, 0, 160, 70, 0, 0, 160, 70);
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

			while (!streamClosed && (next = audioStream.read()) != -1 && !resetFile) {
				fos.write(next);
				fos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void playChannel(Channel channel) {
		try {
			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			g.drawImage(image, channel.imagePos.getX() * 160, channel.imagePos.getY() * 70, 160, 70, 0, 0, 160, 70);

			pauseRadio();

			if (radioContainer.getChildren().contains(playButton)) {
				radioContainer.getChildren().remove(playButton);
				radioContainer.getChildren().add(3, pauseButton);
			}

			audioStreamURL = new URL(channel.url);
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
		radioPlayer.setOnEndOfMedia(endOfMediaListener);
		radioPlayer.play();
		radioPlayer.setVolume(volumeSlider.getValue());
	}

	private Runnable endOfMediaListener = () -> {
		if (resetFile) {
			resetFile = false;
			Radio.this.createAudioFile();
			Radio.this.playChannel(channels.get(channelID));
		}
	};

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

		if (radioContainer.getChildren().contains(pauseButton)) {
			radioContainer.getChildren().remove(pauseButton);
			radioContainer.getChildren().add(3, playButton);
		}
	}

	@FXML
	private void playRadio() {
		playChannel(channels.get(channelID));
	}

	@FXML
	private void previousChannel() {
		channelID = channelID == 0 ? channels.size() - 1 : channelID - 1;
		playChannel(channels.get(channelID));
	}

	@FXML
	private void nextChannel() {
		channelID = channelID == channels.size() ? 0 : channelID + 1;
		playChannel(channels.get(channelID));
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
				resetThread.join(0);
				radioThread.join(0);
				fos.flush();
				fos.close();

				audioStream.close();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public AnchorPane getRootNode() {
		return radioRoot;
	}

	public void setResetFile(boolean resetFile) {
		this.resetFile = resetFile;
	}

	private class Channel {
		private String name, url;
		private Point2D imagePos;

		Channel(String name, String url, Double x, Double y) {
			this.name = name;
			this.url = url;
			this.imagePos = new Point2D(x, y);
		}
	}
}
