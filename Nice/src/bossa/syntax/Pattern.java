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
import mlsub.typing.*;
import mlsub.typing.TypeConstructor;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Typing;
import mlsub.typing.TypingEx;
import nice.tools.typing.Types;

import gnu.expr.*;
import nice.tools.code.Gen;

/**
   Represents the information about one argument of a method body.
   
   @see MethodBodyDefinition

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sf.net)
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
     @param runtimeTC a type constructor that will be bound to 
       the argument's runtime class.
   */
  public Pattern(LocatedString name,
		 TypeIdent tc, ConstantExp atValue,
		 LocatedString refName,
		 boolean exactlyAt, int kind,
		 TypeIdent additional,
		 TypeConstructor runtimeTC,
		 Location location)
  {
    this.name = name;
    this.typeConstructor = tc;
    this.additional = additional;
    this.runtimeTC = runtimeTC;
    this.atValue = atValue;
    this.exactlyAt = exactlyAt;
    this.compareKind = kind;
    this.refName = refName;
    this.location = location;

    if (atValue != null) 
      {
	if (atValue instanceof StringConstantExp)
	  this.typeConstructor = new TypeIdent(
		new LocatedString("java.lang.String", atValue.location()));
	else
	  this.tc = atValue.tc;
      }
  }

  Pattern(LocatedString name, TypeIdent tc)
  {
    this(name, tc, null, null, false, NONE, null, null, name.location());
  }

  Pattern(TypeConstructor tc, boolean exactlyAt)
  {
    this.tc = tc;
    this.exactlyAt = exactlyAt;
  }

  Pattern(ConstantExp atValue)
  {
    this(null, null, atValue, null, false, NONE, null, null, 
	atValue!=null ? atValue.location() : Location.nowhere() );    
  }

  Pattern(int kind, ConstantExp atValue)
  {
    this(null, null, atValue, null, false, kind, null, null, atValue.location());    
  }

  Pattern(LocatedString name)
  {
    this(name, null, null, null, false, NONE, null, null, name.location());
  }

  static Pattern any(LocatedString name)
  {
    Pattern res = new Pattern(name);
    return res;
  }

  public final TypeConstructor getTC()
  {
    return tc;
  }

  final TypeConstructor getRuntimeTC()
  {
    return runtimeTC;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/
  
  void resolveTC(TypeScope scope)
  {
    if (typeConstructor != null)
      {
        TypeSymbol sym = typeConstructor.resolveToTypeSymbol(scope);

        if (sym == TopMonotype.instance)
          {
            // This is @Object, which is always true, nothing to test.
          }
        else if (sym instanceof TypeConstructor)
          tc = (TypeConstructor) sym;
        else
          throw User.error(this, typeConstructor + 
                           " is not a declared class or interface");

        if (exactlyAt && !TypeConstructors.instantiable(tc))
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
      bossa.syntax.dispatch.resolveGlobalConstants(patterns[i]);
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

    if (that.atNonNull())
      // The only reason it could be false is if we are atNull, but that's
      // already handled.
      return true;

    // that is not atNonNull nor atAny at this point
    if (this.atNonNull())
      return false;

    if (this.atEnum() && that.atEnum())
      return this.atValue.equals(that.atValue);

    if (this.atIntCompare() && that.atIntCompare())
      {
	if (this.atLess() != that.atLess())
	  return false;

        long val = this.atValue.longValue();
        if (this.compareKind == LT) val--;
        if (this.compareKind == GT) val++;

	return that.matchesCompareValue(val);
      }
      
    if (that.atIntCompare())
      return this.atIntValue() && that.matchesCompareValue(this.atValue.longValue());

    if (that.atValue != null && !that.atNull() &&!that.atIntCompare())
      return (this.atValue != null && !this.atNull() &&!this.atIntCompare()) && this.atValue.equals(that.atValue);

    if (this.tc == that.tc)
      return this.exactlyAt || ! that.exactlyAt;

    return Typing.testRigidLeq(this.tc, that.tc); 
  }

  /**
   returns true when the patterns can't match the same values.
   May return false if it can't be determined easily.
  */
  public boolean disjoint(Pattern that)
  {
    if (this.atAny() || that.atAny())
      return false;

    if (this.atNull() ^ that.atNull())
      return true;

    if (this.atBool() && that.atBool())
      return ! this.atValue.equals(that.atValue);

    if (this.atReference() && that.atReference())
      return ! this.atValue.equals(that.atValue);

    if (this.atString() && that.atString())
      return ! this.atValue.equals(that.atValue);
    
    if (this.atIntCompare() && that.atIntCompare())
      {
	if (this.atLess() == that.atLess())
	  return false;

        long val = this.atValue.longValue();
        if (this.compareKind == LT) val--;
        if (this.compareKind == GT) val++;

	return ! that.matchesCompareValue(val);
      }

    if (this.atIntCompare() && that.atIntValue())
      return ! this.matchesCompareValue(that.atValue.longValue());

    if (this.atIntValue() && that.atIntCompare())
      return ! that.matchesCompareValue(this.atValue.longValue());

    if (this.atIntValue() && that.atIntValue())
      return ! this.atValue.equals(that.atValue);    

    if (this.exactlyAt && that.exactlyAt)
      return this.tc != that.tc;

    if (TypeConstructors.isClass(this.tc) && TypeConstructors.isClass(that.tc))
      return (! Typing.testRigidLeq(this.tc, that.tc)) &&
	     (! Typing.testRigidLeq(that.tc, this.tc));

    //TODO: check disjoints for all tc's with a <T | T <: TC_PA, T <: TC_PB> constraint

    return false;
  }

  public boolean matches(TypeConstructor tag)
  {
    if (atAny())
      return true;

    if (atNull())
      return tag == PrimitiveType.nullTC;

    if (atNonNull())
      return tag != PrimitiveType.nullTC;

    // a null tc is an unmatchable argument (e.g. function)
    if (tag == null)
      return false;

    if ((atValue != null) && ! atTypeMatchingValue())
      return false;

    if (atIntCompare())
      return Typing.testRigidLeq(tag, PrimitiveType.longTC);

    if (exactlyAt)
      return Typing.testRigidLeq(tag, tc) && Typing.testRigidLeq(tc, tag);

    return Typing.testRigidLeq(tag, tc);
  }

  public boolean matchesValue(ConstantExp val)
  {
    if (atAny())
      return true;

    if (atIntCompare())
      return val.value instanceof Number && matchesCompareValue(val.longValue());

    if (atNull())
      return false;

    return (atValue != null) && atValue.equals(val);
  }

  private boolean matchesCompareValue(long val)
  {
    if (compareKind == LT)
      return val < atValue.longValue();

    if (compareKind == LE)
      return val <= atValue.longValue();

    if (compareKind == GE)
      return val >= atValue.longValue();

    if (compareKind == GT)
      return val > atValue.longValue();

    return false;
  }

  public void setDomainEq(boolean equal)
  {
    // only set it to atAny if it's a @type pattern
    if (equal && atValue == null && !exactlyAt)
      tc = null;

    // don't allow integer primitive types in @type and #type patterns
    if (!equal && atValue == null && Typing.testRigidLeq(tc, PrimitiveType.longTC))
      User.error(location, "A pattern cannot have a primitive type that is different from the declaration.");

  }     

  public void setDomainTC(TypeConstructor domaintc)
  {
    if (atIntValue())
      {
        if (domaintc != null)
          {
	    if (Typing.testRigidLeq(domaintc, PrimitiveType.intTC))
	    {
	      tc = PrimitiveType.intTC;
	      return;
            }
            else if (Typing.testRigidLeq(domaintc, PrimitiveType.longTC))
	    {
	      tc = PrimitiveType.longTC;
	      return;
            }
            else if (Typing.testRigidLeq(domaintc, PrimitiveType.charTC))
              return;
          }    
        User.error(location, "Integer value patterns are not allowed for methods where the declared parameter isn't a primitive type.");
      }
  }

  public List getEnumValues ()
  {
    List res = new LinkedList();

    if (atBool())
      {
        res.add(ConstantExp.makeBoolean(true, location));
        res.add(ConstantExp.makeBoolean(false, location));
        return res;
      }

    // atEnum()
    List symbols = ((EnumDefinition)((EnumDefinition.EnumSymbol)atValue.value).getDefinition()).symbols;
    for (Iterator it = symbols.iterator(); it.hasNext(); )
      {
	EnumDefinition.EnumSymbol sym = (EnumDefinition.EnumSymbol)it.next();
        res.add(new ConstantExp(null, tc, sym, sym.name.toString(), location));
      }

    return res;
  }
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if (atIntCompare())
      {
	String prefix = "";
	if (compareKind == LT) prefix = "<";
	if (compareKind == LE) prefix = "<=";
	if (compareKind == GT) prefix = ">";
	if (compareKind == GE) prefix = ">=";
        return (name!=null? name.toString() : "") + prefix + atValue;
      }

    if (atValue != null)
      return atValue.toString();

    if (atAny())
      return name != null ? name.toString() : "Any";

    StringBuffer res = new StringBuffer();
    res.append(exactlyAt ? "#" : (name != null ? "" : "@"));
    
    if (typeConstructor != null)
      res.append(typeConstructor);
    else if (tc != null)
      res.append(tc);

    if (name != null)
      res.append(" " + name);
    
    return res.toString();
  }

  public Location location() { return location; }

  /****************************************************************
   * Bytecode representation
   ****************************************************************/
  
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
  public String bytecodeRepresentation()
  {
    if (atAny())
      return "@_";

    if (atNull())
      return "@NULL";

    if (atValue != null)
      {
        if (atIntCompare())
	  {
            String prefix = "";
	    if (compareKind == LT) prefix = "<";
	    if (compareKind == LE) prefix = "<=";
	    if (compareKind == GT) prefix = ">";
	    if (compareKind == GE) prefix = ">=";

	    return "@" + prefix + atValue;
	  }

        if (atValue.value instanceof Number)
          return "@" + (atValue.longValue() >= 0 ? "+" : "") + atValue;

        if (atValue.value instanceof Character)
          return "@\'" + atValue.value + "\'";

	if (atReference())
          return "@=" + refName;

        if (atString())
	  return "@\"" + ((StringConstantExp)atValue).escapedValue + "\""; 

	return "@" + atValue;
      }

    return (exactlyAt ? "#" : "@") + tc.toString().replace('$','.');
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

    if (rep.charAt(pos[0]) == '\'')
      { //char literal patterns are 3 chars
        pos[0] += 3;
      }
    else if (rep.charAt(pos[0]) == '\"')
      { //we need to skip possible '@' or '#' content of the string literal
        pos[0] += 2;
	while(pos[0] < len &&
		! ( ( rep.charAt(pos[0]) == '@' || rep.charAt(pos[0]) != '#')
		&& rep.charAt(pos[0]-1) == '\"' 
		&& rep.charAt(pos[0]-2) != '\\') )
	  pos[0]++;

      }
    else
      {
	while(pos[0] < len && 
		rep.charAt(pos[0]) != '@' && rep.charAt(pos[0]) != '#')
	  pos[0]++;
      }

    String name = rep.substring(start, pos[0]);

    if (name.length() > 1)
      {
	if (name.charAt(0) == '\'')
	  return new Pattern(new ConstantExp(PrimitiveType.charTC,
		new Character(name.charAt(1)), name, Location.nowhere()));

	if (name.charAt(0) == '-')
          return new Pattern(ConstantExp.makeNumber(new LocatedString(name,
		 Location.nowhere())));

        if (name.charAt(0) == '+')
          return new Pattern(ConstantExp.makeNumber(new LocatedString(
		name.substring(1), Location.nowhere())));

        if (name.charAt(0) == '\"')
          return new Pattern(ConstantExp.makeString(new LocatedString(
		name.substring(1,name.length()-1), Location.nowhere())));

        if (name.charAt(0) == '<')
	  {
	    if (name.charAt(1) == '=')
	      return new Pattern(LE, ConstantExp.makeNumber(new LocatedString(
		name.substring(2), Location.nowhere())));

	    return new Pattern(LT, ConstantExp.makeNumber(new LocatedString(
		name.substring(1), Location.nowhere())));
          }

        if (name.charAt(0) == '>')
	  {
	    if (name.charAt(1) == '=')
	      return new Pattern(GE, ConstantExp.makeNumber(new LocatedString(
		name.substring(2), Location.nowhere())));

	    return new Pattern(GT, ConstantExp.makeNumber(new LocatedString(
		name.substring(1), Location.nowhere())));
          }
        
        if (name.charAt(0) == '=')
          {
            LocatedString refName = new LocatedString(name.substring(1),
				Location.nowhere());

            Pattern res = new Pattern(refName);
            bossa.syntax.dispatch.resolveGlobalConstants(res);
	    return res;
          }
      }

    if (name.equals("_"))
      return new Pattern((ConstantExp)null);

    if (name.equals("NULL"))
      return new Pattern(NullExp.create(Location.nowhere()));

    if (name.equals("true") || name.equals("false") ) 
      return new Pattern(ConstantExp.makeBoolean(name.equals("true"),
		Location.nowhere()));

    TypeSymbol sym = Node.getGlobalTypeScope().lookup(name);

    if (sym == null)
      // This can happen if the class exists only in a later version
      // of the JDK.
      throw new Unknown();

    return new Pattern((TypeConstructor) sym, exact);
  }

  public static class Unknown extends RuntimeException {}

  /****************************************************************
   * Code generation
   ****************************************************************/

  /**
     Returns code that tests if the parameter is matched.

     @param dispatchMade indicates that dispatch has already occured. It is
       still necessary to check for exact matching if applicable.
  */
  public gnu.expr.Expression matchTest(gnu.expr.Expression parameter,
                                       boolean dispatchMade)
  {
    if (atAny() || (dispatchMade && ! exactlyAt))
      return QuoteExp.trueExp;

    if (atNull())
      return Gen.isNullExp(parameter);

    if (atBool())
      {
	if (atValue.isFalse())
          return Gen.boolNotExp(parameter);

        return parameter;
      }

    if (atIntValue())
      {
        String kind;
        if (compareKind == LT) kind = "Lt";
        else if (compareKind == LE) kind = "Le";
        else if (compareKind == GE) kind = "Ge";
        else if (compareKind == GT) kind = "Gt";
        else kind = "Eq";

        return Gen.integerComparison(kind, parameter, atValue.longValue());
      }

    if (atString())
      return Gen.stringEquals((String)atValue.value, parameter);

    if (atReference())
      return Gen.referenceEqualsExp(atValue.compile(), parameter);

    gnu.bytecode.Type ct = nice.tools.code.Types.javaType(tc);
    if (exactlyAt)
      return Gen.isOfClass(parameter, ct);
      
    return Gen.instanceOfExp(parameter, ct);
  }

  /****************************************************************
   * Fields
   ****************************************************************/
  
  LocatedString name,refName;
  TypeIdent typeConstructor;
  private TypeIdent additional;
  TypeConstructor tc;
  TypeConstructor tc2;
  private TypeConstructor runtimeTC;

  // The class constraint verified by this pattern.
  private mlsub.typing.Constraint constraint;
  // The type of the class matched, if it is constrained.
  private mlsub.typing.Monotype patternType;

  private boolean exactlyAt;
  public ConstantExp atValue;
  public int compareKind = NONE;

  public static final int NONE = 0;
  public static final int LT = 1;
  public static final int LE = 2;
  public static final int GT = 4;
  public static final int GE = 5;

  private Location location;

  public boolean atIntValue() { 
    return atValue != null && (atValue.value instanceof Number ||
		atValue.value instanceof Character);
  }
  public boolean atNull() { return (atValue != null) && atValue.isNull(); }
  /** This pattern only specifies that the vlaue is not null.
      This cannot be explicitely used in source programs, but it is useful
      when a method with a non-null parameter specializes one where that 
      parameter can be null.
  */
  public boolean atNonNull() { return tc == PrimitiveType.sureTC; }
  public boolean atAny()  { return atValue == null && tc == null; }
  public boolean atBool() { 
    return atValue != null && tc == PrimitiveType.boolTC;
  }
  public boolean atString() { return atValue instanceof StringConstantExp; }
  public boolean atReference() { return atValue != null && atValue.value instanceof VarSymbol; }
  public boolean atEnum() { return atReference() && atValue.value instanceof EnumDefinition.EnumSymbol; }
  public boolean atIntCompare() { return compareKind > 0;}
  public boolean atLess() { return compareKind == LT || compareKind == LE; }
  public boolean atTypeMatchingValue() { return atEnum() || atIntCompare() || atBool();}
  public boolean atSimpleValue() { return atString() || (atIntValue() && ! atIntCompare()); }
  public boolean atEnumerableValue() { return atBool() || atEnum(); }
}
