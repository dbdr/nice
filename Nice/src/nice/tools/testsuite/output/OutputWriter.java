package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;



/**
 * TODO
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class OutputWriter extends FilterWriter {

	/**
	 * TODO
	 * 
	 */
	private StringBuffer _buf = new StringBuffer();
	/**
	 * TODO
	 * 
	 */
	private int _mark = 0;



	/**
	 * TODO
	 * 
	 * @param	out	TODO
	 */
	public OutputWriter(Writer out) {
		super(out);
	}

	/**
	 * TODO
	 * 
	 * @param	str	TODO
	 * @exception	IOException	TODO
	 */
	public void write(String str) throws IOException {
		_buf.append(str);
	}
	
	/**
	 * TODO
	 * 
	 * @param	chArray	TODO
	 * @exception	IOException	TODO
	 */
	public void write(char[] chArray) throws IOException {
		_buf.append(chArray);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param	str	TODO
	 * @param	offset	TODO
	 * @param	length	TODO
	 * @exception	IOException	TODO
	 */
	public void write(String str, int offset, int length) throws IOException {
		_buf.append(str.substring(offset, length));
	}

	/**
	 * TODO
	 * 
	 * @param	ch	TODO
	 */
	public void write(int ch) {
		_buf.append(ch);
	}

	/**
	 * TODO
	 * 
	 * @param	chArray	TODO
	 * @param	offset	TODO
	 * @param	length	TODO
	 * @exception	IOException	TODO
	 */
	public void write(char[] chArray, int offset, int length) throws IOException {
		_buf.append(new String(chArray).substring(offset, length));
	}

	/**
	 * TODO
	 * 
	 * @exception	IOException	TODO
	 */
	public void flush() throws IOException {
		out.write(_buf.toString());
		out.flush();
		_mark = 0;
		reset();	//	shortcut for deleting the whole content
	}
	
	/**
	 * TODO
	 * 
	 * @exception	IOException	TODO
	 */
	public void close() throws IOException {
		flush();
		out.close();
	}


	/**
	 * TODO
	 * 
	 */
	public void mark() {
		_mark = _buf.length();
	}

	/**
	 * TODO
	 * 
	 */
	public void reset() {
		_buf.delete(_mark, _buf.length());

	}


}



