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
import java.lang.reflect.*;

import nice.tools.compiler.OutputMessages;





/**
 * Class represents a testcase in the testsuite file.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class TestCase {
	/**
	 * Compiler message
	 * 
	 */
	private static final String
		ERROR_MSG = "Compilation failed with errors.";
	/**
	 * Compiler message
	 * 
	 */
	private static final String
		BUG_MSG = "Compilation failed because of a bug in the compiler.";
	/**
	 * Compiler message
	 * 
	 */
	private static final String
		WARNING_MSG = "Compilation successful despite warnings.";
	/**
	 * Compiler message
	 * 
	 */
	private static final String
		OK_MSG = "Compilation successful.";
		


	/**
	 * TODO
	 * 
	 */
	private TestSuite _testSuite;
	/**
	 * TODO
	 * 
	 */
	private List _niceSourceFiles = new ArrayList();
	/**
	 * TODO
	 * 
	 */
	private NiceSourceFile _currentSourceFile;
	/**
	 * TODO
	 * 
	 */
	private Set _dontCompilePackages = new HashSet();
	/**
	 * TODO
	 * 
	 */
	private ByteArrayOutputStream _compilerMessagesStream;
	
	/**
	 * Positions where failures should occur, defined by the user.
	 * 
	 */
	private List _failPositions = new ArrayList();

	/**
		Counter that keeps track of the lines written to the
		testcase source file. Needed for comparisons of
		user defined failure positions to compiler reported
		failure positions
	*/
	private int _lineCounter = 0;





	/**
	 * Constructor.
	 * 
	 * @param	suite	TODO
	 */
	public TestCase(TestSuite suite) {
		_testSuite = suite;
		createNewSourceFile();
	}
	
	
	/**
	 * Creates a new SourceFile objectand adds it to the list
	 * of source files and sets the current source file to this file.
	 * 
	 */
	private void createNewSourceFile() {
		_currentSourceFile = new NiceSourceFile();
		_niceSourceFiles.add(_currentSourceFile);
		
		if (_testSuite.hasGlobalSource())
			_currentSourceFile.addImportGlobal();
			
		_lineCounter = 1;
	}
	
	/**
		Returns a list of expected failure positions defined by the user.
	*/
	protected List getFailPositions() {
		return _failPositions;
	}
	
	
	/**
	 * Consumes a read line from the testsuite file.
	 * Sets the status if necessary.
	 * 
	 * @param	line	TODO
	 * @exception	TestSuiteException	TODO
	 */
	public void consumeLine(String line) throws TestSuiteException {
		//System.out.println("line " + _lineCounter + "   : " + line);
		if (consumeKeywordLine(line))
			return;

		_lineCounter++;
		consumeCommentedKeyword(line);
		
		_currentSourceFile.consumeLine(line);
	}

	
	/**
	 * Checks whether the line is a keyword line and sets the status new
	 * if it is a keyword line. Packages are also recorded in the dontcompile-collection
	 * if the word "dontcompile" occures in the keyword-line. Comments are delegated to the testsuite.
	 * 
	 * @param	line	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private boolean consumeKeywordLine(String line) throws TestSuiteException {
		line = line.trim();
		if (!line.startsWith(TestNice.KEYWORD_SIGN))
			return false;
		
		String keywordStatement = line.substring(TestNice.KEYWORD_SIGN.length()).trim();
		//System.out.println("keywordStatement: " + keywordStatement);
		if (TestNice.KEYWORD_TOPLEVEL.equalsIgnoreCase(keywordStatement.toLowerCase()))
			_currentSourceFile.setStatus(NiceSourceFile.STATUS_TOPLEVEL);
		else if (keywordStatement.startsWith(TestNice.KEYWORD_PACKAGE)) {
			if (! _currentSourceFile.isEmpty())
				createNewSourceFile();

			_currentSourceFile.consumePackageKeyword(keywordStatement);
			if (keywordStatement.indexOf(TestNice.KEYWORD_DONTCOMPILE) != -1)
				_dontCompilePackages.add(_currentSourceFile.getPackage());
		} else
			//	assume its a comment and delegate it to the testsuite
			_testSuite.consumeComment(line);
		
		return true;
	}


	/**
		Consumes keywords embedded in comments.
		Currently only <code>FAIL HERE</code> is supported.
	*/
	private void consumeCommentedKeyword(String line) {
		int pos = 0;
		while(true) {
			int startCommentPos = line.indexOf("/*", pos);
			if (startCommentPos == -1)
				break;
			int endCommentPos = line.indexOf("*/", startCommentPos + 2);
			pos = endCommentPos;
			//System.out.println("startCommentPos: " + startCommentPos);
			//System.out.println("endCommentPos: " + endCommentPos);
			if (startCommentPos == -1  ||  endCommentPos == -1)
				return;
				
			String comment = line.substring(startCommentPos, endCommentPos);
			int keywordSignPos = line.indexOf(TestNice.KEYWORD_SIGN);
			//System.out.println("keywordSignPos: " + keywordSignPos);
			if (keywordSignPos < startCommentPos  ||  endCommentPos < keywordSignPos)
				return;
			
			String keywordStatement = line.substring(keywordSignPos + TestNice.KEYWORD_SIGN.length(), endCommentPos).trim();
			//System.out.println("keywordStatement: <" + keywordStatement + ">");
			
			if (TestNice.KEYWORD_FAILHERE.equalsIgnoreCase(keywordStatement.toLowerCase())) {
				//	determine column
				int columnNum = endCommentPos + 2;
				while(Character.isWhitespace(line.charAt(columnNum)))
					columnNum++;
				int lineNum = _lineCounter + _currentSourceFile.getCountImports() + 2;
				if (_currentSourceFile.getStatus() == NiceSourceFile.STATUS_MAIN)
					lineNum += 2;
				//System.out.println("_lineCounter: " + _lineCounter + "      _currentSourceFile.getCountImports(): " + _currentSourceFile.getCountImports());
				_failPositions.add(new FailPosition(_currentSourceFile.getFileName(), lineNum, columnNum + 1));
			}
		}
	}


	/**
	 * Writes the files of this testcase to disk.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	public void writeFiles() throws TestSuiteException {
		for (Iterator iter = _niceSourceFiles.iterator(); iter.hasNext(); ) {
			NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
			sourceFile.write();
		}
		
		if (_testSuite.hasGlobalSource())
			_testSuite.getGlobalSource().write();
	}
	
	/**
	 * Performs the test for this testcase
	 * 
	 */
	public void performTest() {
		TestNice.getOutput().startTestCase(this);
	}
	
	/**
	 * Returns the involved packages.
	 * 
	 */
	private List getPackages() {
		List packages = new ArrayList();
	
		for (Iterator iter = _niceSourceFiles.iterator(); iter.hasNext();) {
			NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
			if (! packages.contains(sourceFile.getPackage()))
				packages.add(sourceFile.getPackage());
		}

		return packages;
	}


	/**
	 * Compiles all packages of this testcase except it is listed in the dontcompile-collection.
	 * 
	 * @exception	TestSuiteException	TODO
	 * @exception	CompilerBugException	TODO
	 */
	public void compilePackages() throws TestSuiteException, CompilerBugException {
		_compilerMessagesStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(_compilerMessagesStream);
		PrintStream origOut = System.out;
		PrintStream origErr = System.err;
		System.setOut(out);
		System.setErr(out);
		boolean showMessages = false;
		
		try {
			List packageNames = getPackages();
			for (Iterator iter = packageNames.iterator(); iter.hasNext();) {
				String packageName = (String)iter.next();
				if (! _dontCompilePackages.contains(packageName)) {
					int retval = compilePackage(packageName);
					switch (retval) {
					case OutputMessages.ERROR:
						showMessages = true;
						throw new TestSuiteException(ERROR_MSG);
					case OutputMessages.BUG:
						showMessages = true;
						throw new CompilerBugException(BUG_MSG);
					case OutputMessages.WARNING:
						showMessages = true;
						break;
					}
				}
			}
		} finally {
			System.setOut(origOut);
			System.setErr(origErr);
			try {
				_compilerMessagesStream.close();
				out.close();
			} catch(IOException e) {}
		}
	}
	
	
	/**
	 * Compiles the specified package of this testcase.
	 * 
	 * @param	packageName	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private int compilePackage(String packageName) throws TestSuiteException {
		return nice.tools.compiler.fun.compile
			(packageName,
			TestNice.getTempFolder().getAbsolutePath(),
			TestNice.getTempFolder(),
			null,//classpath,
			null,//jar,
			null,//output,
			false,//recompile,
			false,//recompile_all,
			false,//compile,
			false,//exclude_runtime,
			getRuntime(),
			null,//native_compiler,
			false,//help,
			false,//editor,
			false,//man,
			false,//version,
			false,//usage,
			false//memory
			);
	}
	
	/**
	 * Runs the main method of the testcase. Only if main method exists and
	 * the package was compiled.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	public void runMain() throws TestSuiteException {
		ByteArrayOutputStream mainMessagesStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(mainMessagesStream);
		PrintStream origOut = System.out;
		PrintStream origErr = System.err;
		System.setOut(out);
		System.setErr(out);
		try {
			for(Iterator iter = _niceSourceFiles.iterator(); iter.hasNext();) {
				NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
				if (sourceFile.hasMainMethod()
					&&  ! _dontCompilePackages.contains(sourceFile.getPackage()))
				{
					try {
						Class c = Class.forName(sourceFile.getPackage() + ".fun",
									true,
									new TestSuiteClassLoader(this.getClass().getClassLoader()));
						Class[] parameterTypes = new Class[] {String[].class};
						Method m = c.getMethod("main", parameterTypes);
						Object[] arguments = new Object[] {new String[0]};
						m.invoke(c.newInstance(), arguments);
					} catch(Throwable e) {
						throw new TestSuiteException("Exception while invoking main()", e);
					}
				}
			}
		} finally {
			System.setOut(origOut);
			System.setErr(origErr);
			try {
				mainMessagesStream.close();
				out.close();
			} catch(IOException e) {}
			if (mainMessagesStream.size() != 0)
				TestNice.getOutput().log("main", mainMessagesStream.toString());
		}
	}
	
	
	/**
	 * Called by a specific testcase if the testcase succeeds.
	 * This is the place for output messages.
	 * 
	 */
	public void pass() {
		TestNice.increaseSucceeded();
		TestNice.getOutput().endTestCase(true);
	}
	
	/**
	 * Called by a specific testcase if the testcase fails.
	 * This is the place for output messages.
	 */
	public void fail() {
		TestNice.increaseFailed();
		
		//	log the sources
		printSources();
		
		//	compiler messages
		TestNice.getOutput().log("nicec", getCompilerMessages());
		TestNice.getOutput().endTestCase(false);
		
		//	move contents of temp folder to a new folder in the fail folder
		TestNice.moveFilesToFailFolder();
	}
	
	/**
		Called if a warning occures. For example if FAIL_HERE is at a wrong position.
	*/
	public void warning() {
		TestNice.increaseWarning();
		
		//	log the sources
		printSources();
		
		//	compiler messages
		TestNice.getOutput().log("nicec", getCompilerMessages());
		TestNice.getOutput().endTestCase(false);
		
		//	move contents of temp folder to a new folder in the fail folder
		TestNice.moveFilesToFailFolder();	
	}
	
	
	
	/**
		Print global and local sources that are involved in this testcase.
	*/
	private void printSources() {
		BufferedWriter writer;
		StringWriter contentWriter;
		//	ordinary files
		for (Iterator iter = _niceSourceFiles.iterator(); iter.hasNext(); ) {
			NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
			contentWriter = new StringWriter();
			writer = new BufferedWriter(contentWriter);
			try {
				sourceFile.write(writer);
				contentWriter.close();
				writer.close();
			} catch(IOException e) {e.printStackTrace();}
			TestNice.getOutput().log("file " + sourceFile.getPackage() + "." + sourceFile.getFileName(),
						contentWriter.toString());
			TestNice.getOutput().log("");
		}
		//	global file
		if (_testSuite.hasGlobalSource()) {
			contentWriter = new StringWriter();
			writer = new BufferedWriter(contentWriter);
			try {
				_testSuite.getGlobalSource().write(writer);
				contentWriter.close();
				writer.close();
			} catch(IOException e) {e.printStackTrace();}
			TestNice.getOutput().log("file " + _testSuite.getGlobalSource().getPackage() + "." + _testSuite.getGlobalSource().getFileName(),
						contentWriter.toString());
			TestNice.getOutput().log("");
		}
	}
	
	
	
	
	/**
		Returns the compiler messages as String
	*/
	protected String getCompilerMessages() {
		return _compilerMessagesStream.toString();
	}
	
	
	/**
	 * Determines the location of the nice runtime.
	 * This method is also used by Nicec
	 * 
	 */
	private String getRuntime() {
		//	when Nicec is in the nice.jar, then we should use
		//	nice.tools.ant.Nicec dummy = new ...
		nice.tools.runJar dummy = new nice.tools.runJar();
		String resource = nice.tools.runJar.class.getName();

		// Format the file name into a valid resource name.
		if (!resource.startsWith("/")) {
			resource = "/" + resource;
		}
		resource = resource.replace('.', '/');
		resource = resource + ".class";

		// Attempt to locate the file using the class loader.
		java.net.URL classUrl = TestNice.class.getResource(resource);
		
		if (classUrl == null) {
			return null;
		} else {
			String file = classUrl.getFile();
			try {
				//	handle as jarfile
				return file.substring(file.indexOf(":")+1, file.indexOf("!"));
			} catch(StringIndexOutOfBoundsException e) {
				//	oops it is a class file
				return file.substring(0, file.indexOf(resource));
			}
		}
	}





	/**
		InnerClass that holds a user defined failure position.
	*/
	protected class FailPosition {
		private String _fileName;
		private int _line;
		private int _column;
		
		FailPosition(String fileName, int line, int column) {
			_fileName = fileName;
			_line = line;
			_column = column;
		}
		
		protected String getFileName() {
			return _fileName;
		}
		
		protected int getLine() {
			return _line;
		}
		
		protected int getColumn() {
			return _column;
		}
	}

}



