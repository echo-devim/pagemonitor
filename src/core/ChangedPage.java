package core;

/*
 * Simple class used by PageMonitor to return a summary of the changes
 */
public class ChangedPage {
	private int id;
	private String url;
	//maybe could be useful to add a timestamp here
	private String summary;
	
	public ChangedPage(int id, String url) {
		this.id = id;
		this.url = url;
		this.summary = "";
	}
	
	public ChangedPage(int id, String url, String summary) {
		this.id = id;
		this.url = url;
		this.summary = summary.substring(0, Math.min(summary.length(), 500)) + "..";
	}
	
	public int getId() {
		return this.id;
	}
	
	public String toString() {
		return "URL: " + this.url + "\nSummary:\n" + this.summary;
	}
	
	@Override
	public boolean equals(Object obj){
		ChangedPage other = (ChangedPage) obj;
		return (this.id == other.id) && (this.url == other.url);
	}
	
	@Override
    public int hashCode() {
		return this.id;
	}
}
