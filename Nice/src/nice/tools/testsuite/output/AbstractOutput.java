package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;



/**
 * TODO
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public abstract class AbstractOutput implements Output {

	
	/**
	 * Wrapper around the original writer.
	 * 
	 */
	private OutputWriter _writer;

	/**
	 * TODO
	 * 
	 * @param	out	TODO
	 */
	public AbstractOutput(Writer out) {
		_writer = new OutputWriter(out);
	}


	/**
	 * TODO
	 * 
	 * @param	str	TODO
	 */
	private void write(String str) {
		try {
			_writer.write(str);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * TODO
	 * 
	 */
	protected void mark() {
		_writer.mark();
	}
	/**
	 * TODO
	 * 
	 */
	protected void reset() {
		_writer.reset();
	}

	/**
	 * TODO
	 * 
	 */
	public void close() {
		try {
			_writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * TODO
	 * 
	 */
	protected void flush() {
		try {
			_writer.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Logs a statement to this Output.
	 * 
	 * @param	statement	TODO
	 */
	public void log(String statement) {
		log(null, statement);
	}


	/**
	 * Logs a statement with the given prefix in angled braces.
	 * 
	 * @param	prefix	TODO
	 * @param	statement	TODO
	 */
	public void log(String prefix, String statement) {
		if (statement.length() == 0) {	//	workaround, reader returns null for ""
			write(getIndent());
			write((prefix == null ? "" : "["+prefix+"] ") + statement + getLineBreak());
			return;
		}
		
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new StringReader(statement));
			while((line = reader.readLine()) != null) {
				write(getIndent());
				write((prefix == null ? "" : "["+prefix+"] ") + line + getLineBreak());
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
		}
	}


	/**
	 * Logs a statement to this output and flushes the writer.
	 * 
	 * @param	statement	TODO
	 */
	public void logAndFlush(String statement) {
		log(null, statement);
		flush();
	}
	
	
	/**
	 * Logs a statement with the given prefix in angled braces and flushes the writer.
	 * 
	 * @param	prefix	TODO
	 * @param	statement	TODO
	 */
	public void logAndFlush(String prefix, String statement) {
		log(prefix, statement);
		flush();
	}





	/**
	 * TODO
	 * 
	 */
	protected String getLineBreak() {
		return "\n";
	}
	
	/**
	 * TODO
	 * 
	 */
	protected String getIndent() {
		return "";
	}


}



