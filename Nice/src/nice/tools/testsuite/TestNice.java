package nice.tools.testsuite;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.taskdefs.optional.junit.*;

import junit.textui.TestRunner;
import junit.framework.*;

import java.io.*;
import java.util.*;
import nice.tools.compiler.OutputMessages;

import nice.tools.ant.Nicec;

/**


 * @author Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 */

public class TestNice extends Task {

	File niceTestFolder;
	List sources = new ArrayList();
	List tests = new ArrayList();


	public SourceType createSource() {
		SourceType type =  new SourceType();
		sources.add(type);
		
		return type;
	}

	public TestType createTest() {
		TestType type =  new TestType();
		tests.add(type);
		
		return type;
	}

	public TestType createAsserttrue() {
		AssertTrueType type =  new AssertTrueType();
		tests.add(type);
		
		return type;
	}





	/**	Executes the ant Nice compiler.
	 */
	public void execute() throws BuildException {
		Project project = createAntProject();
		niceTestFolder = new File(project.getBaseDir(), "nice-test");
		
		//	delete the test folder
		Delete deleteTask = (Delete)project.createTask("delete");
		deleteTask.setDir(niceTestFolder);
		deleteTask.setIncludeEmptyDirs(true);
		deleteTask.perform();

		//	create the test folder
		Mkdir mkdirTask = (Mkdir)project.createTask("mkdir");
		mkdirTask.setDir(niceTestFolder);
		mkdirTask.perform();

		//	write the nice sources in the test folder
		writeSources(project);
		
		//	compile the sources
		compileSources(project);
		
		//	performs the tests
		performTests(project);
	
		
	}


	/**
		Write the sources to the filesystem
	*/
	private void writeSources(Project project) throws BuildException {
		for(Iterator it = sources.iterator(); it.hasNext();) {
			SourceType source = (SourceType)it.next();
			project.log("writing nice file: " + source.getFile(), Project.MSG_INFO);
			source.write();
		}
	}

	/**
		Returns the involved packages
	*/
	private Set getPackages() {
		Set packages = new HashSet();
		
		for(Iterator it = sources.iterator(); it.hasNext();) {
			SourceType source = (SourceType)it.next();
			packages.add(source.getPackage());
		}
		
		return packages;
	}


	/**
		Write the sources to the filesystem
	*/
	private void compileSources(Project myproject) throws BuildException {
		for(Iterator it = getPackages().iterator(); it.hasNext();) {
			Nicec nicecTask = (Nicec)myproject.createTask("nicec");
			nicecTask.setSourcepath(niceTestFolder.getAbsolutePath());
			nicecTask.setPackage((String)it.next());
			nicecTask.setJar(new File(niceTestFolder, "nice-test.jar").getAbsolutePath());
			nicecTask.perform();
			
			/*String[] argArray = {"--jar", new File(niceTestFolder, "nice-test.jar").getAbsolutePath(),
						"--runtime", Nicec.getRuntime(),
						"--sourcepath", niceTestFolder.getAbsolutePath(),
						(String)it.next()};
			int retval = nice.tools.compiler.fun.compile(argArray);
			System.out.println("retval: " + retval);
			*/
		}
	}



	/**
		Performs the junit tests.
		Creates the java source for the testsuite and compiles it.
		Then it starts the junit tests upon the java source.
	*/
	private void performTests(Project project) throws BuildException {
		//	compose the test class
		File testCasesSourceFile = new File(niceTestFolder, "TestCases.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(testCasesSourceFile));
			writer.write("import junit.framework.*;\n");
			for(Iterator it = getPackages().iterator(); it.hasNext();) {
				String packageStr = (String)it.next();
				writer.write("import " + packageStr + ".*;\n");
				writer.write("import " + packageStr + ".fun.*;\n");
			}
			
			writer.write("public class TestCases extends TestCase {\n");
			writer.write("\tpublic TestCases(String name) {super(name);}\n");
			writer.write("\tpublic static Test suite() {return new TestSuite(TestCases.class);}\n");
			for(Iterator it = tests.iterator(); it.hasNext();) {
				TestType testCase = (TestType)it.next();
				project.log("writing testcase: " + testCase.getName(), Project.MSG_INFO);
				testCase.write(writer);
			}
			writer.write("}");
			
		} catch(IOException e) {
			throw new BuildException("could not write TestCases file: " + testCasesSourceFile, e, location);
		} finally {
			try { if (writer != null) writer.close();}
			catch(IOException e) {}
		}
		
		//	compile the test class
		Javac javacTask = (Javac)project.createTask("javac");
		javacTask.setSrcdir(new Path(project, testCasesSourceFile.getParentFile().getAbsolutePath()));
		javacTask.setDestdir(testCasesSourceFile.getParentFile());
		javacTask.createInclude().setName("TestCases.java");
		javacTask.perform();
		
		
		
		
		//	run junit suite
		
		JUnitTask junitTask = (JUnitTask)project.createTask("junit");
		junitTask.addTest(new JUnitTest("TestCases", true, true));
		junitTask.createClasspath().setLocation(new File(niceTestFolder, "nice-test.jar"));
		junitTask.createClasspath().setLocation(niceTestFolder);
		junitTask.createClasspath().setPath(Path.systemClasspath.toString());
		junitTask.setHaltonfailure(true);
		junitTask.execute();
		
		
		/*junitTask.createClasspath().setLocation(new File(niceTestFolder, "nice-test.jar"));
		junitTask.createClasspath().setLocation(niceTestFolder);
		junitTask.createClasspath().setPath(Path.systemClasspath.toString());
		BatchTest bt = junitTask.createBatchTest();
		FileSet fs = new FileSet();
		fs.setDir(niceTestFolder);
		fs.createInclude().setName("TestCases.class");
		bt.addFileSet(fs);
		junitTask.execute();
		*/
		
		/*
		Java javaTask = (Java)myproject.createTask("java");
		javaTask.setClassname("junit.textui.TestRunner");
		//javaTask.setTaskname("junit");
		javaTask.setFailonerror(true);
		javaTask.createArg().setValue("TestCases");
		javaTask.createClasspath().setLocation(new File(niceTestFolder, "nice-test.jar"));
		javaTask.createClasspath().setLocation(niceTestFolder);
		javaTask.createClasspath().setPath(Path.systemClasspath.toString());
		javaTask.perform();
		*/
		
	}


	/**
		Create the ant project that performs the tests plus creation and deletion of the
		temporary files and folders
	*/
	private Project createAntProject() {
		Project project = new Project();
		//setProject(project);
		project.init();

		try {
			project.addTaskDefinition("nicec", Class.forName("nice.tools.ant.Nicec"));
		} catch (NoClassDefFoundError ncdfe) {
			// ignore...
		} catch (ClassNotFoundException cnfe) {
			// ignore...
		}

		String def = "main";
		String name = "TestNice";
		String id = null;
		String baseDir = ".";
		
		project.setDefaultTarget(def);
		
		project.setName(name);
		project.addReference(name, this);
		
		project.setBasedir(baseDir);
		
		DefaultLogger logger = new DefaultLogger();
		logger.setMessageOutputLevel(Project.MSG_WARN);
		logger.setOutputPrintStream(System.out);
		logger.setErrorPrintStream(System.out);
		project.addBuildListener(logger);

		return project;
	}



	/**
	
	*/
	public class SourceType {
		private String source = "";
		private String packageName;
		private String fileName;


		public void setFile(String file) {
			fileName = file;
		}

		public String getFile() {
			return fileName;
		}

		public void setPackage(String packageName) {
			this.packageName = packageName;
		}

		public String getPackage() {
			return packageName;
		}

		/**
		* Set a multiline message.
		*/
		public void addText(String msg) {
			source += ProjectHelper.replaceProperties(project, msg, project.getProperties());
		}


		/**
		* Writes the nice source to the specified file.
		*/
		public void write() throws BuildException {
			if (! fileName.endsWith(".nice"))
				throw new BuildException("Only nice sources can be tested!", location);
			
			File packageFolder = new File(niceTestFolder, packageName.replace('.', '/'));
			if ((! packageFolder.exists())  &&  (! packageFolder.mkdirs()))
				throw new BuildException("could not create folder :" + packageFolder, location);

			File sourceFile = new File(packageFolder, fileName);
			
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(sourceFile));
				writer.write("package " + packageName + ";");
				writer.newLine();
				writer.write(source);
				
			} catch(IOException e) {
				throw new BuildException("could not write sourcefile: " + sourceFile, e, location);
			} finally {
				try { if (writer != null) writer.close();}
				catch(IOException e) {}
			}
		}

	}




	public class TestType {
		private String name;
		private String source = "";
		private String buildFileName = new File(ProjectHelper.replaceProperties(project, "${ant.file}", project.getProperties())).getName();
		
		public void setName(String name) {
			this.name = name;
		}
	
		public String getName() {
			return name;
		}
	
		protected void setSource(String source) {
			this.source = source;
		}
	
		/**
		* Set a multiline message.
		*/
		public void addText(String msg) {
			source += ProjectHelper.replaceProperties(project, msg, project.getProperties());
		}

		public void write(BufferedWriter writer) throws IOException {
			writer.write("\tpublic void test" + hashCode() + "test() {");
			writer.newLine();
			writer.write("\t\tSystem.out.println(\"[" + buildFileName + "]  running test: " + name + "\");");
			writer.newLine();
			writer.write(source);
			writer.newLine();
			writer.write("\t}");
			writer.newLine();
		}
	}


	public class AssertTrueType extends TestType {
		/**
		* Set a multiline message.
		*/
		public void addText(String condition) {
			condition = ProjectHelper.replaceProperties(project, condition, project.getProperties());
			setName(condition.replace('\"', '\''));
			setSource("\t\tassertTrue(" + condition + ");");
		}


	
	}

}


