/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
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
import mlsub.typing.*;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Typing;
import mlsub.typing.TypingEx;
import nice.tools.typing.Types;

//import gnu.expr.*;

/**
   Represents the information about one argument of a method body.
   
   @see MethodBodyDefinition
*/
public abstract class Pattern implements Located
{
  LocatedString name;
  TypeIdent typeConstructor;
  private TypeIdent additional;
  TypeConstructor tc;
  TypeConstructor tc2;

  // The class constraint verified by this pattern.
  private mlsub.typing.Constraint constraint;
  // The type of the class matched, if it is constrained.
  private mlsub.typing.Monotype patternType;

  private Location loc;

  Pattern(LocatedString name, Location loc)
  {
    this(name, null, loc);
  }

  Pattern(LocatedString name, TypeConstructor tc, Location loc)
  {
    this.name = name;
    this.tc = tc;
    this.loc = loc;
  }

  Pattern(TypeConstructor tc, TypeIdent add, Location loc)
  {
    this.tc = tc;
    this.additional = add;
    this.loc = loc;
  }

  Pattern(LocatedString name, TypeIdent ti, TypeIdent add, Location loc)
  {
    this.name = name;
    this.typeConstructor = ti;
    this.additional = add;
    this.loc = loc;
  }

  public final TypeConstructor getTC()
  {
    return tc;
  }

  TypeConstructor getRuntimeTC()
  {
    return null;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/
  
  void resolveTC(TypeScope scope)
  {
    if (typeConstructor != null)
      {
        TypeSymbol sym = typeConstructor.resolveToTypeSymbol(scope);

        if (sym instanceof TypeConstructor)
          tc = (TypeConstructor) sym;
        else
          throw User.error(this, typeConstructor + 
                           " is not a declared class or interface");

        if (exactlyAtType() && !TypeConstructors.instantiable(tc))
          User.error
	    (typeConstructor.location(), 
	     "Pattern #" + typeConstructor +
	     " cannot be matched because interfaces and abstract classes do not have direct instances.");

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

    Polytype classType = def.getConstrainedType();
    if (classType != null)
      {
        constraint = classType.getConstraint();
        patternType = classType.getMonotype();
      }
  }

  static void resolve(TypeScope tscope, VarScope vscope, Pattern[] patterns)
  {
    for(int i = 0; i < patterns.length; i++) {
      patterns[i].resolveTC(tscope);
    }
  }
 
  static void resolveValues(Pattern[] patterns)
  {
    for(int i = 0; i < patterns.length; i++) {
      patterns[i] = bossa.syntax.dispatch.resolveGlobalConstants(patterns[i]);
    }
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
      patterns[i].leq(monotypes[i]);
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
      Typing.leq(PrimitiveType.maybeTC, mc.getTC());

    if (tc == null)
      return;

    // the argument is not null
    Typing.leq(mc.getTC(), PrimitiveType.sureTC);
    Monotype type = Types.rawType(mc);

    if (constraint != null)
      {
	constraint.enter();
	Typing.leq(type, patternType);
	if (exactlyAtType())
	  {
	    Typing.leq(patternType, type);
	    MonotypeConstructor inner = (MonotypeConstructor) type.equivalent();
	    inner.getTC().setMinimal();
	  }
      }
    else
      {
	Typing.leq(type, tc);
	if (exactlyAtType())
	  {
	    Typing.leq(tc, type);
	    MonotypeConstructor inner = (MonotypeConstructor) type.equivalent();
	    inner.getTC().setMinimal();
	  }
      }
  }
  
  static void inDomain(Pattern[] patterns, Monotype[] types) throws TypingEx
  {
    for (int i = 0; i < patterns.length; i++)
      Types.setMarkedKind(types[i]);

    for (int i = 0; i < patterns.length; i++)
      patterns[i].inDomain(Types.rawType(types[i]));
  }

  private void inDomain(Monotype type) throws TypingEx
  {
    if (constraint != null)
      {
	constraint.enter();
	Typing.leq(patternType, type);
      }
    else
      {
	Typing.leq(tc, type);
      }

    if (tc2 != null)
      Typing.leq(tc2, type);
  }

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

  public abstract boolean matches(TypeConstructor tag);

  public abstract boolean matchesValue(ConstantExp val);

  public Pattern setDomainEq(boolean equal) 
  {
    return this;
  }     

  public void setDomainTC(TypeConstructor domaintc)  {}

  public void addValues(List values) {}

  public LocatedString getName() { return name; }

  public Location location() { return loc; }

  /**
   * Returns a string used to recognize this pattern in the bytecode.
   * for the different patterns the following signatures are used:
   *   any 		: `@_`
   *   null 		: `@NULL`
   *   type 		: `@` + type   where `.` in type are replaced by `$`
   *   exactly at type  : `#` + type   idem
   *   integer literal	: `@-` / `@+` + int_literal 
   *   char literal	: `@'c'`   where c is an unescaped char
   *   string literal	: `@"string"` where string is an escaped string
   *   reference        : `@` + globalconstantname
   *   int comparison   : `@>` / `@>=` / `@<` / `@<=` + int_literal
   *
   * This is usefull to distinguish alternatives of a method.
   */
  public abstract String bytecodeRepresentation();
  
  public static String bytecodeRepresentation(Pattern[] patterns)
  {
    StringBuffer res = new StringBuffer();
    for(int i = 0; i < patterns.length; i++)
      res.append(patterns[i].bytecodeRepresentation());

    return res.toString();
  }

  public static class Unknown extends RuntimeException {} 
 
  /**
     Returns code that tests if the parameter is matched.

     @param dispatchMade indicates that dispatch has already occured. It is
       still necessary to check for exact matching if applicable.
  */
  public abstract gnu.expr.Expression matchTest(gnu.expr.Expression parameter, boolean dispatchMade);

  public boolean atAny()  { return false; }
  public boolean atNull() { return false; }
  protected boolean exactlyAtType() { return false; } 
  public boolean atValue() { return false; }
  public boolean atTypeMatchingValue() { return false;}
}
