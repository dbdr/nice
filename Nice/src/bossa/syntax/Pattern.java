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

import gnu.expr.*;
import nice.tools.code.*;

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

  Pattern(TypeConstructor tc, Expression atValue, boolean exactlyAt)
  {
    this.tc = tc;
    this.atValue = atValue;
    this.exactlyAt = exactlyAt;
  }

  final mlsub.typing.Monotype getType()
  {
    return t;
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
   * Type checking
   ****************************************************************/
  
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
	  {
	    Typing.leq(p.tc, monotypes[i]);
	    MonotypeConstructor mc = (MonotypeConstructor) monotypes[i].equivalent();
	    Typing.assertMinimal(mc.getTC());
	  }
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
  
  static TypeConstructor[] getAdditionalTC(Pattern[] patterns)
  {
    TypeConstructor[] res = new TypeConstructor[patterns.length];

    for (int i = 0; i < patterns.length; i++)
      res[i] = patterns[i].tc2;

    return res;
  }

  public boolean leq(Pattern that)
  {
    if (that.atAny() || this == that)
      return true;
    
    if (this.atAny())
      return false;

    if (that.atNull() && this.atNull())
      return true;
    
    if (that.atNull() || this.atNull())
      return false;

    if (!that.exactlyAt)
      return Typing.testRigidLeq(this.tc, that.tc); 
    else if (this.exactlyAt)
      return Typing.testRigidLeq(this.tc, that.tc) 
	&& Typing.testRigidLeq(that.tc, this.tc);
    else 
      return false;
  }

  public boolean matches(TypeConstructor tag)
  {
    if (atAny())
      return true;

    if (atNull())
      // @null matches no class
      return false;

    // a null tc is an unmatchable argument (e.g. function)
    if (tag == null)
      return false;

    if (exactlyAt)
      return Typing.testRigidLeq(tag, tc) && Typing.testRigidLeq(tc, tag);
    else
      return Typing.testRigidLeq(tag, tc);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if (atNull())
      return "@null";
    if (atAny())
      return "@_";
    
    StringBuffer res = new StringBuffer();
    if (name != null)
      res.append(name.toString());

    res.append(exactlyAt ? '#' : '@');
    
    if (typeConstructor != null)
      res.append(typeConstructor.toString());
    else if (tc != null)
      res.append(tc.toString());
    
    return res.toString();
  }

  /****************************************************************
   * Bytecode representation
   ****************************************************************/
  
  /**
   * Returns a string used to recognize this pattern in the bytecode.
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public String bytecodeRepresentation()
  {
    if (atAny())
      return "@_";
    
    return 
      (exactlyAt ? "#" : "@") 
      +
      (atNull() ? "NULL" : tc.toString().replace('$','.'));
  }
  
  public static String bytecodeRepresentation(Pattern[] patterns)
  {
    StringBuffer res = new StringBuffer();
    for(int i = 0; i < patterns.length; i++)
      res.append(patterns[i].bytecodeRepresentation());
    return res.toString();
  }
  
  public static Pattern read(String rep, int[]/*ref*/ pos, String methodName)
  {
    if (pos[0] >= rep.length())
      return null;

    if (rep.charAt(pos[0]) != '@' && rep.charAt(pos[0]) != '#')
      Internal.error("Invalid pattern representation at character " + pos[0] +
		     ": " + rep);

    boolean exact = rep.charAt(pos[0]) == '#';

    int start = ++pos[0];
      
    try{
      while(rep.charAt(pos[0]) != '@' && rep.charAt(pos[0]) != '#')
	pos[0]++;
    }
    catch(StringIndexOutOfBoundsException ex) {
      // reached the ned of the string
    }

    String name = rep.substring(start, pos[0]);

    TypeConstructor tc = null;
    Expression atValue = null;

    if (name.equals("_"))
      tc = null;
    else if (name.equals("NULL"))
      atValue = NullExp.instance;
    else
      {
	mlsub.typing.TypeSymbol sym = Node.getGlobalTypeScope().lookup(name);

	if (sym == null)
	  User.error("Pattern " + name +
		     " of method " + methodName + 
		     " is not known");

	tc = (TypeConstructor) sym;
      }
    return new Pattern(tc, atValue, exact);
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /**
     Returns code that tests if the parameter is matched.
  */
  public gnu.expr.Expression matchTest(gnu.expr.Expression parameter)
  {
    if (atNull())
      return Inline.inline(IsNullProc.instance, parameter);

    if (atAny())
      return QuoteExp.trueExp;

    gnu.bytecode.Type ct = nice.tools.code.Types.javaType(tc);

    if (exactlyAt)
      return Gen.isOfClass(parameter, ct);
    else
      /* 
	 This is not correct if the parameter is null:
	 we don't want @C to match a null value 
	 OPTIM: produce a '!is_null' in this case
      */
      //if (parameter.getType().isSubtype(ct))
      //return QuoteExp.trueExp;
      
      return InstanceOfProc.instanceOfExp(parameter, ct);
  }

  /****************************************************************
   * Fields
   ****************************************************************/
  
  LocatedString name;
  TypeIdent typeConstructor, additional;
  public TypeConstructor tc;
  TypeConstructor tc2;
  private bossa.syntax.Monotype type;
  private mlsub.typing.Monotype t;

  private boolean exactlyAt;
  private Expression atValue;

  public boolean atNull() { return atValue instanceof NullExp; }
  public boolean atAny()  { return atValue == null && tc == null; }
}
