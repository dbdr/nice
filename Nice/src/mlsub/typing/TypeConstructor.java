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
//$Modified: Tue Jun 13 15:56:27 2000 by Daniel Bonniot $

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
  public TypeConstructor(String name, Variance v, boolean concrete)
  {
    this.name = name;
    this.concrete = concrete;
    if (v!=null)
      setVariance(v);
  }
  
  /**
   * Creates a non-concrete TypeConstructor with a known variance.
   *
   * Equivalent to TypeConstructor(v, false)
   */
  public TypeConstructor(Variance v)
  {
    if (v!=null)
      setVariance(v);
  }
  

  public TypeSymbol cloneTypeSymbol()
  {
    return new TypeConstructor(name, variance, concrete);
  }
  
  /**
   * Tell which variance this TypeConstructor has.
   * Can be called from ImplementsCst.
   */
  protected void setVariance(Variance v)
  {
    setKind(v.getConstraint());
    
//    if(variance!=null)
//        {
//  	if(!(variance.equals(v)))
//  	  User.error(this,"Incorrect variance for "+this);
//  	return;
//        }
//      this.variance=v;
//      this.kind=Engine.getConstraint(v);
  }

  public int arity()
  {
    if(variance==null)
      throw new InternalError("Variance of "+this+" not known in arity()");
    
    return variance.size;
  }

  public boolean isConcrete()
  {
    return concrete;
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
    if(kind!=null)
      if(kind==value)
	return;
      else
	throw new InternalError("Variance already set in type constructor "+this);

    variance=(Variance)((Engine.Constraint)value).associatedKind;
    kind=value;

    if(willImplementTop)
      try{
	//bossa.typing.initialLeq(this, object(this.variance.size));
	
	Typing.assertImp
	  (this,
	   this.variance.top,
	   true);
      }
      catch(TypingEx e){
	throw new InternalError("Impossible");
      }
  }
  
  /****************************************************************
   * The Top interface
   ****************************************************************/

  /**
   * Marks that as soon as this variable is deconstructed
   * (so its variance is known)
   * we will have to assert that the corresponding type constructor
   * implements the correct Top<n> interface
   */
  //used for java classes for the moment
  //as we don't know their arity in time
  private boolean willImplementTop=false;

  /**
   * Marks that as soon as this variable is deconstructed
   * (so its variance is known)
   * we will have to assert that the corresponding type constructor
   * implements the correct Top<n> interface
   */
  public void rememberToImplementTop()
  {
    willImplementTop=true;
  }

  /****************************************************************
   * Misc
   ****************************************************************/

  public String toString()
  {
    return name;
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  public int enumerateTagIndex=-1; // used in Typing.enumerate. ugly ! Subclass

  public Variance variance;
  private boolean concrete;
  String name;
}
