package nice.tools.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
import java.util.Vector;
import bossa.modules.Compilation;

/**
		<h2><a name="java">NiceDoc</a></h2>
		<h3>Description</h3>
		<p>Runs the Nice documentation generator.</p>
		All arguments to the NiceDoc task have to be placed as attributes in the nicedoc xml-element.
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
				<td valign="top">sourcepath</td>
				<td valign="top">Search path for source packages. Is a list of directories and .jar archives.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">destination</td>
				<td valign="top">Destination directory for compiled packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">classpath</td>
				<td valign="top">Search path for compiled packages and libraries.</td>
				<td align="center" valign="top">No</td>
			</tr>
		</table>

		<h4>classpath</h4>
		<p><code>Nicec</code>'s <i>classpath</i> attribute is a PATH like structure and can also be set via a nested
		<i>classpath</i> element. This is very reasonable if you want to make your build script's pathes platform
		independent. </p>
		<h5>Example</h5>
<pre>  
	&lt;nicedoc package=&quot;test&quot; &gt;
	  &lt;classpath&gt;
	    &lt;pathelement location=&quot;\test.jar&quot;/&gt;
	    &lt;pathelement path=&quot;${java.class.path}&quot;/&gt;
	  &lt;/classpath&gt;
	&lt;/nicedoc&gt;
</pre>
		<p>It is possible to use the <i>classpath</i> attribute together with the
		<i>classpath<i> nested tag. In this case the result is a concatenated path.</p>
		<p>It is highly recommended to use the nested version!<p>


		<h3>Examples</h3>
<pre>
	&lt;taskdef name=&quot;nicedoc&quot; classname=&quot;nice.tools.ant.NiceDoc&quot;/&gt;
	&lt;target name=&quot;nice-documentation&quot;&gt;
	  &lt;nicedoc package=&quot;test&quot; /&gt;
	&lt;/target&gt;
</pre>


 * @author Daniel Bonniot
 */

public class NiceDoc extends Task {

	private static final String
		ERROR_MSG = "Generation failed with errors.";
	private static final String
		BUG_MSG = "Generation failed because of a bug.";
	private static final String
		WARNING_MSG = "Generation successful despite warnings.";
	private static final String
		OK_MSG = "Generation successful.";


	/**	Search path for source packages
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

	public void setDestination(File destdir)
	{
		this.destination = destdir;
	}


	/**	Search path for compiled packages and libraries
	 */
	private String classpath = "";

	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
	}

	/**	Location of nice.jar
	 */
	private String runtime = null;

	public void setRuntime(String runtime)
	{
		this.runtime = runtime;
	}

	/**	The root package to document.
	 */
	private String pack;

	public void setPackage(String pack)
	{
		this.pack = pack;
	}



	private Path nestedClasspath = null;

    /**
     * Creates a nested classpath element
     */
    public Path createClasspath() {
    	nestedClasspath = new Path(project);
        return nestedClasspath.createPath();
    }


	/**	Executes nicedoc.
	 */
	public void execute() throws BuildException {

		String oldUserDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", 
												 project.getBaseDir().getAbsolutePath());

			nice.tools.compiler.console.ConsoleOutput console = 
				nice.tools.compiler.console.fun.consoleOutput();
			Compilation compilation = bossa.modules.fun.createCompilation(console);
			if (sourcepath != null)
				compilation.sourcePath = sourcepath;
			compilation.packagePath = classpath + (nestedClasspath != null ? File.pathSeparator+nestedClasspath : "");

			nice.tools.doc.fun.generate(compilation, pack, destination);

			int retval = console.statusCode;

			switch (retval) {
			case nice.tools.compiler.fun.ERROR:
				throw new BuildException(ERROR_MSG, location);
			case nice.tools.compiler.fun.BUG:
				throw new BuildException(BUG_MSG, location);
			case nice.tools.compiler.fun.WARNING:
				log(WARNING_MSG, Project.MSG_WARN);
				break;
			case nice.tools.compiler.fun.OK:
				log(OK_MSG, Project.MSG_INFO);
				break;
			}
		}
		finally {
			System.setProperty("user.dir", oldUserDir);
		}
	}

}


// Setting for Emacs
// Local variables:
// tab-width:2
// indent-tabs-mode:t
// End:
