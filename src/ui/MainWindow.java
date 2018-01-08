package ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.HttpsClient;
import core.PageMonitor;
import core.Settings;
import core.Util;

public class MainWindow {
	
	private final static int TEXT_SIZE = 16;
	private static DefaultListModel<String> listModel;
	private static PageMonitor pageMonitor;
	private static Settings settings;
	private static SettingsWindow settingsWindow;
	
	private static void updateList() {
		listModel.removeAllElements();
		for (String url : pageMonitor.getMonitoredPages()) {
			listModel.addElement(url);
		}
	}
	
	public static void setUIFont (javax.swing.plaf.FontUIResource f){
	    java.util.Enumeration<Object> keys = UIManager.getLookAndFeelDefaults().keys();
	    while (keys.hasMoreElements()) {
	      Object key = keys.nextElement();
	      Object value = UIManager.get (key);
	      if (value instanceof javax.swing.plaf.FontUIResource)
	        UIManager.put (key, f);
	      }
	    } 
	
	public static void main(String[] args) {
		pageMonitor = new PageMonitor();
		if ((args.length > 0) && (args[0].equals("run"))) {
			//wait the execution of all the checks, before to exit
			for (Future<Boolean> f : pageMonitor.checkAllPages()) {
				try {
					f.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.exit(0);
		}
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, use the default look and feel.
		}
		setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.PLAIN,12));
		settings = new Settings();
		settingsWindow = new SettingsWindow(settings);
		JFrame mainwindow = new JFrame();
		mainwindow.setTitle("PageMonitor");
		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel statusBar = new JLabel("status bar");
		listModel = new DefaultListModel<>();
		JList<String> listPages = new JList<>(listModel);
		JButton btnMonitor = new JButton("▶️ Start");
		btnMonitor.setFont(new Font(btnMonitor.getFont().getName(), Font.PLAIN, TEXT_SIZE));		
		btnMonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnMonitor.getText().contains("Start")) {
					pageMonitor.startMonitor();
					btnMonitor.setText("⏹️ Stop");
					statusBar.setText("Started pagemonitor for " + listModel.size() + " pages, every " + settings.getMinutesInterval() + " minutes.");
				} else {
					pageMonitor.stopMonitor();
					btnMonitor.setText("▶️ Start");
					statusBar.setText("Stopped pagemonitor");
				}
			}});
		JButton btnAdd = new JButton("➕ Add");
		btnAdd.setFont(new Font(btnAdd.getFont().getName(), Font.PLAIN, TEXT_SIZE));
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String url = JOptionPane.showInputDialog(mainwindow, "Insert the complete url (e.g. https://www.example.com/page.html):");
				if ((url != null) && (!url.equals(""))) {
					if (pageMonitor.addPage(url) < 0) {
						JOptionPane.showMessageDialog(mainwindow,
							    "Malformed url, please fix it.",
							    "Url warning",
							    JOptionPane.WARNING_MESSAGE);
					} else {
						updateList();
						statusBar.setText("Added " + url.substring(0, Math.min(url.length(), 20)) + "..");
					}
				}
			}	
		});
		JButton btnDelete = new JButton("➖ Delete");
		btnDelete.setFont(new Font(btnDelete.getFont().getName(), Font.PLAIN, TEXT_SIZE));
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!listPages.isSelectionEmpty()) {
					String value = listPages.getSelectedValue();
					pageMonitor.removePage(listPages.getSelectedIndex());
					updateList();
					statusBar.setText("Deleted " + value.substring(0,  Math.min(value.length(), 20)) + ".. ");
				}
			}});
		JButton btnDeleteAll = new JButton("🚮 Delete All");
		btnDeleteAll.setFont(new Font(btnDeleteAll.getFont().getName(), Font.PLAIN, TEXT_SIZE));
		btnDeleteAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageMonitor.removeAllMonitoredPages();
				listModel.removeAllElements();
				statusBar.setText("Deleted all the pages");
			}});
		JButton btnCheck = new JButton("➡️ Check");
		btnCheck.setFont(new Font(btnCheck.getFont().getName(), Font.PLAIN, 16));
		btnCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!listPages.isSelectionEmpty()) {
					//TODO: better to move this from the main ui thread to a background thread
					String value = listPages.getSelectedValue();
					statusBar.setText("Checking " + value.substring(0,  Math.min(value.length(), 20)) + ".. ");
					if (pageMonitor.checkPage(listPages.getSelectedIndex()))
						statusBar.setText(statusBar.getText() + "OK");
					else
						statusBar.setText(statusBar.getText() + "FAILED");
				}
			}});
		JButton btnCheckAll = new JButton("🔀 Check All");
		btnCheckAll.setFont(new Font(btnCheckAll.getFont().getName(), Font.PLAIN, TEXT_SIZE));
		btnCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusBar.setText("Checking " + listModel.size() + " page(s)");
				pageMonitor.checkAllPages();
			}});
		JButton btnSettings = new JButton("⚙️ Settings");
		btnSettings.setFont(new Font(btnSettings.getFont().getName(), Font.PLAIN, TEXT_SIZE));
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settingsWindow.show();
			}});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		container.add(btnMonitor, c);
		c.gridx = 1;
		c.gridy = 0;
		container.add(btnAdd, c);
		c.gridx = 2;
		c.gridy = 0;
		container.add(btnCheck, c);
		c.gridx = 3;
		c.gridy = 0;
		container.add(btnCheckAll, c);
		c.gridx = 4;
		c.gridy = 0;
		container.add(btnDelete, c);
		c.gridx = 5;
		c.gridy = 0;
		container.add(btnDeleteAll, c);
		c.gridx = 6;
		c.gridy = 0;
		container.add(btnSettings, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 7;
		container.add(statusBar, c);
		//TODO: embed a proper browser inside the gui (e.g. based on webkit)
		JEditorPane jep = new JEditorPane();
		jep.setEditable(false);
		jep.setContentType("text/html");
		JScrollPane scrollPanePage = new JScrollPane(jep);
		JScrollPane scrollPanePageList = new JScrollPane();
		updateList();
		listPages.setBackground(new Color(1.0f, 1.0f, 0.95f));
		listPages.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
					try {
						if ((!settings.getLoadPageInApp()) && (Desktop.isDesktopSupported())) {
						    Desktop.getDesktop().browse(new URI(listPages.getSelectedValue()));
						} else {
							HttpsClient client = new HttpsClient(listPages.getSelectedValue());
							jep.setText(client.getHtml());
						}
					} catch (Exception ex) {
						System.err.println("Could not load page: " + ex.getMessage());
						jep.setText("<html>Could not load page: " + ex.getMessage() + "</html>");
					}
				} else {
					StringSelection stringSelection = new StringSelection(listPages.getSelectedValue());
					Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
					clpbrd.setContents(stringSelection, null);
				}
			}
		});
		c.gridy = 2;
		c.weighty = 1.0;
		c.ipady = 300;
		scrollPanePageList.setViewportView(listPages);
		container.add(scrollPanePageList, c);
		if (settings.getLoadPageInApp()) {
			c.gridy = 3;
			c.ipady = 500;
			container.add(scrollPanePage, c);
		}
		mainwindow.add(container);
		mainwindow.setSize(new Dimension(800, 500));
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainwindow.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}

			@Override
			public void windowClosed(WindowEvent e) {}
			
			public void windowIconified(WindowEvent e) {
			
			}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			
		});
		mainwindow.setVisible(true);
	}
}
