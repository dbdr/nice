/**************************************************************************/
/*                            NICE Testsuite                              */
/*             A testsuite for the Nice programming language              */
/*                         (c) Alex Greif 2002                            */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;



/**
 * This class writes the output statements of the test engine to the underlying writer.
 The output is buffered in a StringBuffer and written to out if the user flushes or
 closes the writer. This filter writer supports marking and reseting.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class OutputWriter extends FilterWriter {

	/**
	 * The buffer where the content is collected. This buffer can be flushed.
	 * 
	 */
	private StringBuffer _buf = new StringBuffer();

	/**
	 * The marked position. If the writer is flushed, then the marker is set to zero.
	 * 
	 */
	private int _mark = 0;



	/**
	 * Creates an OutputWriter instance with the destination writer.
	 * 
	 * @param	out	TODO
	 */
	public OutputWriter(Writer out) {
		super(out);
	}

	/**
	 * Write a string.
	 * 
	 * @param	str	TODO
	 * @exception	IOException	If an I/O error occurs
	 */
	public void write(String str) throws IOException {
		_buf.append(str);
	}
	
	/**
	 * Write an array of characters.
	 * 
	 * @param	chArray	TODO
	 * @exception	IOException	If an I/O error occurs
	 */
	public void write(char[] chArray) throws IOException {
		_buf.append(chArray);
	}
	
	
	/**
	 * Write a portion of a string.
	 * 
	 * @param	str	TODO
	 * @param	offset	TODO
	 * @param	length	TODO
	 * @exception	IOException	If an I/O error occurs
	 */
	public void write(String str, int offset, int length) throws IOException {
		_buf.append(str.substring(offset, length));
	}

	/**
	 * Write a single character.
	 * 
	 * @param	ch	TODO
	 */
	public void write(int ch) {
		_buf.append(ch);
	}

	/**
	 * Write a portion of an array of characters.
	 * 
	 * @param	chArray	TODO
	 * @param	offset	TODO
	 * @param	length	TODO
	 * @exception	IOException	If an I/O error occurs
	 */
	public void write(char[] chArray, int offset, int length) throws IOException {
		_buf.append(new String(chArray).substring(offset, length));
	}

	/**
	 * Flush the stream.
	 * 
	 * @exception	IOException	If an I/O error occurs
	 */
	public void flush() throws IOException {
		out.write(_buf.toString());
		out.flush();
		_mark = 0;
		reset();	//	shortcut for deleting the whole content
	}
	
	/**
	 * Close the stream.
	 * 
	 * @exception	IOException	If an I/O error occurs
	 */
	public void close() throws IOException {
		flush();
		out.close();
	}


	/**
	 * Marks the current position in the buffer.
	 * 
	 */
	public void mark() {
		_mark = _buf.length();
	}

	/**
	 * resets the buffer to the marked position.
	 * 
	 */
	public void reset() {
		_buf.delete(_mark, _buf.length());

	}


}



