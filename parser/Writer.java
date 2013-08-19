package parser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class Writer {

	BufferedWriter writer;
	
	public Writer(String file_name) throws UnsupportedEncodingException, FileNotFoundException{
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name), "UTF8"));
	}

	public void write(String line) throws IOException{
		writer.write(line);
	}
	
	public void close() throws IOException{
		writer.close();
	}
	
}
