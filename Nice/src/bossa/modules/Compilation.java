
package bossa.modules;

/**
 * Stores information about a bossa compilation.
 *
 * @author Daniel Bonniot
 */

public class Compilation extends mlsub.compilation.Compilation
{
  public boolean recompileAll;
  public boolean recompileCommandLine;

  /** Set if at least one package is not up-to-date. */
  public boolean recompilationNeeded;

  public String sourcePath;
  public String packagePath;
  public String destinationDir;

  public String output;

  public Locator locator;
}

