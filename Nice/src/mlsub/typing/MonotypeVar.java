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
//$Modified: Thu Aug 31 15:32:56 2000 by Daniel Bonniot $

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

  private MonotypeVar(String name, TypeConstructor persistentHeadLeq)
  {
    this.name = name;
    this.persistentHeadLeq = persistentHeadLeq;
  }

  private String name;
  public String getName() { return name; }

  public TypeSymbol cloneTypeSymbol()
  {
    return new MonotypeVar(name, persistentHeadLeq);
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
    if (newVar == null)
      return this;

    return (Monotype) newVar;
  }
  
  /****************************************************************
   * Fresh monotype variables
   ****************************************************************/
  
  public static MonotypeVar[] news(int n)
  {
    if (n == 0) return null;
    
    MonotypeVar[] res = new MonotypeVar[n];
    for(int i=0; i<n; i++)
      res[i] = new MonotypeVar();
    return res;
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
    if(kind!=null 
       && value!=null
       && kind!=value) //TODO: not sure if this case is OK to happen
      throw new InternalError("Variance already set in MonotypeVar "+
			      this+":\nkind was "+kind+", value is "+value);
    
    if (kind == value)
      return;
    
    kind = value;
    if(value == null)
      {
	// Reset the equivalent field so that
	// this variable becomes "free" again
	equivalent = null;
	id=-1;
      }
    else
      {
	// Do the apropriate cast
	equivalent = value.freshMonotype();

	// equivalent is null if the kind is that of unconstrained variables
	if (equivalent != null)
	  {
	    // set the name for debug or display purposes
	    TypeConstructor tc = equivalent.head();
	    if (tc != null)
	      tc.name = this + "'";
	  }
      }
  }
  
  public void setPersistentHeadLeq(TypeConstructor h)
  {
    persistentHeadLeq = h;
  }

  TypeConstructor persistentHeadLeq;

  void reset()
  {
    if (persistentHeadLeq == null)
      setKind(null);
    else
      {
	// Force update of the equivalent.
	kind = null;
	setKind(persistentHeadLeq.variance);
	try {
	  Typing.leq(((MonotypeConstructor) equivalent).getTC(), persistentHeadLeq);
	} catch(TypingEx ex) {
	  bossa.util.Internal.error("Reset of type variable failed");
	}
      }
  }

  /** When this variable is discovered to be of some given kind,
      an equivalent monotype is created. */
  private Monotype equivalent;
  // overrides Monotype.equivalent()
  public Monotype equivalent()
  {
    if (equivalent != null)
      return equivalent;
    else
      return this;
  }
  
  /**
     Return the head type constructor if this monotype is
     of a known variance, or null.
  */
  public TypeConstructor head()
  {
    if (equivalent != null)
      return equivalent.head();
    else
      return null;
  }

  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    if(name != null)
      return name;
    else
      return super.toString();
  }

  public String superToString()
  {
    return String.valueOf(name) + " " + super.toString();
  }

  /****************************************************************
   * Simplification
   ****************************************************************/

  void tag(int variance)
  {
    if (equivalent != null)
      equivalent.tag(variance);
    else
      mlsub.typing.lowlevel.Engine.tag(this, variance);
  }

  Monotype canonify()
  {
    if (equivalent != null)
      return equivalent.canonify();
    else
      return (MonotypeVar) mlsub.typing.lowlevel.Engine.canonify(this);
  }
}
