package coursework;

import model.Gui;

public class StartGui {

	/**
	 * Starts the Gui Application
	 * 
	 */
	public static void main(String[] args) {
		Parameters.setHidden(6);
		Parameters.popSize = 200;
		Parameters.mutateRate = 0.65;
		Parameters.mutateChange = 0.5;
		Parameters.replacement_rate = 0.11;
		Gui.main(null);
	}

}
