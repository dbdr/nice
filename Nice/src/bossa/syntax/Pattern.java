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
import mlsub.typing.Typing;
import mlsub.typing.TypingEx;

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
     @param atValue constant value which the parameter must be equal to
       in order to match the pattern. Ex: null, 1, true, ...
     @param additional an additional TypeConstructor that 
       the argument must match.
       It must be a super-class of <code>tc</code>.
       It is only used for overloading resolution, not at runtime.
     @param type a monotype that must be equal to 
       the argument's runtime type. This is usefull to introduce
       a type constructor variable with the exact type of the argument.
   */
  public Pattern(LocatedString name, 
		 TypeIdent tc, Expression atValue,
		 boolean exactlyAt, TypeIdent additional,
		 bossa.syntax.Monotype type)
  {
    this.name = name;
    this.typeConstructor = tc;
    this.additional = additional;
    this.type = type;
    this.atValue = atValue;
    this.exactlyAt = exactlyAt;
  }

  public Pattern(LocatedString name, TypeIdent tc)
  {
    this(name, tc, null, false, null, null);
  }

  public Pattern(LocatedString name)
  {
    this(name, null, null, false, null, null);
  }

  final mlsub.typing.Monotype getType()
  {
    return t;
  }

  /**
     Assert that the monotypes belong the the patterns.
   */
  static void in(Monotype[] monotypes, Pattern[] patterns)
  throws TypingEx
  {
    for (int i = 0; i < monotypes.length; i++)
      {
	Pattern p = patterns[i];
	if (p.tc == null) continue;

	Typing.leq(monotypes[i], p.tc);
	if (p.exactlyAt)
	  Typing.leq(p.tc, monotypes[i]);
      }
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
      if (patterns[i].atNull())
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
      atNull() ? "NULL" : nice.tools.code.Types.bytecodeRepresentation(tc);

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

  private boolean exactlyAt;
  private Expression atValue;

  public boolean atNull() { return atValue instanceof NullExp; }
}
