package app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitListener implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
		App.exit();
	}
}