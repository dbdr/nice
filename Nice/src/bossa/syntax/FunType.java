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

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Monotype;
import mlsub.typing.FunTypeKind;

/**
   Functional type.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class FunType extends bossa.syntax.Monotype
{
  public FunType(List /* of bossa.syntax.Monotype */ in, 
		 bossa.syntax.Monotype out)
  {
    this(bossa.syntax.Monotype.toArray(in), out);
  }

  public FunType(bossa.syntax.Monotype[] in, bossa.syntax.Monotype out)
  {
    this.in = in;
    this.out = out;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  public Monotype resolve(TypeMap typeScope)
  {
    return new mlsub.typing.FunType
      (bossa.syntax.Monotype.resolve(typeScope, in),
       out.resolve(typeScope));
  }

  bossa.syntax.Monotype substitute(Map map)
  {
    return new bossa.syntax.FunType
      (bossa.syntax.Monotype.substitute(map,in), out.substitute(map));
  }

  boolean containsAlike()
  {
    return bossa.syntax.Monotype.containsAlike(in) || out.containsAlike();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return out.location();
  }

  public String toString()
  {
    return "fun(" + Util.map("", ", ", "", in) + ")(" + out + ")";
    //return "("+toStringExtern()+")";
  }
  
  public String toStringExtern()
  {
    return "(" + Util.map("", ", ", "", in) + ")->" + out.toStringExtern();
  }

  private bossa.syntax.Monotype[] in;
  private bossa.syntax.Monotype out;
}
