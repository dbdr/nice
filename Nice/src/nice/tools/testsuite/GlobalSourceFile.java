package nice.tools.testsuite;


import java.io.*;
import java.util.*;




/**
 * TODO
 * 
 * @author	Alex Greif
 * @version	$Id$
 */
public class GlobalSourceFile extends NiceSourceFile {

	/**
	 * TODO
	 * 
	 */
	public GlobalSourceFile() {
		setPackage("global");
	}


	/**
	 * TODO
	 * 
	 * @param	line	TODO
	 */
	public void consumeLine(String line) {
		addToTopLevel(line);
	}



}
