/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;
import mlsub.typing.lowlevel.Element;

/**
   An unknown monotype.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class UnknownMonotype extends Monotype implements TypeSymbol
{
  private UnknownMonotype() {}

  public static final UnknownMonotype instance = new UnknownMonotype();

  public TypeSymbol cloneTypeSymbol() { return this; }

  boolean isUnknown() { return true; }

  Monotype canonify() { return this; }

  Monotype substitute(java.util.Map map) { return this; }

  void tag(int variance) {}

  public String toString() { return "unknown"; }

  /****************************************************************
   * low-level interface
   ****************************************************************/

  public int getId() 		{ throw new Error(); }
  public void setId(int value) 	{ throw new Error(); }

  public Kind getKind() 	  { return NullnessKind.instance; }
  public void setKind(Kind value) { throw new Error(); }
}
