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
public class Pattern implements Located
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
		 bossa.syntax.Monotype type,
		 Location location)
  {
    this.name = name;
    this.typeConstructor = tc;
    this.additional = additional;
    this.type = type;
    this.atValue = atValue;
    this.exactlyAt = exactlyAt;
    this.location = location;
    if (tc != null) 
      {
	String ident = tc.getName().toString();
	if (ident.equals("true"))
	  {
	    this.atValue = new IdentExp(tc.getName());
	    this.typeConstructor = 
                     new TypeIdent(new LocatedString("boolean",location));
          }
	else if (ident.equals("false"))
	  {
	    this.atValue = new IdentExp(tc.getName());
	    this.typeConstructor = 
                     new TypeIdent(new LocatedString("boolean",location));
	  }
      }
  }

  public Pattern(LocatedString name)
  {
    this(name, null, null, false, null, null, name.location());
  }

  Pattern(LocatedString name, TypeIdent tc)
  {
    this(name, tc, null, false, null, null, name.location());
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
        if (exactlyAt && TypeConstructors.isInterface(tc))
          User.error(typeConstructor.location(), "Pattern #"+typeConstructor.toString()+
                " can never be matched because instances of an interface don't exist.");
	typeConstructor = null;
      }
    if (additional != null)
      {
	tc2 = additional.resolveToTC(scope);
	additional = null;
      }

    // Class constraints
    ClassDefinition def = ClassDefinition.get(tc);
    if (def == null)
      return;

    constraint = def.getResolvedConstraint();
    patternType = new MonotypeConstructor(tc, def.getTypeParameters());
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
	p.leq(monotypes[i]);
      }
  }

  /**
     Asserts that m is below this pattern.
   */
  private void leq(Monotype m) throws TypingEx
  {
    Types.setMarkedKind(m);
    m = m.equivalent();
    if (!(m instanceof MonotypeConstructor))
      Internal.error("Nullness check");
    
    MonotypeConstructor mc = (MonotypeConstructor) m;

    if (atNull())
      {
	Typing.leq(PrimitiveType.maybeTC, mc.getTC());
      }

    if (tc == null)
      return;
    
    // the argument is not null
    Typing.leq(mc.getTC(), PrimitiveType.sureTC);
    Monotype type = mc.getTP()[0];

    if (constraint != null)
      {
	constraint.enter();
	Typing.leq(type, patternType);
	if (exactlyAt)
	  {
	    Typing.leq(patternType, type);
	    MonotypeConstructor inner = (MonotypeConstructor) type.equivalent();
	    inner.getTC().setMinimal();
	  }
      }
    else
      {
	Typing.leq(type, tc);
	if (exactlyAt)
	  {
	    Typing.leq(tc, type);
	    MonotypeConstructor inner = (MonotypeConstructor) type.equivalent();
	    inner.getTC().setMinimal();
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

  /**
     Order on pattern that is compatible with the specificity of patterns.

     If that matches all values that this matches, the result is true.
     Additionally, if this matches values that are more specific than
     the values that matches, the result is also true. 

     For instance, assuming class B extends A, exactly the following are true:
       @A less @A
       #A less @A
       @B less @A
       #B less @A
       #A less #A
       @B less #A
       #B less #A
  */
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

    if (that.atBool())
      return this.atBool() && (this.atTrue() == that.atTrue());

    if (this.atBool())
      return that.tc == PrimitiveType.boolTC; 

    if (this.tc == that.tc)
      return this.exactlyAt || ! that.exactlyAt;

    return Typing.testRigidLeq(this.tc, that.tc); 
  }

  public boolean matches(TypeConstructor tag)
  {
    if (atAny())
      return true;

    if (atNull())
      return tag == PrimitiveType.nullTC;

    // a null tc is an unmatchable argument (e.g. function)
    if (tag == null)
      return false;

    if (tag == PrimitiveType.trueBoolTC)
      {
	if (atBool())
          return atTrue();
        return tc == PrimitiveType.boolTC;
      }

    if (tag == PrimitiveType.falseBoolTC)
      {
	if (atBool())
          return atFalse();
        return tc == PrimitiveType.boolTC;
      }
	
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
    if (atBool())
      return "@" + atValue.toString();
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

  public Location location()
  {
    return location;
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

    if (atBool())
      return "@" + atValue.toString();

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
    int len = rep.length();

    while(pos[0] < len && 
	  rep.charAt(pos[0]) != '@' && rep.charAt(pos[0]) != '#')
      pos[0]++;

    String name = rep.substring(start, pos[0]);

    TypeConstructor tc = null;
    Expression atValue = null;

    if (name.equals("_"))
      tc = null;
    else if (name.equals("NULL"))
      atValue = NullExp.instance;
    else if (name.equals("true") || name.equals("false") ) 
      {
	atValue = new IdentExp(new LocatedString(name, Location.nowhere()));
	tc = PrimitiveType.boolTC;
      }
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
	
    if (atBool())
      {
	if (atFalse())
          return Inline.inline(nice.lang.inline.BoolNotOp.instance, parameter);
        return parameter;
      }

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

  // The class constraint verified by this pattern.
  private mlsub.typing.Constraint constraint;
  // The type of the class matched, if it is constrained.
  private mlsub.typing.Monotype patternType;

  private boolean exactlyAt;
  public Expression atValue;

  private Location location;

  public boolean atNull() { return atValue == NullExp.instance; }
  public boolean atAny()  { return atValue == null && tc == null; }
  public boolean atBool() { 
    return atValue != null && tc == PrimitiveType.boolTC;
  }
  public boolean atTrue() { 
    return atBool() && atValue.toString().equals("true");
  }
  public boolean atFalse() { 
    return atBool() && atValue.toString().equals("false");
  }
}
