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
import mlsub.typing.Domain;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeVar;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeConstructor;

/**
   Represents the information about one argument of a method body.
   
   @see MethodBodyDefinition

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class Pattern
{
  /**
     Builds a new pattern.
     
     @param name the name of the argument
     @param tc a TypeConstructor that the argument must match
       for this alternative to be selected
     @param additional an additional TypeConstructor that 
       the argument must match.
       It must be a super-class of <code>tc</code>.
       It is only used for overloading resolution, not at runtime.
     @param type a monotype that must be equal to 
       the argument's runtime type. This is usefull to introduce
       a type constructor variable with the exact type of the argument.
   */
  public Pattern(LocatedString name, 
		 TypeIdent tc, boolean atNull, TypeIdent additional,
		 bossa.syntax.Monotype type)
  {
    this.name = name;
    this.typeConstructor = tc;
    this.additional = additional;
    this.type = type;
    this.atNull = atNull;
  }

  public Pattern(LocatedString name, TypeIdent tc)
  {
    this(name, tc, false, null, null);
  }

  public Pattern(LocatedString name)
  {
    this(name, null, false, null, null);
  }

  final mlsub.typing.Monotype getType()
  {
    return t;
  }

  final boolean isSharp()
  {
    return tc!=null &&
      TypeConstructors.isSharp(tc);
  }

  Domain getDomain()
  {
    if(tc==null)
      return Domain.bot;
    
    MonotypeVar[] tp = MonotypeVar.news(tc.arity());
    
    return new Domain(new Constraint(tp,null),
		      new MonotypeConstructor(tc, tp));
  }
  
  /**
   * Iterates getDomain on a collection of Pattern.
   */
  static Domain[] getDomain(Pattern[] patterns)
  {
    Domain[] res = new Domain[patterns.length];

    for (int i = 0; i < patterns.length; i++)
      res[i] = patterns[i].getDomain();

    return res;
  }

  /**
   * Iterates getTypeConstructor on a collection of Pattern.
   */
  static TypeConstructor[] getTC(Pattern[] patterns)
  {
    TypeConstructor[] res = new TypeConstructor[patterns.length];

    for (int i = 0; i < patterns.length; i++)
      res[i] = patterns[i].tc;

    return res;
  }
  
  public static final TypeConstructor nullTC = 
    new TypeConstructor("null", null, false, true);

  static TypeConstructor[] getLinkTC(Pattern[] patterns)
  {
    TypeConstructor[] res = new TypeConstructor[patterns.length];

    for (int i = 0; i < patterns.length; i++)
      if (patterns[i].atNull)
	res[i] = nullTC;
    else
	res[i] = patterns[i].tc;

    return res;
  }
  
  static TypeConstructor[] getAdditionalTC(Pattern[] patterns)
  {
    TypeConstructor[] res = new TypeConstructor[patterns.length];

    for (int i = 0; i < patterns.length; i++)
      res[i] = patterns[i].tc2;

    return res;
  }
  
  Polytype getPolytype()
  {
    if(tc==null)
      return Polytype.bottom();
    
    MonotypeVar[] params = MonotypeVar.news(tc.arity());
    return new Polytype(new Constraint(params, null),
			new MonotypeConstructor(tc, params));
  }
  
  /**
   * Iterates getPolytype on a collection of Pattern.
   */
  static Polytype[] getPolytype(Collection patterns)
  {
    Iterator i=patterns.iterator();
    Polytype[] res = new Polytype[patterns.size()];

    int n = 0;
    while(i.hasNext())
      {
	Pattern p = (Pattern) i.next();
	res[n++] = p.getPolytype();
      }
    return res;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/
  
  void resolveTC(TypeScope scope)
  {
    if (typeConstructor != null)
      {
	tc = typeConstructor.resolveToTC(scope);
	typeConstructor = null;
      }
    if (additional != null)
      {
	tc2 = additional.resolveToTC(scope);
	additional = null;
      }
  }
  
  void resolveType(TypeScope scope)
  {
    if(type!=null)
      {
	t = type.resolve(scope);
	type = null;
      }
  }
  
  static void resolveTC(TypeScope scope, Pattern[] patterns)
  {
    for(int i = 0; i < patterns.length; i++)
      patterns[i].resolveTC(scope);
  }
  
  static void resolveType(TypeScope scope, Pattern[] patterns)
  {
    for(int i = 0; i < patterns.length; i++)
      patterns[i].resolveType(scope);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    StringBuffer res = new StringBuffer();
    if (name != null)
      res.append(name.toString());

    if (typeConstructor != null)
      res.append("@").append(typeConstructor.toString());
    else if (tc != null)
      res.append("@").append(tc.toString());
    
    return res.toString();
  }

  public final static String AT_encoding = "$";
  public final static int AT_len = AT_encoding.length();
  
  /**
   * Returns a string used to recognize this pattern in the bytecode.
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public String bytecodeRepresentation()
  {
    String enc = 
      atNull ? "NULL" : nice.tools.code.Types.bytecodeRepresentation(tc);

    return AT_encoding + enc;
  }
  
  public static String bytecodeRepresentation(Pattern[] patterns)
  {
    StringBuffer res = new StringBuffer();
    for(int i = 0; i < patterns.length; i++)
      res.append(patterns[i].bytecodeRepresentation());
    return res.toString();
  }
  
  LocatedString name;
  TypeIdent typeConstructor, additional;
  TypeConstructor tc, tc2;
  private bossa.syntax.Monotype type;
  private mlsub.typing.Monotype t;

  private boolean atNull;
  public boolean atNull() { return atNull; }
}
