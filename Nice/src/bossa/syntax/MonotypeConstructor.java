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
import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;

/**
   A monotype, build by application of
   a type constructor to type parameters.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class MonotypeConstructor extends Monotype
{
  /**
   * Constructs a monotype by application of the type constructor
   * to the type parameters
   *
   * @param tc the type constructor
   * @param parameters the type parameters
   */
  public MonotypeConstructor(TypeIdent tc, TypeParameters parameters,
			     Location loc)
  {
    this.tc = tc;
    if(parameters==null)
      this.parameters = new TypeParameters(null);
    else
      this.parameters = parameters;
    this.loc = loc;
  }

  MonotypeConstructor(TypeConstructor tc, TypeParameters parameters,
		      Location loc)
  {
    this.lowlevelTC = tc;
    this.parameters = parameters;
    this.loc = loc;
  }
  
  Monotype cloneType()
  {
    return new MonotypeConstructor(tc,parameters,loc);
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  public mlsub.typing.Monotype resolve(TypeScope typeScope) 
  {
    if(lowlevelTC==null)
      {
	TypeSymbol newTC = this.tc.resolveToTC(typeScope);
	if (!(newTC instanceof TypeConstructor))
	  User.error(this.tc, this.tc+" should be a type constructor");
	lowlevelTC = (TypeConstructor) newTC;
      }
    
    try{
      return new mlsub.typing.MonotypeConstructor
	(lowlevelTC, parameters.resolve(typeScope));
    }
    catch(mlsub.typing.BadSizeEx e){
      int arity = e.expected;
      
      User.error(this,
		 "Class "+tc+" has "+
		 (arity==0 ? "no" : ""+arity)+
		 " type parameter"+(arity>1 ? "s" : ""));

      return null;
    }
  }

  Monotype substitute(Map map)
  {
    TypeIdent newTC = (TypeIdent) map.get(tc);
    if (newTC==null)
      newTC = tc;
    
    return new MonotypeConstructor
      (newTC,
       new TypeParameters(Monotype.substitute(map,parameters.content)),
       loc);
  }

  boolean containsAlike()
  {
    return Monotype.containsAlike(parameters.content);
  }
  
  public TypeIdent getTC()
  {
    return tc;
  }
  
  public TypeParameters getTP()
  {
    return parameters;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    if(loc==null)
      return tc.location();
    else
      return loc;
  }

  public boolean equals(Object o)
  {
    if(!(o instanceof MonotypeConstructor))
      return false;
    MonotypeConstructor that = (MonotypeConstructor) o;
    
    return tc.equals(that.tc) && parameters.equals(that.parameters);
  }
  
  public String toString()
  {
    return ""+tc+parameters;
  }

  public TypeIdent tc;

  // used when the lowlevel TC is know in advance
  private mlsub.typing.TypeConstructor lowlevelTC;

  TypeParameters parameters;
  Location loc;
}
