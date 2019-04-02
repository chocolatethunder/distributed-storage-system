package app;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.awt.*;

public class DownloadListener implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
		App.downloadFile();
	}
}