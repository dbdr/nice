package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;



/**
 * Abstract representation of the test engine output.
 * This class holds a reference to the underlying writer.
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
	 * Creates an instance of AbstractWriter with the underlying writer.
	 * 
	 * @param	out	TODO
	 */
	public AbstractOutput(Writer out) {
		_writer = new OutputWriter(out);
	}


	/**
	 * Writes a string to the writer of this output.
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
	 * Marks the current position in the buffer of the writer of this output.
	 * 
	 */
	protected void mark() {
		_writer.mark();
	}


	/**
	 * Resets the buffer to the writer to the marked position.
	 * 
	 */
	protected void reset() {
		_writer.reset();
	}


	/**
	 * Closes the writer of the writer of this output.
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
	 * Flushes the writer of this output.
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
	 * Returns the line break that is specific to this output.
	 * Default is the unix type linebreak
	 * 
	 */
	protected String getLineBreak() {
		return "\n";
	}
	
	/**
	 * Returns the indentation as string that should be used in this output format.
	 * The daultvalue is an empty string.
	 * 
	 */
	protected String getIndent() {
		return "";
	}


}



