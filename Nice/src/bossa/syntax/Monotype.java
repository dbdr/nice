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

/**
   Syntactic monomorphic type.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
abstract public class Monotype
implements Located
{
  /****************************************************************
   * Syntactic fresh monotype variables
   ****************************************************************/
  
  static Monotype fresh(LocatedString associatedVariable)
  {
    return new TypeIdent(associatedVariable);
  }
  
  static List freshs(int arity, LocatedString associatedVariable)
  {
    List res=new ArrayList(arity);
    for(int i=1;i<=arity;i++)
      res.add(fresh(new LocatedString(associatedVariable.content+i,associatedVariable.location())));
    return res;
  }
  
  /** @return true if "alike" appears inside this monotype. */
  abstract boolean containsAlike();
  
  final static boolean containsAlike(List monotypes)
  {
    for(Iterator i = monotypes.iterator(); i.hasNext();)
      if(((Monotype) i.next()).containsAlike())
	return true;
    return false;
  }

  /**************************************************************
   * Scoping
   **************************************************************/
  
  // public since it is called from bossa.dispatch
  public abstract mlsub.typing.Monotype resolve(TypeMap tm);

  /** iterates resolve() on the collection of Monotype */
  static final mlsub.typing.Monotype[] resolve(TypeMap s, Collection c)
  //TODO: imperative version ?
  {
    if(c.size()==0)
      return null;
    
    mlsub.typing.Monotype[] res = new mlsub.typing.Monotype[c.size()];

    int n=0;
    Iterator i=c.iterator();
    while(i.hasNext())
      {
	Object o = i.next();
	if (!(o instanceof Monotype))
	  Debug.println(o+" ::: "+o.getClass());
	
	Monotype old = (Monotype) o;
	mlsub.typing.Monotype nou = old.resolve(s);

	if(nou==null)
	  User.error(old,old+" : Monotype not defined");

	res[n++]=nou;
      }
    return res;
  }
  
  abstract Monotype substitute(Map map);

  static List substitute(Map map, Collection c)
  {
    if(c==null) return null;
    
    List res = new ArrayList(c.size());
    Iterator i = c.iterator();

    while(i.hasNext())
      res.add( ((Monotype)i.next()).substitute(map));

    return res;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  /** called instead of toString if parenthesis are unnecessary */
  public String toStringExtern()
  {
    return toString();
  }

  /** don't print type parameters */
  public String toStringBase()
  {
    return toString();
  }

  /****************************************************************
   * Wrapping a mlsub Monotype in a syntactic monotype
   ****************************************************************/
  
  static Monotype create(mlsub.typing.Monotype m)
  {
    return new Wrapper(m);
  }
  
  private static final class Wrapper extends Monotype
  {
    Wrapper(mlsub.typing.Monotype m)
    {
      this.type = m;
    }

    boolean containsAlike() { return false; }
    
    public mlsub.typing.Monotype resolve(TypeMap s)
    {
      return type;
    }

    Monotype substitute(Map m)
    {
      return this;
    }

    public Location location()
    {
      return Location.nowhere();
    }

    public String toString() 
    {
      return String.valueOf(type);
    }
    
    private final mlsub.typing.Monotype type;
  }  
}

