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

// File    : TypeConstructor.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Thu Aug 31 16:22:28 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;
import mlsub.typing.lowlevel.*;

/**
 * A class. It "needs" type parameters to become a Monotype
 */
public class TypeConstructor
  implements mlsub.typing.lowlevel.Element, TypeSymbol
{
  /**
   * Creates a TypeConstructor.
   *
   * A concrete TC is a TC that can tag runtime objects.
   */
  public TypeConstructor(String name, AtomicKind v, boolean concrete, 
			 boolean rigid)
  {
    this.name = name;
    this.concrete = concrete;
    this.rigid = rigid;
    
    if (v != null)
      setVariance(v);
  }
  
  /**
     Creates a non rigid type constructor.
  */
  public TypeConstructor(String name)
  {
    this(name, null, false, false);
  }
  
  /**
     Creates an anonymous non-concrete TypeConstructor with a known variance.
   */
  public TypeConstructor(AtomicKind v)
  {
    this(null, v, false, false);
  }
  
  public void setMinimal()
  {
    variance.getConstraint().assertMinimal(this);
  }

  public boolean isMinimal() 
  { 
    return variance.getConstraint().isMinimal(this);
  }

  public TypeSymbol cloneTypeSymbol()
  {
    return new TypeConstructor(name, variance, concrete, rigid);
  }

  /**
     @return s if it is a type constructor, null otherwise
  */
  public static TypeConstructor fromTypeSymbol(TypeSymbol s)
  {
    if (s instanceof TypeConstructor)
      return (TypeConstructor) s;
    else
      return null;
  }
  
  /**
   * Tell which variance this TypeConstructor has.
   * Can be called from ImplementsCst.
   */
  public void setVariance(AtomicKind v)
  {
    setKind(v.getConstraint());
  }

  public int arity()
  {
    if(variance==null)
      throw new InternalError("Variance of "+this+" not known in arity()");
    
    return variance.arity();
  }

  public boolean isConcrete()
  {
    return concrete;
  }
  
  public boolean isExistential()
  {
    // Only MonotypeVars need to be marked as existential.
    return false;
  }
  
  /****************************************************************
   * Kinding
   ****************************************************************/

  private int id=-1;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }
  
  private Kind kind;
  
  public Kind getKind() 	{ return kind; }
  
  public void setKind(Kind value)
  {
    if(kind != null)
      if(kind == value)
	return;
      else
	throw new InternalError
	  ("Variance already set in type constructor " + this);

    variance = (AtomicKind) ((Engine.Constraint) value).associatedKind;
    kind = value;
  }
  
  /****************************************************************
   * Misc
   ****************************************************************/

  public String toString()
  {
    if (name != null)
      return name;
    else
      return super.toString();
  }
  
  /**
     Create a string representing the monotype build by application
     of this type constructor to the given parameters.

     This default implementation returns "tc<p1, ..., pn>".

     It should be overriden by type constructors that print differently.
     For instance, the array tape constructor could return "p1[]".

     @param parameters the type parameters
     @return the representation of the monotype
  */
  public String toString(Monotype[] parameters)
  {
    return toString(parameters, false, null);
  }

  /**
     Print the monotype when it can be null.
  */
  public String toString(Monotype[] parameters, boolean isNull, String suffix)
  {
    String res = this.toString() + 
      bossa.util.Util.map("<", ", ", ">", parameters);
    if (isNull)
      res = "?" + res;
    if (suffix != null)
      res = res + suffix;
    return res;
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  public AtomicKind variance;
  private boolean concrete;
  private boolean rigid;
  public final boolean isRigid() { return rigid; };
  
  String name;
}
