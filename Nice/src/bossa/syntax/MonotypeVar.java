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

// File    : MonotypeVar.java
// Created : Fri Jul 23 15:36:39 1999 by bonniot
//$Modified: Wed Sep 08 16:59:49 1999 by bonniot $

package bossa.syntax;

import java.util.*;

import bossa.util.*;
import bossa.typing.*;
import bossa.engine.*;

/**
 * A monotype variable.
 * 
 * @author bonniot
 */

public class MonotypeVar extends Monotype
  implements TypeSymbol, bossa.engine.Element
{
  public MonotypeVar(LocatedString name, boolean imperative)
  {
    this.name=name;
    this.soft=false;
    this.imperative=imperative;
  }

  public MonotypeVar(boolean soft, LocatedString name)
  {
    this.name=name;
    this.soft=soft;
    this.imperative=false;
  }

  public TypeSymbol cloneTypeSymbol()
  {
    MonotypeVar res=new MonotypeVar(name,imperative);
    res.soft=soft;
    res.kind=null;
    res.id=-1;
    return res;
  }
  
  Monotype cloneType()
  {
    return this;
  }

  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * Imperative type variables
   ****************************************************************/

  public boolean appear(TypeSymbol s, boolean imperatively)
  {
    if(imperatively)
      return false;
    else
      return name.equals(s.getName());
  }
  
  public boolean isImperative()
  {
    return imperative;
  }
  
  /****************************************************************
   * Functional Types
   ****************************************************************/

  public Monotype codomain()
  {
    return equivalentCodomain;
  }
  
  public Collection domain()
  {
    return equivalentDomain;
  }
  
  /****************************************************************
   * Kinding
   ****************************************************************/

  private void functionalCast(int arity)
  {
    Internal.error(equivalentCodomain!=null,"equivalentCodomain!=null in MonotypeVar");
    
    equivalentCodomain=Monotype.fresh(new LocatedString(name.content+"0",name.location()));
    Typing.introduce(equivalentCodomain);
    
    equivalentDomain=Monotype.freshs(arity,name);
    Typing.introduce(equivalentDomain);
  }
  
  public TypeConstructor getTC()
  {
    return equivalentTC;
  }
  
  public TypeParameters getTP()
  {
    return equivalentTP;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  Monotype resolve(TypeScope ts)
  {
    if(soft)
      return this;
    
    TypeSymbol s=ts.lookup(this.name);
    User.error(s==null,this,this.name+" is not defined"," in "+ts);

    if(s instanceof Monotype)
      return (Monotype) s;

    // In this case, it was indeed a type constructor
    // applied to no type parameters
    if(s instanceof TypeConstructor)
      return new MonotypeConstructor((TypeConstructor) s, null, 
				     name.location());

    Internal.error(this,this.name+" is not well kinded :"+s.getClass());
    return null;
  }

  Monotype substitute(java.util.Map map)
  {
    if(map.containsKey(this))
      return (Monotype) map.get(this);
    return this;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }

  public String toString()
  {
    if(soft)
      return name.toString();
    else
      return "\""+name+"("+id+")\"";
  }

  public LocatedString getName()
  {
    return name;
  }

  LocatedString name;

  /****************************************************************
   * low-level interface
   ****************************************************************/

  private int id=-1;
  
  public int getId() 		{ return id; }
  
  public void setId(int value) 	{ id=value; }
  
  Kind kind;
  
  public Kind getKind() 	{ return kind; }
  
  public void setKind(Kind value)
  {
    // it is ok to reset kind to null
    Internal.error(kind!=null && value!=null 
		   && kind!=value //TODO: not sure if this case is OK to appen
		   ,"Variance already set in MonotypeVar :\n"+
		   this+": kind was "+kind+", value is "+value);
    
    kind=value;
    if(value==null)
      {
	// Reset the equivalent* fields so that
	// this variable becomes "free" again
	equivalentTP=null;
	equivalentTC=null;
	equivalentCodomain=null;
	equivalentDomain=null;
	id=-1;
	willImplementTop=false;
      }
    else
      {
	// Do the apropriate cast
	if(value instanceof FunTypeKind)
	  functionalCast(((FunTypeKind)value).domainArity);
	else if(value instanceof Variance)
	  {
	    Variance v=(Variance) value;
	
	    equivalentTC=new TypeConstructor(this,v);
	    Typing.introduce(equivalentTC);
	    equivalentTP=new TypeParameters(this.name,v);
	    Typing.introduce(equivalentTP.content);
	    if(willImplementTop){
	      try{
		Typing.assertImp
		  (equivalentTC,InterfaceDefinition.top(v.size));
	      }
	      catch(TypingEx e){
		Internal.error(this,
			       "This variable couln't implement top");
	      }
	      finally{
		willImplementTop=false;
	      }
	    }
	  }
	// in other cases (kind of the rigid monotype variables)
	// nothing to do
      }
  }
  
  /** When this variable is comparable to a functional type */
  private MonotypeVar equivalentCodomain;
  private Collection equivalentDomain;
  private Monotype functionalType;

  private TypeConstructor equivalentTC;
  private TypeParameters equivalentTP;

  /**
   * If true,  this is really a variable monotype
   * If false, this must be found in scope
   */
  private boolean soft;

  private boolean imperative;

  /****************************************************************
   * The Top interface
   ****************************************************************/

  /**
   * Marks that as soon as this variable is deconstructed
   * (so its variance is known)
   * we will have to assert that the corresponding type constructor
   * implements the correct Top<n> interface
   */
  private boolean willImplementTop=false;

  /**
   * Marks that as soon as this variable is deconstructed
   * (so its variance is known)
   * we will have to assert that the corresponding type constructor
   * implements the correct Top<n> interface
   */
  void rememberToImplementTop()
  {
    willImplementTop=true;
  }
}
