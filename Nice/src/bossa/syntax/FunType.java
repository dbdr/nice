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
  public FunType(List /* of Monotype */ in, bossa.syntax.Monotype out)
  {
    if (in == null)
      in = emptyList;
    this.in = in;
    this.out = out;
  }

  private static emptyList = new LinkedList();
  
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
      (bossa.syntax.Monotype.substitute(map,in),out.substitute(map));
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
    return out.location().englobe(in);
  }

  public String toString()
  {
    return "fun"+Util.map("(",", ",")",true,in)+"("+out+")";
      //return "("+toStringExtern()+")";
  }
  
  public String toStringExtern()
  {
    return Util.map("(",", ",")",true,in)+"->"+out.toStringExtern();
  }

  public boolean equals(Object o)
  {
    if(!(o instanceof FunType))
      return false;
    FunType that = (FunType) o;
    
    return out.equals(that.out) && in.equals(that.in);
  }
  
  private List in;
  private bossa.syntax.Monotype out;
}
