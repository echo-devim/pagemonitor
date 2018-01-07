package ui.notification;

public interface Notification {
	public void setUrl(String url);
	public void setMessage(String message);
	public void setTitle(String title);
	public void setX(int x);
	public void setY(int y);
	public int getHeight();
	public void display();
}
