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

import bossa.util.*;
import java.util.*;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Interface;
import mlsub.typing.MonotypeVar;
import mlsub.typing.TypeSymbol;

/**
   A syntactic type identifier.
   
   After scoping, it will either reveal to be a 
   TypeConstructor or a MonotypeVar.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public final class TypeIdent extends Monotype implements Located
{
  public TypeIdent(LocatedString name)
  {
    this.name = name;
  }

  public TypeIdent cloneTypeIdent()
  {
    return new TypeIdent(name);
  }
  
  /****************************************************************
   * 
   ****************************************************************/

  boolean containsAlike()
  {
    return false;
  }
  
  public boolean isVoid()
  {
    return "void".equals(getName().toString());
  }

  Monotype substitute(Map map)
  {
    Monotype res = (Monotype) map.get(name.toString());
    if(res!=null)
      return res;
    else
      return this;
  }

  public final TypeSymbol resolveToTypeSymbol(TypeMap scope)
  {
    TypeSymbol res = scope.lookup(name);

    if (res == null)
      throw dispatch.unknownIdent(name);
    
    return res;
  }
  
  mlsub.typing.Monotype rawResolve(TypeMap scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);
    
    if (res instanceof mlsub.typing.Monotype)
      return (mlsub.typing.Monotype) res;

    if (res instanceof TypeConstructor)
      {
	TypeConstructor tc = (TypeConstructor) res;

	try{
	  return new mlsub.typing.MonotypeConstructor(tc, null);
	}
	catch(mlsub.typing.BadSizeEx e){
	  throw User.error(this, name + 
			   Util.has(e.expected, "type parameter", e.actual));
	}
      }
    
    if (res instanceof mlsub.typing.Interface)
      User.error(this, "This abstract interface cannot be used as a type");

    Internal.error("Invalid type ident: " + res.getClass() + " = " + res);
    return null;
  }
  
  public mlsub.typing.TypeConstructor resolveToTC(TypeMap scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);

    if (res instanceof TypeConstructor)
      return (TypeConstructor) res;
    
    throw User.error(this, this + " is not a class");
  }

  public TypeSymbol resolvePreferablyToItf(TypeMap scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);

    if (res instanceof Interface)
      return (Interface) res;

    if (res instanceof TypeConstructor)
      {
	ClassDefinition def = ClassDefinition.get((TypeConstructor) res);
	if (def != null)
	  {
	    mlsub.typing.Interface itf = def.getAssociatedInterface();
	    if (itf != null)
	      return itf;
	  }
      }

    return res;
  }
  
  public mlsub.typing.Interface resolveToItf(TypeMap scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);

    if (res instanceof Interface)
      return (Interface) res;

    if (res instanceof TypeConstructor)
      {
	ClassDefinition def = ClassDefinition.get((TypeConstructor) res);
	if (def != null)
	  {
	    mlsub.typing.Interface itf = def.getAssociatedInterface();
	    if (itf != null)
	      return itf;
	  }
      }

    throw User.error(this, res + " should be an interface");
  }

  public static Interface[] resolveToItf(TypeMap scope, List idents)
  {
    if (idents==null || idents.size()==0) return null;
    
    Interface[] res = new Interface[idents.size()];

    int n = 0;
    for(Iterator i = idents.iterator(); i.hasNext();)
      res[n++] = ((TypeIdent) i.next()).resolveToItf(scope);    

    return res;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return nullnessString() + name.toString();
  }

  public LocatedString getName()
  {
    return name;
  }
  
  public boolean hasName(LocatedString name)
  {
    return this.name.equals(name);
  }
  
  public Location location()
  {
    return name.location();
  }

  public LocatedString name;
}
