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
import mlsub.typing.Typing;
import mlsub.typing.TypingEx;
import nice.tools.typing.Types;

import gnu.expr.*;
import nice.tools.code.Gen;

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

    constraint = def.getResolvedConstraint();
    patternType = new MonotypeConstructor(tc, def.getTypeParameters());
  }

  private static VarSymbol findRefSymbol(LocatedString refName)
  {
    VarSymbol symbol = null;
    for (Iterator it = Node.getGlobalScope().lookup(refName).iterator(); it.hasNext();)
      {
	Object sym = it.next();
	if (sym instanceof GlobalVarDeclaration.GlobalVarSymbol ||
            sym instanceof EnumDefinition.EnumSymbol )
          symbol = (VarSymbol)sym;
      }

    return symbol;
  }

  void resolveGlobalConstants()
  {
    if (refName != null)
      {
	VarSymbol sym = findRefSymbol(refName);
	if (sym instanceof GlobalVarDeclaration.GlobalVarSymbol)
	  {
	    GlobalVarDeclaration.GlobalVarSymbol symbol = (GlobalVarDeclaration.GlobalVarSymbol)sym;
	    if (symbol.getValue() instanceof ConstantExp && symbol.constant)
	      {
		ConstantExp val = (ConstantExp)symbol.getValue();
		if (Typing.testRigidLeq(val.tc, PrimitiveType.longTC))
		  {
		    tc = val.tc;
		    atValue = val;
                    return;
		  }
	      }
	  }
	User.error(refName, refName.toString() + " is not a global constant with an integer value."); 
      }

    if (name == null)
      return;

    VarSymbol sym = findRefSymbol(name);
    if (sym == null)
      return;

    if (sym instanceof EnumDefinition.EnumSymbol)
      {
         EnumDefinition.EnumSymbol symbol = (EnumDefinition.EnumSymbol)sym;
	 NewExp val = (NewExp)symbol.getValue();

	 tc = val.tc;
	 atValue = new ConstantExp(null, tc, symbol,
				name.toString(), location);
        return;
      }
    
    GlobalVarDeclaration.GlobalVarSymbol symbol = (GlobalVarDeclaration.GlobalVarSymbol)sym;
    if (symbol.getValue() instanceof ConstantExp)
      {
        if (!symbol.constant)
          User.error(name, "" + name + " is not constant");

	ConstantExp val = (ConstantExp)symbol.getValue();

	if (val.tc == PrimitiveType.floatTC)
	  return;

	if (val instanceof StringConstantExp)
          typeConstructor = new TypeIdent(new LocatedString("java.lang.String",
								location));
	 tc = val.tc;
	 atValue = val;
       }
     else if (symbol.getValue() instanceof NewExp)
       {
	 NewExp val = (NewExp)symbol.getValue();

	 tc = val.tc;
	 atValue = new ConstantExp(null, tc, symbol,
				name.toString(), location);
       }
     else
       User.error(name, "The value of " + name + " can't be used as pattern");
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
      patterns[i].resolveGlobalConstants();
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

    if (that.atBool())
      return this.atBool() && (this.atTrue() == that.atTrue());

    if (this.atBool())
      return that.tc == PrimitiveType.boolTC; 

    if (this.atEnum() && that.atEnum())
      return false;

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

    if (that.atNonBoolValue())
      return this.atNonBoolValue() && this.atValue.equals(that.atValue);

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

    if (atNonBoolValue() && !atEnum() )
      return false;

    if (atIntCompare())
      return Typing.testRigidLeq(tag, PrimitiveType.longTC);

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

    return Typing.testRigidLeq(tag, tc);
  }

  public boolean matchesValue(ConstantExp val)
  {
    if (atAny())
      return true;

    if (atIntCompare())
      return val.value instanceof Number && matchesCompareValue(val.longValue());

    return atNonBoolValue() && atValue.equals(val);
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
    if (!atEnum())
      return res;

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
          return "@=" + name;

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

	    VarSymbol sym = findRefSymbol(refName);
	    NewExp val;
            if (sym instanceof GlobalVarDeclaration.GlobalVarSymbol )
              {
	        GlobalVarDeclaration.GlobalVarSymbol symbol = (GlobalVarDeclaration.GlobalVarSymbol)sym;
 	        val = (NewExp)symbol.getValue();
	      }
            else
              {
                EnumDefinition.EnumSymbol symbol = (EnumDefinition.EnumSymbol) sym;
	        val = (NewExp)symbol.getValue();
              }
            
	    return new Pattern(new ConstantExp(null, val.tc, sym,
		    		refName.toString(), refName.location()));
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
      User.error("Pattern " + name + " of method " + methodName + 
	     " is not known");

    return new Pattern((TypeConstructor) sym, exact);
  }

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
	if (atFalse())
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
  TypeIdent typeConstructor, additional;
  public TypeConstructor tc;
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
  public boolean atNonBoolValue() { 
    return atValue != null && !atBool() && !atNull() &&!atIntCompare();
  }
  public boolean atNull() { return atValue instanceof NullExp; }
  public boolean atAny()  { return atValue == null && tc == null; }
  public boolean atBool() { 
    return atValue != null && tc == PrimitiveType.boolTC;
  }
  public boolean atTrue() { return atValue != null && atValue.isTrue(); }
  public boolean atFalse() { return atValue != null && atValue.isFalse(); }
  public boolean atString() { return atValue instanceof StringConstantExp; }
  public boolean atReference() { return atValue != null && atValue.value instanceof VarSymbol; }
  public boolean atEnum() { return atReference() && atValue.value instanceof EnumDefinition.EnumSymbol; }
  public boolean atIntCompare() { return compareKind > 0;}
  public boolean atLess() { return compareKind == LT || compareKind == LE; }
  public boolean atTypeMatchingValue() { return atEnum() || atIntCompare(); }
}
