
package bossa.modules;

/**
 * Stores information about a bossa compilation.
 *
 * This java source is only used to break a dependency cycle when 
 * bootstrapping. The master source is in Nice files.
 *
 * @author Daniel Bonniot
 */

import bossa.util.Location;

public abstract class Compilation extends CompilationInterface
{
  public boolean recompileAll;
  public boolean recompileCommandLine;

  /** Set if at least one package is not up-to-date. */
  public boolean recompilationNeeded;

  /** Location of the nice.jar file. */
  public String runtimeFile;

  public String sourcePath;
  public String packagePath;
  public String destinationDir;

  public String output;
  public boolean excludeRuntime;
  public Parser parser;

  public Locator locator;

  public java.util.Map packages;
  public java.util.Map javaTypeConstructors;

  nice.tools.visibility.Scope javaScope;
  public bossa.syntax.TypeScope globalTypeScope;
}

