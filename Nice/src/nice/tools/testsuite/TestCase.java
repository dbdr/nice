package nice.tools.testsuite;


import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import nice.tools.compiler.OutputMessages;





/**
 * Class represents a testcase in the testsuite file.
 * 
 */
public abstract class TestCase {
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
		int posKeywordSign = line.indexOf(TestNice.KEYWORD_SIGN);
		if (posKeywordSign == -1)
			return false;
		
		String keywordStatement = line.substring(posKeywordSign + TestNice.KEYWORD_SIGN.length()).trim();
		//System.out.println("keywordStatement: " + keywordStatement);
		
		if (TestNice.KEYWORD_TOPLEVEL.equalsIgnoreCase(keywordStatement.toLowerCase()))
			_currentSourceFile.setStatus(NiceSourceFile.STATUS_TOPLEVEL);
		else if (keywordStatement.startsWith(TestNice.KEYWORD_PACKAGE)) {
			if (! _currentSourceFile.isEmpty())
				createNewSourceFile();

			_currentSourceFile.consumePackageKeyword(keywordStatement);
			//System.out.println(" package: " + _currentSourceFile.getPackage());
			if (keywordStatement.indexOf(TestNice.KEYWORD_DONTCOMPILE) != -1)
				_dontCompilePackages.add(_currentSourceFile.getPackage());
		}
		
		
		
		return true;
	}




	/**
	 * Writes the files of this testcase to disk.
	 * 
	 */
	public void writeFiles() throws TestSuiteException {
		for (Iterator iter = _niceSourceFiles.iterator(); iter.hasNext(); ) {
			NiceSourceFile sourceFile = (NiceSourceFile)iter.next();
			sourceFile.write();
		}
		
		TestNice.getGlobalSource().write();
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
		Vector args = new Vector();

		args.addElement("--sourcepath");
		args.addElement(TestNice.getTempFolder().getAbsolutePath());
		args.addElement("--destination");
		args.addElement(TestNice.getTempFolder().getAbsolutePath());
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
				/*System.out.println("Run main() of file: " + sourceFile.getPackage()
									+ "."
									+ sourceFile.getClassName());
				*/
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



}



