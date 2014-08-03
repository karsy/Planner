package radio;

/**
 * Created by Vegard on 14.07.14.
 */
public class FileResetter implements Runnable{

	private static boolean interrupted = false;

	@Override
	public void run() {
		interrupted = false;

		try {
			Thread.sleep(1000 * 60 * 30); // Reset every 30 min
		} catch (InterruptedException e) {
			interrupted = true;
		}

		if (!interrupted) {
			Radio radio = Radio.getRadio();
			radio.playChannel(radio.getAudioStreamURL().toString());
		}
	}
}
