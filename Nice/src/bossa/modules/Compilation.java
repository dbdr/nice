
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
  public boolean staticLink;

  /** Set if at least one package is not up-to-date. */
  public boolean recompilationNeeded;
}

