package ui.notification;

import java.io.IOException;

public class LinuxNotification implements Notification {
	
	private String title;
	private String message;
	private String url;

	public LinuxNotification() {
		this.title = "PageMonitor";
		this.message = "";
		this.url = "";
	}
	
	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;		
	}

	@Override
	public void setTitle(String title) {
		this.title = title;		
	}

	@Override
	public void display() {
		try {
			//Doesn't seem to work properly FIXME
			String cmd = "notify-send '" + this.title + "' '" + this.message + "\\n" + this.url + "'";
			System.out.println("Eseguo "+cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.err.println("Error in displaying the notification");
		}		
	}

	@Override
	public void setX(int x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setY(int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 100;
	}

}
