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

package nice.tools.testsuite;


import java.io.*;
import java.util.*;



/**
 * Class represents a nice source file.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class NiceSourceFile {
	/**
	 * TODO
	 * 
	 */
	public final static int STATUS_MAIN = 0;
	/**
	 * TODO
	 * 
	 */
	public final static int STATUS_TOPLEVEL = 1;

	/**
	 * TODO
	 * 
	 */
	private int _status = STATUS_MAIN;

	/**
	 * TODO
	 * 
	 */
	public static final String DEFAULT_PACKAGE = "defaultpackage";
	
	/**
	 * TODO
	 * 
	 */
	private String _package = DEFAULT_PACKAGE + TestNice.getFileCounter();
	/**
	 * TODO
	 * 
	 */
	private StringBuffer _mainMethodContent = new StringBuffer();
	/**
	 * TODO
	 * 
	 */
	private StringBuffer _topLevelContent = new StringBuffer();
	/**
	 * TODO
	 * 
	 */
	private Set _imports = new HashSet();
	
	/**
	 * TODO
	 * 
	 */
	//private final String _className = "main";
	/**
	 * TODO
	 * 
	 */
	//private final String _fileName = _className + ".nice";


	/**
	 * Sets the status.
	 * 
	 * @param	status	TODO
	 */
	public void setStatus(int status) {
		_status = status;
	}


	/**
	 * Returns the status.
	 * 
	 */
	public int getStatus() {
		return _status;
	}


	/**
	 * Consumes a read line from the testsuite file.
	 * Sets the status if necessary.
	 * 
	 * @param	line	TODO
	 */
	public void consumeLine(String line) {
		switch(_status) {
			case STATUS_MAIN:
				addToMainMethod(line);
				break;
			case STATUS_TOPLEVEL:
				addToTopLevel(line);
				break;
		}
	}




	/**
	 * Adds the global import statement
	 * 
	 */
	public void addImportGlobal() {
		_imports.add(GlobalSourceFile.PACKAGE_GLOBAL);
	}







	/**
	 * Add a line to the main methods body.
	 * 
	 * @param	line	TODO
	 */
	public void addToMainMethod(String line) {
		_mainMethodContent.append(line).append('\n');
	}

	/**
	 * Add a line to the main methods body.
	 * 
	 * @param	line	TODO
	 */
	public void addToTopLevel(String line) {
		_topLevelContent.append(line).append('\n');
	}


	/**
	 * Consumes a keyword statement that starts with the word package.
	 * The dontcompile keyword is handled and package imports too.
	 * Package imports can me separated by : space colon, semicolon and the plus sign
	 * 
	 * @param	keywordStatement	TODO
	 * @exception	TestSuiteException	TODO
	 */
	public void consumePackageKeyword(String keywordStatement) throws TestSuiteException {
		//	get rid of the dontcompile keyword
		int dontCompilePos = keywordStatement.indexOf(TestNice.KEYWORD_DONTCOMPILE);
		if (dontCompilePos != -1)
			keywordStatement = keywordStatement.substring(0, dontCompilePos)
					+ keywordStatement.substring(dontCompilePos + TestNice.KEYWORD_DONTCOMPILE.length());

		keywordStatement = keywordStatement.substring(TestNice.KEYWORD_PACKAGE.length()).trim();
		//	if contains no space then statement has only package name
		int spacePos = keywordStatement.indexOf(" ");
		if (spacePos == -1) {
			setPackage(keywordStatement);
			return;
		}
		
		setPackage(keywordStatement.substring(0, spacePos));

		//	look for import keyword
		int importStartPos = keywordStatement.indexOf(TestNice.KEYWORD_IMPORT, spacePos);
		if (importStartPos == -1)
			throw new TestSuiteException("unknown keyword statement: " + keywordStatement);
		
		keywordStatement = keywordStatement.substring(importStartPos + TestNice.KEYWORD_IMPORT.length()).trim();
		StringTokenizer tokenizer = new StringTokenizer(keywordStatement, " ,;+");
		for (;tokenizer.hasMoreTokens();)
			_imports.add(tokenizer.nextToken());
	}









	/**
	 * Sets the package, this file belongs to.
	 * 
	 * @param	packageName	TODO
	 */
	public void setPackage(String packageName) {
		_package = packageName;
	}
	
	/**
	 * Returns the package, this file belongs to.
	 * 
	 */
	public String getPackage() {
		return _package;
	}
	
	/**
	 * Returns the file name.
	 * The file name is calculated prom the package name suffixed with
	 * <code>_main.nice</code>
	 * 
	 */
	public String getFileName() {
		//return _fileName;
		return _package + "_main.nice";
	}
	
	/**
	 * Returns the class name.
	 * 
	 */
	/*public String getClassName() {
		return _className;
	}
	*/
	
	/**
	 * Writes the nice source file to disc if it is not empty.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	public void write() throws TestSuiteException {
		if (isEmpty())
			return;
			
		File packageFolder = new File(TestNice.getTempFolder(), _package.replace('.', File.separatorChar));
		if (! packageFolder.exists()  &&  ! packageFolder.mkdirs())
			throw new TestSuiteException("could not create folder: " + packageFolder);
			
		File sourceFile = new File(packageFolder, getFileName());
		BufferedWriter writer = null;
		try {
			StringWriter debugWriter = new StringWriter();
			writer = new BufferedWriter(debugWriter);
			
			write(writer);
			
			writer.close();
			//System.out.println("#####\n" + debugWriter.toString() + "\n###\n");
			writer = new BufferedWriter(new FileWriter(sourceFile));
			writer.write(debugWriter.toString());
		} catch(IOException e) {
			throw new TestSuiteException("Exception while writing file: " + sourceFile, e);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch(IOException e) {
					throw new TestSuiteException("Could not close file: " + sourceFile, e);
				}
		}
	}
	
	/**
	 * Writes the contenmts to the writer
	 */
	void write(BufferedWriter writer) throws IOException {
		writePackage(writer);
		writeImports(writer);
		writeTopLevel(writer);
		writeMainMethod(writer);
	}
	
	
	
	/**
	 * Writes the package in the nice source file.
	 * 
	 * @param	writer	TODO
	 * @exception	IOException	TODO
	 */
	private void writePackage(BufferedWriter writer) throws IOException {
		writer.write("package ");
		writer.write(_package);
		writer.write(";");
		writer.newLine();
		writer.newLine();
	}
	
	/**
	 * Writes the import statements in the nice source file.
	 * 
	 * @param	writer	TODO
	 * @exception	IOException	TODO
	 */
	private void writeImports(BufferedWriter writer) throws IOException {
		for (Iterator iter = _imports.iterator(); iter.hasNext();) {
			writer.write("import ");
			writer.write((String)iter.next());
			writer.write(";");
			writer.newLine();
		}		
	}
	
	/**
	 * Writes the main() method in the nice source file.
	 * 
	 * @param	writer	TODO
	 * @exception	IOException	TODO
	 */
	private void writeMainMethod(BufferedWriter writer) throws IOException {
		if (! hasMainMethod())
			return;
			
		writer.write("// main method");writer.newLine();
		writer.write("void main(String[] args) {\n");
		writer.write(_mainMethodContent.toString());
		writer.write("}\n");
	}
	
	
	/**
	 * Writes the toplevel source in the nice source file.
	 * 
	 * @param	writer	TODO
	 * @exception	IOException	TODO
	 */
	private void writeTopLevel(BufferedWriter writer) throws IOException {
		writer.write("// Top level code");writer.newLine();
		writer.write(_topLevelContent.toString());
	}
	
	
	/**
	 * Returns whether this file has sources for the main() method.
	 * 
	 */
	public boolean hasMainMethod() {
		return true;
	}
	
	/**
	 * Return whether the source file contains code or not.
	 * 
	 */
	public boolean isEmpty() {
		return (_mainMethodContent.length() == 0)
			&&  (_topLevelContent.length() == 0);
	}
	
	/**
		Returns the number of import statements
	*/
	int getCountImports() {
		return _imports.size();
	}
	
}

// Local Variables:
// tab-width: 2
// End:
