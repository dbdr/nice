package nice.tools.ant;

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
		<h2><a name="java">Nicec</a></h2>
		<h3>Description</h3>
		<p>Runs the Nice compiler.</p>
		All arguments to the Nice compiler Task has to be placed as attributes in the nicec xml-element.
		<h3>Parameters</h3>
		<table border="1" cellpadding="2" cellspacing="0">
			<tr>
				<td valign="top"><b>Attribute</b></td>
				<td valign="top"><b>Description</b></td>
				<td align="center" valign="top"><b>Required</b></td>
			</tr>
			<tr>
				<td valign="top">package</td>
				<td valign="top">The Nice package to compile.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">jar</td>
				<td valign="top">Compile the Nice sources to archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">sourcepath</td>
				<td valign="top">Lookup path for source packages. Is a list of directories and .jar archives.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">destination</td>
				<td valign="top">Destination directory for compiled packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">packagepath</td>
				<td valign="top">Additional lookup path for compiled packages. Is a list of directories and .jar archives</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">classpath</td>
				<td valign="top">Lookup path for external classes.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">output</td>
				<td valign="top">Generate native executable.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">compile</td>
				<td valign="top">Compile packages but do not link them.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">recompile</td>
				<td valign="top">Force recompilation of package.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">recompile_all</td>
				<td valign="top">Force recompilation of all dependant packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">exclude_runtime</td>
				<td valign="top">Avoid inclusion of the runtime in the archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">runtime</td>
				<td valign="top">Location of nice.jar.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">native_compiler</td>
				<td valign="top">Location of the native compiler binary (gcj).</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">help</td>
				<td valign="top">Print help message and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">editor</td>
				<td valign="top">Tell nicec that it is called by an editor.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">man</td>
				<td valign="top">Print man page to stdout.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">version</td>
				<td valign="top">Print version info and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">usage</td>
				<td valign="top">Print usage information and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">memory</td>
				<td valign="top">Print memory usage information after compilation.</td>
				<td align="center" valign="top">No</td>
			</tr>
		</table>
		<h3>Examples</h3>
		<pre>&lt;taskdef name=&quot;nicec&quot; classname=&quot;nice.tools.ant.Nicec&quot;/&gt;<br>
			&lt;target name=&quot;nice-compiler&quot;&gt;<br>
			  &lt;nicec package=&quot;test&quot; runtime=&quot;../share/java/nice.jar&quot;/&gt;<br>
			&lt;/target&gt;</pre>


 * @author Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 */

public class Nicec extends Task {



	/**	Lookup path for source packages
	 PATH is a list of directories and .jar archives
	 */
	private String sourcepath;

	public void setSourcepath(String sourcepath)
	{
		this.sourcepath = sourcepath;
	}


	/**	Destination directory for compiled packages
	 */
	private File destination;

	public void setDestination(File destination)
	{
		this.destination = destination;
	}


	/**	Additional lookup path for compiled packages
	 PATH is a list of directories and .jar archives
	 */
	private String packagepath;

	public void setPackagepath(String packagepath)
	{
		this.packagepath = packagepath;
	}


	/**	Lookup path for external classes
	 */
	private String classpath;

	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
	}

	/**	Compile to archive
	 You can then run the program with 'java -jar FILE'
	 */
	private String jar;

	public void setJar(String jar)
	{
		this.jar = jar;
	}


	/**	Generate native executable
	 */
	private String output;

	public void setOutput(String output)
	{
		this.output = output;
	}


	/**	Force recompilation of package
	 */
	private boolean recompile;

	public void setRecompile(boolean recompile)
	{
		this.recompile = recompile;
	}


	/**	Force recompilation of all dependant packages
	 */
	private boolean recompile_all;

	public void setRecompile_all(boolean recompile_all)
	{
		this.recompile_all = recompile_all;
	}


	/**	Compile packages but do not link them
	 */
	private boolean compile;

	public void setCompile(boolean compile)
	{
		this.compile = compile;
	}


	/**	Avoid inclusion of the runtime in the archive
	 */
	private boolean exclude_runtime;

	public void setExclude_runtime(boolean exclude_runtime)
	{
		this.exclude_runtime = exclude_runtime;
	}

	/**	Location of nice.jar
	 */
	private String runtime;

	public void setRuntime(String runtime)
	{
		this.runtime = runtime;
	}

	/**	Location of the native compiler binary (gcj)
	 */
	private String native_compiler;

	public void setNative_compiler(String native_compiler)
	{
		this.native_compiler = native_compiler;
	}








	/**	Print help message and exit
	 */
	private boolean help;

	public void setHelp(boolean help)
	{
		this.help = help;
	}


	/**	Tell nicec that it is called by an editor.
	 */
	private boolean editor;

	public void setEditor(boolean editor)
	{
		this.editor = editor;
	}


	/**	Print man page to stdout
	 */
	private boolean man;

	public void setMan(boolean man)
	{
		this.man = man;
	}


	/**	Print version info and exit
	 */
	private boolean version;

	public void setVersion(boolean version)
	{
		this.version = version;
	}


	/**	Print usage information and exit
	 */
	private boolean usage;

	public void setUsage(boolean usage)
	{
		this.usage = usage;
	}


	/**	Print memory usage information after compilation
	 */
	private boolean memory;

	public void setMemory(boolean memory)
	{
		this.memory = memory;
	}

	/**	The package to compile
	 */
	private String pack;

	public void setPackage(String pack)
	{
		this.pack = pack;
	}







	/**	Executes the ant Nice compiler by reflection.
	 */
	public void execute() throws BuildException {
		Vector args = new Vector();

		if (sourcepath != null) {
			args.addElement("--sourcepath");
			args.addElement(sourcepath);
		}
		if (destination != null) {
			args.addElement("--destination");
			args.addElement(destination.getAbsolutePath());
		}
		if (packagepath != null) {
			args.addElement("--packagepath");
			args.addElement(packagepath);
		}
		if (classpath != null) {
			args.addElement("--classpath");
			args.addElement(classpath);
		}
		if (jar != null) {
			args.addElement("--jar");
			args.addElement(jar);
		}
		if (output != null) {
			args.addElement("--output");
			args.addElement(output);
		}
		if (recompile) {
			args.addElement("--recompile");
		}
		if (recompile_all) {
			args.addElement("--recompile-all");
		}
		if (compile) {
			args.addElement("--compile");
		}
		if (exclude_runtime) {
			args.addElement("--exclude-runtime");
		}
		if (runtime != null) {
			args.addElement("--runtime");
			args.addElement(runtime);
		}
		if (native_compiler != null) {
			args.addElement("--native-compiler");
			args.addElement(native_compiler);
		}
		if (help) {
			args.addElement("--help");
		}
		if (editor) {
			args.addElement("--editor");
		}
		if (man) {
			args.addElement("--man");
		}
		if (version) {
			args.addElement("--version");
		}
		if (usage) {
			args.addElement("--usage");
		}
		if (memory) {
			args.addElement("--memory");
		}
		if (pack != null) {
			args.addElement(pack);
		}

		try {
			Class c = Class.forName("nice.tools.compiler.package");
			String[] argArray = new String[args.size()];
			System.arraycopy(args.toArray(), 0, argArray, 0, args.size());

			Class[] parameterTypes = new Class[] {argArray.getClass()};

			Method mainMethod = c.getMethod("main", parameterTypes);
			mainMethod.invoke(c.newInstance(), new Object[] {argArray});

		} catch (InstantiationException e) {
			throw new BuildException(e, location);
		} catch (ClassNotFoundException e) {
			throw new BuildException(e, location);
		} catch (NoSuchMethodException e) {
			throw new BuildException(e, location);
		} catch (IllegalAccessException e) {
			throw new BuildException(e, location);
		} catch (InvocationTargetException e) {
			throw new BuildException(e, location);
		}

	}

	/** Only for test usage.
	 */
	public static void main(String[] args) {
		Nicec nicec = new Nicec();
		nicec.setRuntime("../share/java/nice.jar");
		nicec.setPackage("test");
		nicec.execute();
	}

}


