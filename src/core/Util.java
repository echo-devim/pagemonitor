package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {

	public static String extractText(String html) {
		return html.replaceAll("\\<.*?>","");
	}
	
	public static String readFile(String filePath) {
        String content = "";
 
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            System.err.println("IOException in readFile function: " + e.getMessage());
        }
 
        return content;
    }
	
	public static void writeFile(String filePath, String content) {
		try {
			Files.write(Paths.get(filePath), content.getBytes());
		} catch (IOException e) {
			System.err.println("IOException in writeFile function: " + e.getMessage());
		}
	}
	
	//TODO: implement a real diff function based on a well known algorithm (e.g. Myers' algorithm)
	//This function returns a substring starting from the first different character
	public static String diff(String newstr, String oldstr) {
		String substr = "";
		char[] first = oldstr.toCharArray();
		char[] second = newstr.toCharArray();
		boolean startDiff = false;

		if (newstr.length() > oldstr.length()) {
			return newstr.substring(oldstr.length());
		} else if (newstr.length() < oldstr.length()) {
			return oldstr.substring(newstr.length());
		}
		
		int minLength = Math.min(first.length, second.length);

		for (int i = 0; i < minLength; i++) {
			startDiff = startDiff || (first[i] != second[i]);
			if (startDiff) {
				substr += second[i];
			}
		}
		return substr;
	}
}
