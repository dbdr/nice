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
// Created : Fri Jun 02 17:12:32 2000 by Daniel Bonniot
//$Modified: Tue Jun 13 17:07:02 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;

/**
 * A monotype variable.
 * 
 * @author Daniel Bonniot
 */

public final class MonotypeVar extends Monotype
  implements mlsub.typing.lowlevel.Element, TypeSymbol
{
  public MonotypeVar()
  {
  }

  public MonotypeVar(String name)
  {
    this.name = name;
  }

  private String name;

  public TypeSymbol cloneTypeSymbol()
  {
    return new MonotypeVar(name);
  }
  
  /**
   * Perform type symbol substitution inside the monotype.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the monotype.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return a monotype with substitution performed
   */
  Monotype substitute(java.util.Map map)
  {
    Object newVar = map.get(this);
    if(newVar==null)
      return this;
    else
      return (Monotype) newVar;
  }
  
  /****************************************************************
   * Fresh monotype variables
   ****************************************************************/
  
  public static MonotypeVar[] news(int n)
  {
    if(n==0)
      return null;
    
    MonotypeVar[] res = new MonotypeVar[n];
    for(int i=0; i<n; i++)
      res[i] = new MonotypeVar();
    return res;
  }
  
  private void functionalCast(int arity)
  {
    if(equivalentCodomain!=null)
      throw new InternalError("equivalentCodomain!=null in MonotypeVar");
    
    equivalentCodomain = new MonotypeVar();
    Typing.introduce(equivalentCodomain);
    
    if(arity==0)
      equivalentDomain = zeroMonotypes;
    else
      equivalentDomain = MonotypeVar.news(arity);
    
    Typing.introduce(equivalentDomain);
  }
  
  /****************************************************************
   * Functional types
   ****************************************************************/

  /** the list of input Monotypes if this type is functional */
  public Monotype[] domain()
  {
    return equivalentDomain;
  }

  /** the return type if this type is functional */
  public Monotype codomain()
  {
    return equivalentCodomain;
  }

  public TypeConstructor getTC()
  {
    return equivalentTC;
  }
  
  public Monotype[] getTP()
  {
    return equivalentTP;
  }
  
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
    if(kind!=null && value!=null
       && kind!=value) //TODO: not sure if this case is OK to happen
      throw new InternalError("Variance already set in MonotypeVar "+
			      this+":\nkind was "+kind+", value is "+value);
    
    kind = value;
    if(value==null)
      {
	// Reset the equivalent* fields so that
	// this variable becomes "free" again
	equivalentTP=null;
	equivalentTC=null;
	equivalentCodomain=null;
	equivalentDomain=null;
	id=-1;
	//willImplementTop=false;
      }
    else
      {
	// Do the apropriate cast
	if(value instanceof FunTypeKind)
	  functionalCast(((FunTypeKind)value).domainArity);
	else if(value instanceof Variance)
	  {
	    Variance v=(Variance) value;
	
	    equivalentTC = new TypeConstructor(v);
	    equivalentTC.name = this.name+"'";
	    
	    Typing.introduce(equivalentTC);
	    equivalentTP = MonotypeVar.news(v.size);
	    Typing.introduce(equivalentTP);
	    if(willImplementTop){
	      try{
		Typing.assertImp
		  (equivalentTC, v.top, false);
	      }
	      catch(TypingEx e){
		throw new InternalError("Variable "+this+" couln't implement top");
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
  private Monotype[] equivalentDomain;
  private Monotype functionalType;

  private TypeConstructor equivalentTC;
  private Monotype[] equivalentTP;

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
  public void rememberToImplementTop()
  {
    willImplementTop=true;
  }

  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    if(name!=null)
      return name;
    else
      return super.toString();
  }

  public String superToString()
  {
    return String.valueOf(name) + " " + super.toString();
  }
}
