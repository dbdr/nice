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

import mlsub.typing.MonotypeVar;
import mlsub.typing.TypeConstructor;
import nice.tools.typing.PrimitiveType;

/**
   Syntactic monomorphic type.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public abstract class Monotype
implements Located
{
  static final Monotype[] toArray(List monotypes)
  {
    if (monotypes == null)
      return array0;

    return (Monotype[]) monotypes.toArray(new Monotype[monotypes.size()]);
  }
  
  static final Monotype[] array0 = new Monotype[0];

  /****************************************************************
   * Syntactic fresh monotype variables
   ****************************************************************/
  
  static Monotype fresh(LocatedString associatedVariable)
  {
    return new TypeIdent(associatedVariable);
  }
  
  static Monotype[] freshs(int arity, LocatedString associatedVariable)
  {
    Monotype[] res = new Monotype[arity];
    for(int i = 0; i < arity; i++)
      res[i] = fresh(new LocatedString(associatedVariable.content + i,
				       associatedVariable.location()));
    return res;
  }
  
  /** @return true if "alike" appears inside this monotype. */
  abstract boolean containsAlike();
  
  static final boolean containsAlike(List monotypes)
  {
    for(Iterator i = monotypes.iterator(); i.hasNext();)
      if(((Monotype) i.next()).containsAlike())
	return true;
 
   return false;
  }

  static final boolean containsAlike(Monotype[] monotypes)
  {
    for(int i = monotypes.length; --i >= 0;)
      if (monotypes[i].containsAlike())
	return true;

    return false;
  }

  public boolean isVoid() { return false; }

  /**************************************************************
   * Scoping
   **************************************************************/

  /** Set by the parser. */
  public byte nullness;

  public static final byte
    none = 0,
    maybe = 1,
    sure = 2,
    absent = 3;

  final String nullnessString()
  {
    switch(nullness) 
      {
      case maybe: return "?";
      case sure:  return "!";
      default:    return "";
      }
  }

  // public since it is called from bossa.dispatch
  public final mlsub.typing.Monotype resolve(TypeMap tm)
  {
    mlsub.typing.Monotype raw = rawResolve(tm);
    
    switch (nullness) 
      {
      case none:  return raw;
      case maybe: return sourceMaybe(raw);
      case sure:  return sourceSure(raw);
      case absent:
	if (raw instanceof MonotypeVar)
	  {
	    nice.tools.typing.Types.makeMarkedType((MonotypeVar) raw);
	    return raw;
	  }
	else
	  return sure(raw);
      default: 
	throw Internal.error("Bad nullness tag");
      }
  }

  abstract mlsub.typing.Monotype rawResolve(TypeMap tm);

  /** iterates resolve() on the collection of Monotype */
  static final mlsub.typing.Monotype[] rawResolve(TypeMap s, Collection c)
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
  
  /** iterates resolve() on the collection of Monotype */
  static final mlsub.typing.Monotype[] resolve(TypeMap s, Monotype[] c)
  {
    if(c == null || c.length == 0)
      return null;
    
    mlsub.typing.Monotype[] res = new mlsub.typing.Monotype[c.length];

    for (int n = c.length; --n >= 0;)
      {
	Monotype old = c[n];
	mlsub.typing.Monotype nou = old.resolve(s);

	if (nou == null)
	  User.error(old, old + " : Monotype not defined");

	res[n] = nou;
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

  static Monotype[] substitute(Map map, Monotype[] m)
  {
    Monotype[] res = new Monotype[m.length];
    for(int i = m.length; --i >= 0;)
      res[i] = m[i].substitute(map);

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
  
  public static Monotype create(mlsub.typing.Monotype m)
  {
    return new Wrapper(m);
  }
  
  private static class Wrapper extends Monotype
  {
    Wrapper(mlsub.typing.Monotype m)
    {
      this.type = m;
    }

    boolean containsAlike() { return false; }
    
    public mlsub.typing.Monotype rawResolve(TypeMap s)
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
    
    final mlsub.typing.Monotype type;
  }

  public static Monotype createVar(mlsub.typing.MonotypeVar m)
  {
    return new VarWrapper(m);
  }
  
  private static final class VarWrapper extends Wrapper
  {
    VarWrapper(mlsub.typing.MonotypeVar m)
    {
      super(m);
    }

    public mlsub.typing.Monotype rawResolve(TypeMap s)
    {
      mlsub.typing.TypeSymbol res = s.lookup(type.toString());
      if (res != null)
	return (mlsub.typing.Monotype) res;

      return type;
    }
  }

  /****************************************************************
   * Wrapper for delaying resolution of constructed monotypes.
   ****************************************************************/

  static Monotype createSure(final TypeConstructor tc, final mlsub.typing.Monotype[] params)
  {
    return new Monotype() {
        boolean containsAlike() { return false; }

        public mlsub.typing.Monotype rawResolve(TypeMap typeMap)
        {
          try{
            return sure(new mlsub.typing.MonotypeConstructor(tc, params));
          }
          catch(mlsub.typing.BadSizeEx e){
            // See if this is a class with default type parameters
            mlsub.typing.Monotype res = dispatch.getTypeWithTC(tc, params);
            if (res != null)
              return sure(res);

            throw User.error(this, "Class " + tc +
                             Util.has(e.expected, "type parameter", e.actual));
          }
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
          return tc + ( params != null ? Util.map("<",",",">",params) : "");
        }
      };
  }

  /****************************************************************
   * Nullness markers
   ****************************************************************/

  /**
     Return a maybe type based on the raw type of the argument
     if the argument is a full type.
  */
  static mlsub.typing.Monotype sourceMaybe(mlsub.typing.Monotype type)
  {
    mlsub.typing.Monotype raw = nice.tools.typing.Types.rawType(type);
    return maybe(raw);
  }

  public static mlsub.typing.Monotype maybe(mlsub.typing.Monotype type)
  {
    return new mlsub.typing.MonotypeConstructor
      (PrimitiveType.maybeTC, new mlsub.typing.Monotype[]{type});
  }

  /**
     Return a sure type based on the raw type of the argument
     if the argument is a full type.
  */
  static mlsub.typing.Monotype sourceSure(mlsub.typing.Monotype type)
  {
    mlsub.typing.Monotype raw = nice.tools.typing.Types.rawType(type);
    return sure(raw);
  }

  public static mlsub.typing.Monotype sure(mlsub.typing.Monotype type)
  {
    return new mlsub.typing.MonotypeConstructor
      (PrimitiveType.sureTC, new mlsub.typing.Monotype[]{type});
  }
}

