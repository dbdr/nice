/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   The global scope of symbols.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class GlobalVarScope extends VarScope
{
  GlobalVarScope()
  {
    super(null);
  }

  /**
   * The lookup method to call when you need to get a VarSymbol
   * from its name
   *
   * @param i the identifier to lookup
   * @return the symbols if some were found, null otherwise
   */
  public java.util.List lookup(LocatedString i)
  {
    // If there is any method by that name waiting, load it.
    JavaClasses.nameRequired(i.toString());

    return super.lookup(i);
  }
}
