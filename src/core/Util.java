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
	
	//This function returns a substring starting from the first different character and
	//ending to the last different character 
	public static String diff(String newstr, String oldstr) {
		String substr = "";
		char[] first = oldstr.toCharArray();
		char[] second = newstr.toCharArray();
		int minLength = Math.min(first.length, second.length);
		int lastIndex = 1;
		int firstIndex = 0;
		boolean foundFirstIndex = false;
		boolean foundLastIndex = false;
		while (!(foundFirstIndex && foundLastIndex) && (firstIndex <= minLength-lastIndex)) {
			//Start from the begin looking for the first different char index
			if (first[firstIndex] == second[firstIndex]) {
				firstIndex++;
			} else {
				foundFirstIndex = true;
			}
			//Start from the end looking for the last different char index
			if (first[first.length-lastIndex] == second[second.length-lastIndex]) {
				lastIndex++;
			} else {
				foundLastIndex = true;
			}
		}
		//if the 2 indexes stop at different positions, this means that there is some new/deleted characters
		if (firstIndex != lastIndex-1) {
			//Check which string is the longest
			if (first.length > second.length) { //if the characters are removed from first (aka oldHtml)
				//return the deleted characters
				substr = oldstr.substring(firstIndex, first.length-lastIndex+1);
			} else { //else if the characters are added to second (aka newHtml)
				//return the added characters
				substr = newstr.substring(firstIndex, second.length-lastIndex+1);
			}
		}
		//if the different chars are at the end of first or second strings..
		if (substr.equals("")) { //From 0 to minLength the strings are equals
			//check who is the longest and return the extra-characters at the end
			if (newstr.length() > oldstr.length()) {
				return newstr.substring(oldstr.length());
			} else if (newstr.length() < oldstr.length()) {
				return oldstr.substring(newstr.length());
			}
		}
		return substr;
	}
}
