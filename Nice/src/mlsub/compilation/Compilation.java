/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.compilation;

/**
 * Stores information about a mlsub compilation.
 *
 * Information includes:
 *   - whether to perform link tests
 *   - ...
 * 
 * Only information needed to perform the steps
 * of compilation should go here.
 * Other language specific options should go in a 
 * subclass.
 */
public class Compilation
{
  public boolean skipLink;
  public Module root;
}

