package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import core.Settings;

public class SettingsWindow {
	
	private JFrame frame;
	private Settings settings;
	private JSlider minutesInterval;
	private JSlider diffCharsThreshold;
	private JCheckBox textOnly;
	private JCheckBox loadPageInApp;
	
	public SettingsWindow(Settings settings) {
		this.settings = settings;
		frame = new JFrame();
		frame.setTitle("Settings");
		frame.setResizable(false);
		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel lblMinutes = new JLabel("Select frequency (in minutes):");
		minutesInterval = new JSlider(JSlider.HORIZONTAL, 0, 180, 60);
		minutesInterval.setMajorTickSpacing(30);
		minutesInterval.setMinorTickSpacing(5);
		minutesInterval.setPaintTicks(true);
		minutesInterval.setPaintLabels(true);
		minutesInterval.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {
				settings.setMinutesInterval((long)minutesInterval.getValue());
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		container.add(lblMinutes, c);
		c.gridy = 1;
		container.add(minutesInterval, c);
		textOnly = new JCheckBox("Extract text only (i.e. no html)");
		textOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.setExtractTextOnly(textOnly.isSelected());
			}			
		});
		c.gridy = 2;
		c.ipady = 10;
		container.add(textOnly, c);
		JLabel lblThreshold = new JLabel("Select minimum changed chars to notify:");
		diffCharsThreshold = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
		diffCharsThreshold.setMajorTickSpacing(50);
		diffCharsThreshold.setMinorTickSpacing(10);
		diffCharsThreshold.setPaintTicks(true);
		diffCharsThreshold.setPaintLabels(true);
		diffCharsThreshold.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {
				settings.setDiffCharsThreshold(diffCharsThreshold.getValue());
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		c.gridy = 3;
		c.ipadx = 10;
		container.add(lblThreshold, c);
		c.gridy = 4;
		container.add(diffCharsThreshold, c);
		loadPageInApp = new JCheckBox("Load page in app (experimental)");
		loadPageInApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.setLoadPageInApp(loadPageInApp.isSelected());
			}			
		});
		c.gridy = 5;
		container.add(loadPageInApp, c);
		JLabel lblWarn = new JLabel("<html><font size=2><i>The settings will auto-save when you change them.<br>May you need to restart the application to see the changes.</i></font></html>");
		c.gridy = 6;
		container.add(lblWarn, c);
		frame.add(container);
		frame.pack();
	}
	
	public void show() {
		minutesInterval.setValue((int)settings.getMinutesInterval());
		diffCharsThreshold.setValue(settings.getDiffCharsThreshold());
		textOnly.setSelected(settings.getExtractTextOnly());
		loadPageInApp.setSelected(settings.getLoadPageInApp());
		frame.setVisible(true);
	}

}
