package core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import ui.notification.JavaNotification;
import ui.notification.Notification;

public class Settings {
	
	private final static Preferences prefs = Preferences.userNodeForPackage(Settings.class);
	private int urls_count;
	
	public Settings() {
		this.urls_count = prefs.getInt("urls", 0);
	}
	
	protected ArrayList<String> getUrls() {
		ArrayList<String> urls = new ArrayList<String>();
		for (int i=0; i<this.urls_count; i++) {
			urls.add(getUrl(i));
		}
		return urls;
	}
	
	protected void addUrl(String url) {
		prefs.put(String.valueOf(this.urls_count), url);
		this.urls_count++;
		prefs.put("urls", String.valueOf(this.urls_count));
	}
	
	protected void removeUrl(int id) {
		String sid = String.valueOf(id);
		this.urls_count--;
		prefs.put("urls", String.valueOf(this.urls_count));
		prefs.put(sid, prefs.get(String.valueOf(this.urls_count), ""));
		prefs.remove(String.valueOf(this.urls_count));		
	}
	
	protected void removeAllUrls() {
		for (int i=0; i<this.urls_count; i++) {
			prefs.remove(String.valueOf(i));
		}
		this.urls_count = 0;
		prefs.remove("urls");
	}
	
	public String getUrl(int id) {
		return prefs.get(String.valueOf(id), "");
	}
	
	public int urlsCount() {
		return this.urls_count;
	}
	
	public long getMinutesInterval() {
		return prefs.getLong("interval", 60);
	}
	
	public void setMinutesInterval(long minutes) {
		prefs.putLong("interval", minutes);
	}
	
	public boolean getExtractTextOnly() {
		return prefs.getBoolean("textonly", false);
	}
	
	public void setExtractTextOnly(boolean value) {
		prefs.putBoolean("textonly", value);
	}
	
	public int getDiffCharsThreshold() {
		return prefs.getInt("diff_chars_threshold", 0);
	}
	
	public void setDiffCharsThreshold(int value) {
		prefs.putInt("diff_chars_threshold", value);
	}
	
	public boolean getLoadPageInApp() {
		return prefs.getBoolean("load_page_in_app", false);
	}
	
	public void setLoadPageInApp(boolean value) {
		prefs.putBoolean("load_page_in_app", value);
	}
	
	public Notification getNotificationClass() throws Exception {
		String className = prefs.get("notification_class", "ui.notification.JavaNotification");
		Class<?> notifclass = Class.forName(className);
		Constructor<?> ctor = notifclass.getConstructor();
		Object object = ctor.newInstance();
		return (Notification) object;
	}
	
	public void setNotificationClass(Class<?> className) {
		prefs.put("notification_class", className.getName());
	}
}
