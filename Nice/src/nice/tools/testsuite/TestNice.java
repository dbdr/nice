package nice.tools.testsuite;


import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import nice.tools.compiler.OutputMessages;


/**
 * Example:
 
 ///	pass
	///	package a dontcompile
	int x = 3;
	System.out.println("hallo world");
	
	///	toplevel
	void f() {
		System.out.println("f()");
	}



	///	package b import a
	int z = 5;
	f();

 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$	TODO
 */

public class TestNice {

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_SIGN = "///";

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_TOPLEVEL = "toplevel";

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_PACKAGE = "package";

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_IMPORT = "import";

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_DONTCOMPILE = "dontcompile";

	/**
	 * TODO
	 * 
	 */
	private final String TESTCASE_TYPE_PASS = "pass";
	/**
	 * TODO
	 * 
	 */
	private final String TESTCASE_TYPE_FAIL = "fail";

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
	private static File _tempFolder = new File("temp-nice-testsuite");
	
	/**
	 * TODO
	 * 
	 */
	private static int _fileCounter = 0;


	/**
	 * TODO
	 * 
	 * @param	args	TODO
	 * @exception	Exception	TODO
	 */
	static public void main(String[] args) {
		if (_tempFolder.exists())
			deleteFolder(_tempFolder);
			
		_tempFolder.mkdir();
		
		try {
			new TestNice().performTests(args[0]);
		} catch(TestSuiteException e) {
			e.printStackTrace();
		}
	}


	/**
	 * TODO
	 * 
	 * @param	folder	TODO
	 */
	private static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isFile())
				file.delete();
			else
				deleteFolder(file);
		}
		folder.delete();
	}

	/**
	 * Determines the location of the nice runtime.
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
	 * This method could collect testsuite files and run the testcases inside of them.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	private void performTests(String testSuitePath) throws TestSuiteException {
		//TestSuite testsuite = new TestSuite(new File("/Users/agreif/projects/Nice/sf/testsuite/compiler/first.testsuite"));
		TestSuite testsuite = new TestSuite(new File(testSuitePath));
	
	}



	/**
	 * Class represents a testsuite file with its testcases.
	 * 
	 */
	public class TestSuite {
	
		private File _testSuiteFile;
		private List _testCases = new ArrayList();
	
		/**
			Constructor.
		*/
		public TestSuite(File testSuiteFile) throws TestSuiteException {
			_testSuiteFile = testSuiteFile;
			if (! _testSuiteFile.exists())
				throw new TestSuiteException("TestSuite file not found. File: " + _testSuiteFile);
				
			System.out.println("testsuite " + _testSuiteFile);
			readTestCases();
			performTests();
		}
	
		/**
			Reads the testsuite file line by line and creates the appropriate testcases.
		*/
		private void readTestCases() throws TestSuiteException {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(_testSuiteFile));
				String line;
				TestCase testCase = null;
				while((line = reader.readLine()) != null) {
					if (line.trim().length() == 0)	//	ignore empty lines
						continue;
						
					if (line.startsWith(KEYWORD_SIGN)) {
						testCase = createTestCase(line);
						_testCases.add(testCase);
						continue;
					}
						
					testCase.consumeLine(line);
				}
			} catch(IOException e) {
				throw new TestSuiteException("Exception while reading file: " + _testSuiteFile, e);
			} finally {
				if (reader != null)
					try {
						reader.close();
					} catch(IOException e) {
						throw new TestSuiteException("could not close file: " + _testSuiteFile);
					}
			}
		}
	
	
		/**
			Creates and returns a specific TestCase object depending upon
			the keyword
		*/
		private TestCase createTestCase(String line) throws TestSuiteException {
			String type = line.substring(KEYWORD_SIGN.length()).trim();
			System.out.println("testcase type: " + type);
			
			if (TESTCASE_TYPE_PASS.equalsIgnoreCase(type))
				return new PassTestCase();
			else if (TESTCASE_TYPE_FAIL.equalsIgnoreCase(type))
				return new FailTestCase();
			throw new TestSuiteException("Unknown testcase type: " + type);
		}
	
	
		/**
			Performs tests on all testcases in this testsuite.
			Writes the files of the testcase, and calls the specific 
			TestCase class to perform the test.
		*/
		private void performTests() throws TestSuiteException {
			for(Iterator iter = _testCases.iterator(); iter.hasNext(); ) {
				TestCase testCase = (TestCase)iter.next();
				testCase.writeFiles();
				testCase.performTest();
			}
		}
	
	}
	
	
	
	
	
	/**
	 * Class represents a testcase in the testsuite file.
	 * 
	 */
	public abstract class TestCase {
		private List _niceSourceFiles = new ArrayList();
		private NiceSourceFile _currentSourceFile;
		private Set _dontCompilePackages = new HashSet();
		
		/**
			Constructor.
		*/
		public TestCase() {
			createNewSourceFile();
		}
		
		
		/**
			Creates a new SourceFile objectand adds it to the list
			of source files and sets the current source file to this file.
		*/
		private void createNewSourceFile() {
			_currentSourceFile = new NiceSourceFile();
			_niceSourceFiles.add(_currentSourceFile);
		}
		
		
		/**
		 * Consumes a read line from the testsuite file.
		 Sets the status if necessary.
		 * 
		 */
		public void consumeLine(String line) throws TestSuiteException {
			if (isKeywordLine(line))
				return;

			_currentSourceFile.consumeLine(line);
		}

		
		/**
			Checks whether the line is a keyword line and sets the status new
			if it is a keyword line
		*/
		private boolean isKeywordLine(String line) throws TestSuiteException {
			int posKeywordSign = line.indexOf(KEYWORD_SIGN);
			if (posKeywordSign == -1)
				return false;
			
			String keywordStatement = line.substring(posKeywordSign + KEYWORD_SIGN.length()).trim();
			System.out.println("keywordStatement: " + keywordStatement);
			
			if (KEYWORD_TOPLEVEL.equalsIgnoreCase(keywordStatement.toLowerCase()))
				_currentSourceFile.setStatus(NiceSourceFile.STATUS_TOPLEVEL);
			else if (keywordStatement.startsWith(KEYWORD_PACKAGE)) {
				if (! _currentSourceFile.isEmpty())
					createNewSourceFile();

				_currentSourceFile.consumePackageKeyword(keywordStatement);
				System.out.println(" package: " + _currentSourceFile.getPackage());
				if (keywordStatement.indexOf(KEYWORD_DONTCOMPILE) != -1)
					_dontCompilePackages.add(_currentSourceFile.getPackage());
			}
			
			
			
			return true;
		}




		/**
		 * Writes the files of this testcase to disk.
		 * 
		 */
		public void writeFiles() throws TestSuiteException {
			for(Iterator iter = _niceSourceFiles.iterator(); iter.hasNext(); ) {
				NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
				sourceFile.write();
			}
		}
		
		/**
			Performs the test for this testcase
		*/
		public abstract void performTest() throws TestSuiteException;
		
		/**
			Returns the involved packages.
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
		 */
		public void compilePackages() throws TestSuiteException {
			List packageNames = getPackages();
			for (Iterator iter = packageNames.iterator(); iter.hasNext();) {
				String packageName = (String)iter.next();
				if (! _dontCompilePackages.contains(packageName))
					compilePackage(packageName);
			}
		}
		
		
		/**
		 * Compiles the specified package of this testcase.
		 * 
		 */
		private void compilePackage(String packageName) throws TestSuiteException {
/*			StringBuffer buf = new StringBuffer();
			buf.append("nicec");
			buf.append(" --sourcepath");
			buf.append(" " + _tempFolder.getAbsolutePath());
			buf.append(" --destination");
			buf.append(" " + _tempFolder.getAbsolutePath());
			buf.append(" --runtime");
			buf.append(" " + getRuntime());
			buf.append(" " + packageName);
			try {
				System.out.println(buf.toString());
				Process p = Runtime.getRuntime().exec(buf.toString());
				int retval = p.waitFor();
				System.out.println(" return value: " + retval);
				p.destroy();
				switch (retval) {
					case OutputMessages.ERROR:
						throw new TestSuiteException(ERROR_MSG);
					case OutputMessages.BUG:
						throw new TestSuiteException(BUG_MSG);
					case OutputMessages.WARNING:
						System.out.println(WARNING_MSG);
						break;
					case OutputMessages.OK:
						System.out.println(OK_MSG);
						break;
				}
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
*/
			Vector args = new Vector();

			args.addElement("--sourcepath");
			args.addElement(_tempFolder.getAbsolutePath());
			args.addElement("--destination");
			args.addElement(_tempFolder.getAbsolutePath());
			args.addElement("--runtime");
			args.addElement(getRuntime());
			args.addElement(packageName);

			String[] argArray = new String[args.size()];
			System.arraycopy(args.toArray(), 0, argArray, 0, args.size());
			int retval = nice.tools.compiler.fun.compile(argArray);
			switch (retval) {
			case OutputMessages.ERROR:
				throw new TestSuiteException(ERROR_MSG);
			case OutputMessages.BUG:
				throw new TestSuiteException(BUG_MSG);
			case OutputMessages.WARNING:
				System.out.println(WARNING_MSG);
				break;
			case OutputMessages.OK:
				System.out.println(OK_MSG);
				break;
			}

		}
		
		/**
		 * Runs the main method of the testcase. Only if main method exists and
		 the package was compiled.
		 * 
		 */
		public void runMain() throws TestSuiteException {
			for(Iterator iter = _niceSourceFiles.iterator(); iter.hasNext();) {
				NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
				if (sourceFile.hasMainMethod()
					&&  ! _dontCompilePackages.contains(sourceFile.getPackage()))
				{
					System.out.println("Run main() of file: " + sourceFile.getPackage()
										+ "."
										+ sourceFile.getClassName());
					try {
						Class c = Class.forName(sourceFile.getPackage() + ".fun",
									true,
									new TestNiceClassLoader(this.getClass().getClassLoader()));
						Class[] parameterTypes = new Class[] {String[].class};
						Method m = c.getMethod("main", parameterTypes);
						Object[] arguments = new Object[] {new String[0]};
						m.invoke(c.newInstance(), arguments);
					} catch(Exception e) {
						throw new TestSuiteException("Exception while invoking main()", e);
					}
				}
			}
		}
		
		
		
		
		
		
		/**
		 * Class represents a nice source file.
		 * 
		 */
		public class NiceSourceFile {
			public final static int STATUS_MAIN = 0;
			public final static int STATUS_TOPLEVEL = 1;
		
			private int _status = STATUS_MAIN;

			public static final String DEFAULT_PACKAGE = "defaultpackage";
			
			private String _package = DEFAULT_PACKAGE;
			private StringBuffer _mainMethodContent = new StringBuffer();
			private StringBuffer _topLevelContent = new StringBuffer();
			private Set _imports = new HashSet();
			
			private final String _className = "file_" + ++_fileCounter;
			private final String _fileName = _className + ".nice";


			/**
				Sets the status.
			*/
			public void setStatus(int status) {
				_status = status;
			}


			/**
			 * Consumes a read line from the testsuite file.
			 Sets the status if necessary.
			 * 
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
				Add a line to the main methods body.
			*/
			public void addToMainMethod(String line) {
				_mainMethodContent.append(line).append('\n');
			}

			/**
				Add a line to the main methods body.
			*/
			public void addToTopLevel(String line) {
				_topLevelContent.append(line).append('\n');
			}


			/**
				TODO
			*/
			public void consumePackageKeyword(String keywordStatement) throws TestSuiteException {
				//	get rid of the dontcompile keyword
				int dontCompilePos = keywordStatement.indexOf(KEYWORD_DONTCOMPILE);
				if (dontCompilePos != -1)
					keywordStatement = keywordStatement.substring(0, dontCompilePos)
							+ keywordStatement.substring(dontCompilePos + KEYWORD_DONTCOMPILE.length());

				keywordStatement = keywordStatement.substring(KEYWORD_PACKAGE.length()).trim();
				//	if contains no space than statement has only package name
				int spacePos = keywordStatement.indexOf(" ");
				if (spacePos == -1) {
					setPackage(keywordStatement);
					return;
				}
				
				setPackage(keywordStatement.substring(0, spacePos));

				//	look for import keyword
				int importStartPos = keywordStatement.indexOf(KEYWORD_IMPORT, spacePos);
				if (importStartPos == -1)
					throw new TestSuiteException("unknown keyword statement: " + keywordStatement);
				
				keywordStatement = keywordStatement.substring(importStartPos + KEYWORD_IMPORT.length()).trim();
				StringTokenizer tokenizer = new StringTokenizer(keywordStatement, " ,;+");
				for (;tokenizer.hasMoreTokens();)
					_imports.add(tokenizer.nextToken());
			}









			/**
				Sets the package, this file belongs to.
			*/
			public void setPackage(String packageName) {
				_package = packageName;
			}
			
			/**
				Returns the package, this file belongs to.
			*/
			public String getPackage() {
				return _package;
			}
			
			/**
				Returns the file name.
			*/
			public String getFileName() {
				return _fileName;
			}
			
			/**
				Returns the class name.
			*/
			public String getClassName() {
				return _className;
			}
			
			/**
				Writes the nice source file.
			*/
			public void write() throws TestSuiteException {
				File packageFolder = new File(_tempFolder, _package.replace('.', File.separatorChar));
				if (! packageFolder.exists()  &&  ! packageFolder.mkdirs())
					throw new TestSuiteException("could not create folder: " + packageFolder);
					
				File sourceFile = new File(packageFolder, _fileName);
				BufferedWriter writer = null;
				try {
					StringWriter debugWriter = new StringWriter();
					writer = new BufferedWriter(debugWriter);
					writePackage(writer);
					writeImports(writer);
					writeTopLevel(writer);
					writeMainMethod(writer);
					writer.close();
					System.out.println("#####\n" + debugWriter.toString() + "\n###\n");
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
				Writes the package in the nice source file.
			*/
			private void writePackage(BufferedWriter writer) throws IOException {
				writer.write("package ");
				writer.write(_package);
				writer.write(";");
				writer.newLine();
				writer.newLine();
			}
			
			/**
				Writes the import statements in the nice source file.
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
				Writes the main() method in the nice source file.
			*/
			private void writeMainMethod(BufferedWriter writer) throws IOException {
				if (! hasMainMethod())
					return;
					
				writer.write("// main method");writer.newLine();
				writer.write("main(args) {\n");
				writer.write(_mainMethodContent.toString());
				writer.write("}\n");
			}
			
			
			/**
				Writes the toplevel source in the nice source file.
			*/
			private void writeTopLevel(BufferedWriter writer) throws IOException {
				writer.write("// Top level code");writer.newLine();
				writer.write(_topLevelContent.toString());
			}
			
			
			/**
				Returns whether this file has sources for the main() method.
			*/
			public boolean hasMainMethod() {
				return _mainMethodContent.length() != 0;
			}
			
			/**
				Return whether the source file contains code or not.
			*/
			public boolean isEmpty() {
				return (_mainMethodContent.length() == 0)
					&&  (_topLevelContent.length() == 0);
			}
		}
		
	}



	/**
	 * TestCase class fot the case that the test should pass
	 * 
	 */
	public class PassTestCase extends TestCase {

		/**
			Performs the test for this testcase.
			Compilation and the execution of the main() method
			should be successfully.
		*/
		public void performTest() throws TestSuiteException {
			compilePackages();
			runMain();
		}
	
	}
	
	/**
	 * TestCase class fot the case that the test should fail
	 * 
	 */
	public class FailTestCase extends TestCase {

		/**
			Performs the test for this testcase.
			Compilation should fail, otherwise throw exception.
		*/
		public void performTest() throws TestSuiteException {
			try {
				compilePackages();
			} catch(TestSuiteException e) {
				return;
			}
			
			throw new TestSuiteException("Compilation was expected to fail, but it succeeded.");
		}
	
	}
	

	





	/**
	 * TODO
	 * 
	 */
	public class TestNiceClassLoader extends ClassLoader {
		
		public TestNiceClassLoader(ClassLoader parent) {
			super(parent);
		}

		/**
		*  This is the method where the task of class loading
		*  is delegated to our custom loader.
		*
		* @param  name the name of the class
		* @return the resulting <code>Class</code> object
		* @exception ClassNotFoundException if the class could not be found
		*/
		protected Class findClass(String name) throws ClassNotFoundException
		{
			FileInputStream fi = null;

			try {
				String path = name.replace('.', '/');
				fi = new FileInputStream(new File(_tempFolder, path + ".class"));
				byte[] classBytes = new byte[fi.available()];
				fi.read(classBytes);
				return defineClass(name, classBytes, 0, classBytes.length);
			} catch (Exception e) {
				// We could not find the class, so indicate the problem with an exception
				throw new ClassNotFoundException(name);
			} finally {
				if ( null != fi )
					try {
						fi.close();
					} catch (Exception e){}
			}
		}
	}


	/**
	 * Nice TestSuite specific exception, that holds a reference to the original exception.
	 * 
	 */
	public class TestSuiteException extends Exception {
		
		Throwable _cause;
		
		public TestSuiteException(String message) {
			super(message);
		}
		
		public TestSuiteException(String message, Throwable cause) {
			super(message);
			_cause = cause;
		}
		
		public Throwable getCause() {
			return _cause;
		}
		
		public void printStackTrace() {
			super.printStackTrace();
			if (_cause != null)
				_cause.printStackTrace();

		}
		
	}


}


