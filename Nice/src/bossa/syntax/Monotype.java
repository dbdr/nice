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

*/
public abstract class Monotype implements Located
{
  /** Set by the parser. */
  public byte nullness;
/*
  public static final byte
    none = 0,
    maybe = 1,
    sure = 2,
    absent = 3;
*/

  /** @return true if "alike" appears inside this monotype. */
  abstract boolean containsAlike();

  abstract mlsub.typing.Monotype rawResolve(TypeMap tm);

  abstract Monotype substitute(Map/*<String,Monotype>*/ map);
  
  public boolean isVoid() { return false; }

  String nullnessString()
  {
    if (nullness == /*maybe*/1)
      return "?";
    else if (nullness == /*sure*/2)
      return "!";
    else
      return "";
  }

  public mlsub.typing.Monotype resolve(TypeMap tm)
  {
    mlsub.typing.Monotype raw = rawResolve(tm);

    if (nullness == /*none*/0)
      return raw;
    else if (nullness == /*maybe*/1)
      return nice.tools.typing.Types.maybeMonotype(nice.tools.typing.Types.rawType(raw));
    else if (nullness ==  /*sure*/2)
      return nice.tools.typing.Types.sureMonotype(nice.tools.typing.Types.rawType(raw));
    else if (nullness == /*absent*/3)
      {
	if (raw instanceof mlsub.typing.MonotypeVar)
	  {
	    nice.tools.typing.Types.makeMarkedType((mlsub.typing.MonotypeVar) raw);
	    return raw;
	  }
	else
	  return nice.tools.typing.Types.sureMonotype(raw);
      }
    else
      throw Internal.error("Bad nullness tag");
  }

  /** called instead of toString if parenthesis are unnecessary */
  public String toStringExtern() { return this.toString(); }

  /** don't print type parameters */
  public String toStringBase() { return this.toString(); }

  /** iterates resolve() on the collection of Monotype */
  static mlsub.typing.Monotype[] resolve(TypeMap s, Monotype[] c)
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
 
  /**
    Wrapping a mlsub Monotype in a syntactic monotype
   */
  public static class MonotypeWrapper extends Monotype
  {
    final mlsub.typing.Monotype type;

    public MonotypeWrapper(mlsub.typing.Monotype m)
    {
      this.type = m;
    }

    boolean containsAlike() { return false; }
    
    public mlsub.typing.Monotype rawResolve(TypeMap tm) { return type; }

    Monotype substitute(Map map) { return this; }

    public Location location() { return Location.nowhere(); }

    public String toString() { return String.valueOf(type); }
  }

  public static final class MonotypeVarWrapper extends MonotypeWrapper
  {
    public MonotypeVarWrapper(mlsub.typing.MonotypeVar m)
    {
      super(m);
    }

    public mlsub.typing.Monotype rawResolve(TypeMap tm)
    {
      mlsub.typing.TypeSymbol res = tm.lookup(type.toString());
      if (res != null)
	return (mlsub.typing.Monotype) res;

      return type;
    }
  }

  /**
    Wrapper for delaying resolution of constructed monotypes.
   */
  public static final class SureMonotypeWrapper extends Monotype
  {
    mlsub.typing.TypeConstructor tc;
    mlsub.typing.Monotype[] params;

    public SureMonotypeWrapper(mlsub.typing.TypeConstructor tc, mlsub.typing.Monotype[] params)
    {
      this.tc = tc;
      this.params = params;
    }
 
    boolean containsAlike() { return false; }

    public mlsub.typing.Monotype rawResolve(TypeMap tm)
    {
      try{
        return nice.tools.typing.Types.sureMonotype(new mlsub.typing.MonotypeConstructor(tc, params));
      }
      catch(mlsub.typing.BadSizeEx e){
        // See if this is a class with default type parameters
        mlsub.typing.Monotype res = dispatch.getTypeWithTC(tc, params);
        if (res != null)
          return nice.tools.typing.Types.sureMonotype(res);

        throw User.error(this, "Class " + tc + Util.has(e.expected, "type parameter", e.actual));
      }
    }

    Monotype substitute(Map map) { return this; }

    public Location location() { return Location.nowhere(); }

    public String toString()
    {
      return tc + ( params != null ? Util.map("<",",",">",params) : "");
    }
  }
}
