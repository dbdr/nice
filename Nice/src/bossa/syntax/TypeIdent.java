/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TypeIdent.java
// Created : Sat Jul 24 14:02:08 1999 by bonniot
//$Modified: Wed Oct 04 15:00:15 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Interface;
import mlsub.typing.MonotypeVar;
import mlsub.typing.TypeSymbol;

/**
 * A syntactic type identifier.
 * 
 * After scoping, it will either reveal to be a 
 * TypeConstructor or a MonotypeVar.
 *
 * @author bonniot
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
  
  Monotype substitute(Map map)
  {
    Monotype res = (Monotype) map.get(this);
    if(res!=null)
      return res;
    else
      return this;
  }

  public final TypeSymbol resolveToTypeSymbol(TypeScope scope)
  {
    TypeSymbol res = scope.lookup(name.toString());

    if (res == null)
      User.error(this, name + " is not declared");
    
    return res;
  }
  
  public mlsub.typing.Monotype resolve(TypeScope scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);
    if (res == null)
      User.error(this, name + " is not declared");
    
    if (res instanceof MonotypeVar)
      return (MonotypeVar) res;

    if (res instanceof TypeConstructor)
      {
	TypeConstructor tc = (TypeConstructor) res;
	try{
	  return new mlsub.typing.MonotypeConstructor(tc, null);
	}
	catch(mlsub.typing.BadSizeEx e){
	  User.error(this, name + " has " + 
		     e.expected + 
		     " type parameter" + (e.expected>1 ? "s" : ""));
	}
      }
    
    Internal.error("Invalid type ident");
    return null;
  }
  
  public mlsub.typing.TypeConstructor resolveToTC(TypeScope scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);

    if (res instanceof TypeConstructor)
      return (TypeConstructor) res;
    
    User.error(this, this+" should be a class");
    return null;
  }
  
  public mlsub.typing.Interface resolveToItf(TypeScope scope)
  {
    TypeSymbol res = resolveToTypeSymbol(scope);

    if (res instanceof Interface)
      return (Interface) res;

    if (res instanceof TypeConstructor)
      {
	ClassDefinition def = ClassDefinition.get((TypeConstructor) res);
	if (def != null && def.getAssociatedInterface()!=null)
	  return def.getAssociatedInterface();
      }
    
    User.error(this, res + " should be an interface");
    return null;
  }
  
  public static TypeConstructor[] resolveToTC(TypeScope scope, List idents)
  {
    if (idents==null || idents.size()==0) return null;
    
    TypeConstructor[] res = new TypeConstructor[idents.size()];

    int n = 0;
    for(Iterator i = idents.iterator(); i.hasNext();)
      res[n++] = ((TypeIdent) i.next()).resolveToTC(scope);    

    return res;
  }
  
  public static Interface[] resolveToItf(TypeScope scope, List idents)
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
    return name.toString();
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
