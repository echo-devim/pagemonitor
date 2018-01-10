package core;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import ui.notification.Notification;

public class PageMonitor {
	private final String WORK_PATH = System.getProperty("user.dir") + "/sites/";
	private final static Settings SETTINGS = new Settings();
	private long minutes_interval;
	private boolean extract_text_only;
	private ArrayList<Integer> monitored_pages;
	private Thread worker;
	private boolean background_monitor = false;
	private Function<ChangedPage,Boolean> callback = null;
	
	public PageMonitor() {
		this.minutes_interval = SETTINGS.getMinutesInterval();
		this.extract_text_only = SETTINGS.getExtractTextOnly();
		this.monitored_pages = getMonitoredPagesId();
		if (getMonitoredPages().size() != this.monitored_pages.size()) {
			System.err.println("Inconsistent status, reset monitored pages.");
			this.removeAllMonitoredPages();
		}
		this.worker = new Thread() {
			public void run(){
				if (background_monitor == true) {
					checkAllPages();
					try {
						Thread.sleep(minutes_interval * 1000 * 60);
					} catch (InterruptedException e) {
						System.err.println("Error in the thread monitor: " + e.getMessage());
						System.exit(1);
					}
				}
			}};
	}
	
	public void setCallback(Function<ChangedPage,Boolean> callback) {
		this.callback = callback;
	}
	
	public Settings getSettings() {
		return SETTINGS;
	}
	
	public void startMonitor() {
		this.background_monitor = true;
		if (!this.worker.isAlive())
			this.worker.start();
	}
	
	public void stopMonitor() {
		this.background_monitor = false;
	}
	
	public ArrayList<String> getMonitoredPages() {
		return SETTINGS.getUrls();
	}
	
	private ArrayList<Integer> getMonitoredPagesId() {
		ArrayList<Integer> monitoredPages = new ArrayList<Integer>();
		File dir = new File(this.WORK_PATH);
		if (!dir.exists())
			dir.mkdir();
		String[] files = dir.list();
		if (files.length > 0) {
		    for (String file : files) {
		    	try {
		    		monitoredPages.add(Integer.parseInt(file));
		    	} catch (NumberFormatException e) {
		    		System.err.println("Not a number: Invalid id name for file "+file);
		    	}
		    }
		}
		return monitoredPages;
	}
	
	public int addPage(String url) {
		int id = SETTINGS.urlsCount();
		HttpsClient client = new HttpsClient(url);
		try {
			Util.writeFile(this.WORK_PATH + id, client.getHtml());
			SETTINGS.addUrl(url);
		} catch (IOException e) {
			System.err.println("Connection problem: " + e.getMessage());
			id = -1;
		}
		return id;
	}
	
	public void removePage(int id) {
		try {
			Files.delete(Paths.get(this.WORK_PATH + id));
		} catch (IOException e) {
			System.err.println("Error during page deletion from disk: " + e.getMessage());
		}
		int lastId = SETTINGS.urlsCount() - 1;
		if (id < lastId) {
			File lastPage = new File(this.WORK_PATH + lastId);
			File deletedPage = new File(this.WORK_PATH + id);
			if (!lastPage.renameTo(deletedPage))
				System.err.println("Error in renaming page from " + lastPage.getAbsolutePath() + " to " + deletedPage.getAbsolutePath());
		}
		SETTINGS.removeUrl(id);
	}
	
	public void removeAllMonitoredPages() {
		File dir = new File(this.WORK_PATH);
		for (File file : dir.listFiles()) {
			if (!file.delete())
				System.err.println("Error during page deletion from disk (" + file.getAbsolutePath()+")");
		}
		SETTINGS.removeAllUrls();
	}
	
	public ArrayList<Future<Boolean>> checkAllPages() {
		ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		int id = 0;
		for (String url : SETTINGS.getUrls()) {
			final int tid = id;
			results.add(executor.submit(() -> { return checkPage(tid, url); }));
			id++;
		}
		return results;
	}
	
	private boolean checkPage(int id, String url) { //Avoid the overhead to read the url related to id from Settings
		if (!url.equals("")) {
			HttpsClient client = new HttpsClient(url);
			try {
				String newHtml = client.getHtml();
				String newOriginalHtml = newHtml;
				String oldHtml = Util.readFile(this.WORK_PATH + id);
				if (this.extract_text_only) {
					oldHtml = Util.extractText(oldHtml);
					newHtml = Util.extractText(newHtml);
				}
				int diffLen = Math.abs(newHtml.length() - oldHtml.length());
				String diffChars = Util.diff(newHtml, oldHtml);
				String message = "";
				if ((diffLen > SETTINGS.getDiffCharsThreshold()) &&
					(newHtml.length() > oldHtml.length())) {
					message = "Added new text close to " + diffChars.replaceAll("\\s+"," ");
				} else if ((diffLen > SETTINGS.getDiffCharsThreshold()) &&
							(newHtml.length() < oldHtml.length())) {
					message = "Removed text close to " + diffChars.replaceAll("\\s+"," ");
				} else {
					if (diffChars.length() > SETTINGS.getDiffCharsThreshold()) {
						message = "Changed text: " + diffChars;
					}
				}
				if (!message.equals("")) {
					Util.writeFile(this.WORK_PATH + id, newOriginalHtml); //Update local copy with the newest version
					Notification notif;
					try {
						notif = SETTINGS.getNotificationClass();
						notif.setMessage(message);
						notif.setUrl(url);
						Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
						notif.setY((id * notif.getHeight()) % (int)rect.getMaxY());
						notif.display();
						if (this.callback != null) {
							if (!this.callback.apply(new ChangedPage(id, url, message)))
								System.err.println("Callback failed with id=" + id);
						}
					} catch (Exception e) {
						e.printStackTrace();
						//System.err.println("Error in the notification creation process: " + e.getMessage());
					}
				}
			} catch (IOException e) {
				System.err.println("Connection problem: " + e.getMessage());
				return false;
			}
			return true;
		} else {
			System.err.println("Received empty url for id=" + id);
			return false;
		}
	}
	
	public boolean checkPage(int id) {
		String url = SETTINGS.getUrl(id);
		return checkPage(id, url);
	}
	
	
}
