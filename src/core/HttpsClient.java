package core;

import java.security.cert.Certificate;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class HttpsClient {
	private URLConnection con = null;
	private String user_agent;
	
	
	public HttpsClient(String url) {
		this.user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0";
		url2Connection(url);
	}
	
	public HttpsClient(String url, String useragent ) {
		this.user_agent = useragent;
		url2Connection(url);
	}
	
	private void url2Connection(String url) {
	      String https_url = url;
	      URL urlObj;
	      try {
		     urlObj = new URL(https_url);
		     this.con = urlObj.openConnection();
		     //TODO: user-agent as a property in settings?
		     this.con.setRequestProperty("User-Agent", this.user_agent);
	      } catch (MalformedURLException e) {
		     System.err.println("Malformed url \""+ https_url + "\": " + e.getMessage());
	      } catch (IOException e) {
		     System.err.println("IOException: "+e.getMessage());
	      }
	}
	
	public HttpsClient setUrl(String url) {
		url2Connection(url);
		return this;
	}
	
	public String getCertInfo() throws IOException {
		String out = "";
		if ((this.con != null) && (this.con instanceof HttpsURLConnection)) {
			HttpsURLConnection httpsCon = (HttpsURLConnection)this.con;
			try {

				out += "Response Code : " + httpsCon.getResponseCode();
				out += "Cipher Suite : " + httpsCon.getCipherSuite();
				out += "\n";

				Certificate[] certs = httpsCon.getServerCertificates();
				for (Certificate cert : certs) {
					out += "Cert Type : " + cert.getType();
					out += "Cert Hash Code : " + cert.hashCode();
					out += "Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm();
					out += "Cert Public Key Format : " + cert.getPublicKey().getFormat();
					out += "\n";
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			}

		}
		return out;
	}

	public String getHtml() throws IOException {
		String out = "";
		if (this.con != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
				String input;
				while ((input = br.readLine()) != null) {
					out += input;
				}
				br.close();
		}
		return out;
	}
}
