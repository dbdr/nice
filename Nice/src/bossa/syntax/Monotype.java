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
  
  /** @return true if "alike" appears inside this monotype. */
  abstract boolean containsAlike();
  
  public boolean isVoid() { return false; }

  /**************************************************************
   * Scoping
   **************************************************************/

  /** Set by the parser. */
  public byte nullness;
/*
  public static final byte
    none = 0,
    maybe = 1,
    sure = 2,
    absent = 3;
*/
  final String nullnessString()
  {
    switch(nullness) 
      {
      case /*maybe*/1: return "?";
      case /*sure*/2:  return "!";
      default:    return "";
      }
  }

  // public since it is called from bossa.dispatch
  public final mlsub.typing.Monotype resolve(TypeMap tm)
  {
    mlsub.typing.Monotype raw = rawResolve(tm);
    
    switch (nullness) 
      {
      case /*none*/0:  return raw;
      case /*maybe*/1: return sourceMaybe(raw);
      case /*sure*/2:  return sourceSure(raw);
      case /*absent*/3:
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

  //temporarily method to call on TypeIdent's
  public mlsub.typing.TypeSymbol resolveToTypeSymbol(TypeMap scope)
  {
    return null;
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

