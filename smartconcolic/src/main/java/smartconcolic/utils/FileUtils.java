package smartconcolic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	public static void cleanDirectory(String path){
		File dir = new File(path);
		if(!dir.exists()){
			return;
		}
		for(File file : dir.listFiles()){
			file.delete();
		}
	}

	public static void writeObject(String fileName, Object obj) throws FileNotFoundException, IOException{
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream(fileName));
		objectOutputStream.writeObject(obj);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public static Object readObject(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(fileName));
		Object obj =  objectInputStream.readObject();
		objectInputStream.close();
		return obj;
	}


	public static boolean isDirEmpty(String dirPath) throws IOException {
		if(new File(dirPath).list().length>0){
			return false;
		}
		return true;

	}

	public static void writeStringToFile(String filepath, String str) throws FileNotFoundException{
		PrintWriter out = new PrintWriter(filepath);
		out.println(str);
		out.close();
	}

	public static void appendStringToFile(String filepath, String str) throws FileNotFoundException{

		File file =new File(filepath);

		//if file doesnt exists, then create it
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//true = append file
		try {
			FileWriter fw = new FileWriter(filepath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			out.println(str);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static List<String> filesInDir(String dirPath){
		List<String> files = new ArrayList<String>();
		File folder = new File(dirPath);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if(file.getAbsolutePath().endsWith(".txt") || file.getAbsolutePath().endsWith(".csv")){
				files.add(file.getAbsolutePath());
			}
		}
		return files;
	}

	public static void createDir(String dirPath){
		File theDir = new File(dirPath);
		if(!theDir.exists()){
			theDir.mkdirs();
		}
	}
	
	public static String readString(String path) throws Exception {
        StringBuffer sb= new StringBuffer("");
        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
       
        String str = null;
       
        while((str = br.readLine()) != null) {
              sb.append(str+"/n");            
        }
       
        br.close();
        reader.close();
		return sb.toString();
	}

}
