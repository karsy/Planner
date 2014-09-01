package radio;

import application.Controller;

/**
 * Created by Vegard on 14.07.14.
 */
public class FileResetter implements Runnable {

	@Override
	public void run() {
		boolean interrupted = false;

		try {
			Thread.sleep(1000 * 60 * 30); // Reset every 30 min
		} catch (InterruptedException e) {
			interrupted = true;
		}

		if (!interrupted) {
			Controller.getRadio().setResetFile(true);
		}
	}
}
