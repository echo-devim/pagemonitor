package ui.notification;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

public class JavaNotification implements Notification {

	private String title;
	private String message;
	private String url;
	private final int HEIGHT = 90;
	private int x;
	private int y;
	
	public JavaNotification() {
		this.title = "PageMonitor";
		this.message = "";
		this.url = "";
		Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		this.x = (int) rect.getMaxX();
		this.y = 10;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setMessage(String message) {
		if (message.length() <= 50)
			this.message = message;
		else {
			this.message = message.substring(0, 50) + "..";
		}
	}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}

	@Override
	public void display() {
		JFrame frame = new JFrame("PageMonitor");
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setType(Type.POPUP);
		frame.setShape(new RoundRectangle2D.Double(0,0, 355,80, 50,100));
		JPanel boxinfo = new JPanel();
		boxinfo.setPreferredSize(new Dimension(250, 70));
		boxinfo.setBackground(new Color(0.6f, 0.8f, 1f));
		Border padding = BorderFactory.createEmptyBorder(5, 10, 10, 10);
		boxinfo.setBorder(padding);
		boxinfo.setLayout(new BoxLayout(boxinfo, BoxLayout.Y_AXIS));
		String domain = this.url;
		URI uri;
		try {
			uri = new URI(this.url);
		    domain = uri.getHost();
		} catch (URISyntaxException e1) {
			System.err.println("Error in parsing URI from url in JavaNotification: " + e1.getMessage());
		}
		JLabel lblTitle = new JLabel(domain);
		lblTitle.setSize(100, 10);
		lblTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblTitle.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
				    try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (Exception e1) {}
				}				
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			
		});
		Font newLabelFont=new Font(lblTitle.getFont().getName(),Font.BOLD,14);
		lblTitle.setFont(newLabelFont);
		JLabel lblMessage = new JLabel(this.message);
		lblMessage.setSize(180, 10);
		lblMessage.setFont(new Font(lblMessage.getFont().getName(), Font.PLAIN, 14));
		JPanel mainpanel = new JPanel();
		mainpanel.setBackground(new Color(0.1f,0.3f,0.5f,1f));
		mainpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton btnClose = new JButton("✖️");
		btnClose.setFont(new Font(btnClose.getFont().getName(), Font.BOLD, 30));
		btnClose.setForeground(Color.WHITE);
		btnClose.setBackground(new Color(0.f, 0.f, 0.f, 0.f));
		btnClose.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		btnClose.setFocusable(false);
		btnClose.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {
				btnClose.setForeground(new Color(0.9f, 0.3f, 0.3f));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				btnClose.setForeground(Color.WHITE);
			}
			
		});
		boxinfo.add(lblTitle);
		boxinfo.add(lblMessage);
		mainpanel.add(btnClose);
		mainpanel.add(boxinfo);
		frame.getContentPane().add(mainpanel);
		frame.pack();
		int x = this.x - (frame.getWidth() + 10);
		frame.setLocation(x, this.y);
		frame.setVisible(true);
		int original_x = frame.getLocation().x;
		Timer animation = new Timer(5, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (frame.getLocation().x - original_x < 200) {
					frame.setLocation(frame.getLocation().x+5, y);
				} else
					frame.dispose();
			}});
    	Timer pause = new Timer(5000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				animation.start();
			}});
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) {
	        	pause.start();
			}
			
	        public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {
				pause.stop();
				animation.stop();
			}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

}
