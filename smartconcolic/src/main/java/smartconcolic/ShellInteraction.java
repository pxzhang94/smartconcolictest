package smartconcolic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellInteraction {

	public static String executeCommand(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();

	}
	
	public static void setEnvironmentVariable(String envVar){
		try {

	        // using the Runtime exec method:
	        Process p = Runtime.getRuntime().exec(envVar);

	        BufferedReader stdInput = new BufferedReader(new
	             InputStreamReader(p.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new
	             InputStreamReader(p.getErrorStream()));

	        // read the output from the command
	        System.out.println("Here is the standard output of the command:\n");
	        String s;
	        while ((s = stdInput.readLine()) != null) {
	            System.out.println(s);
	        }

	        // read any errors from the attempted command
	        System.out.println("Here is the standard error of the command (if any):\n");
	        while ((s = stdError.readLine()) != null) {
	            System.out.println(s);
	        }

	        System.exit(0);
	    }
	    catch (IOException e) {
	        System.out.println("exception happened - here's what I know: ");
	        e.printStackTrace();
	        System.exit(-1);
	    }
	}

	public static String executeCommand(String winCommand) {
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(winCommand);
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();
	}
	
	/**
	 * @param output PRISM result
	 * @return the verification result
	 */
	public static double extractResultFromCommandOutput(String output){
		try{
			Pattern pattern = Pattern.compile("(Result: \\d+.\\d+)");
			Matcher matcher = pattern.matcher(output);

			Pattern pattern1 = Pattern.compile("(Result: \\d+.\\d+E-\\d+)");
			Matcher matcher1 = pattern1.matcher(output);

			if(matcher1.find()){
				String greped = matcher1.group(0);
				String[] strs = greped.split(" ");
				double result = Double.valueOf(strs[1]);
				return result;
			}

			if (matcher.find())
			{
				String greped = matcher.group(0);
				String[] strs = greped.split(" ");
				double result = Double.valueOf(strs[1]);
				return result;
			}
		}catch(Exception e){e.printStackTrace();}
		
		
		return -1;
	}
	
	public static void main(String[] args){
		String output = "Result: 66.07284553215406 (value in the initial state)";
		System.out.println(extractResultFromCommandOutput(output));
	}

}
