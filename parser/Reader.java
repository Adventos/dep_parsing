package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Reader {

	BufferedReader reader;
	
	public Reader(String file_name) throws UnsupportedEncodingException, FileNotFoundException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_name), "UTF8"));	
	}
	
	public String readl() throws IOException{
		return reader.readLine();
	}
	
	public void close() throws IOException{
		reader.close();
	}
	
}