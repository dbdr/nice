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
	 * TODO
	 * 
	 */
	private static final String
		ERROR_MSG = "Compilation failed with errors.";
	/**
	 * TODO
	 * 
	 */
	private static final String
		BUG_MSG = "Compilation failed because of a bug in the compiler.";
	/**
	 * TODO
	 * 
	 */
	private static final String
		WARNING_MSG = "Compilation successful despite warnings.";
	/**
	 * TODO
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
	}
	
	
	/**
	 * Consumes a read line from the testsuite file.
	 * Sets the status if necessary.
	 * 
	 * @param	line	TODO
	 * @exception	TestSuiteException	TODO
	 */
	public void consumeLine(String line) throws TestSuiteException {
		if (isKeywordLine(line))
			return;

		_currentSourceFile.consumeLine(line);
	}

	
	/**
	 * Checks whether the line is a keyword line and sets the status new
	 * if it is a keyword line
	 * 
	 * @param	line	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private boolean isKeywordLine(String line) throws TestSuiteException {
		int posKeywordSign = line.indexOf(TestNice.KEYWORD_SIGN);
		if (posKeywordSign == -1)
			return false;
		
		String keywordStatement = line.substring(posKeywordSign + TestNice.KEYWORD_SIGN.length()).trim();
		
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
	 * Compiles all packages of this testcase.
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
					} catch(Exception e) {
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
	 * TODO
	 * 
	 */
	public void pass() {
		TestNice.increaseSucceeded();
		TestNice.getOutput().endTestCase(true);
	}
	
	/**
	 * TODO
	 * 
	 */
	public void fail() {
		TestNice.increaseFailed();
		
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
		
		//	compiler messages
		TestNice.getOutput().log("nicec", _compilerMessagesStream.toString());
		TestNice.getOutput().endTestCase(false);
		
		//	move contents of temp folder to a new folder in the fail folder
		TestNice.moveFilesToFailFolder();
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



}



