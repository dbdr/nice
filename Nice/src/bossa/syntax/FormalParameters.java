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

import bossa.util.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
   Parameters of a method declaration.

   @version $Date$
   @author Daniel Bonniot
 */

public class FormalParameters extends Node
{
  /**
     An anonymous formal parameter.
   */
  public static class Parameter
  {
    public Parameter(Monotype type) { this.type = type; }

    Monotype type;

    Parameter cloneParam()
    {
      return new Parameter(type);
    }

    boolean match(String id) { return false; }
    boolean requiresName() { return false; }

    void typecheck(mlsub.typing.Monotype domain)
    {
    }

    Expression value() { return null; }

    boolean isOverriden() { return false; }

    void resolve(VarScope scope, TypeScope typeScope)
    {
      if (symbol != null)
	symbol.state = Symbol.ARGUMENT_REFERENCE;
    }

    void resolve(Info info)
    {
      if (symbol != null)
	symbol.state = Symbol.ARGUMENT_REFERENCE;
    }

    public String toString()
    {
      return type.toString();
    }

    LocatedString getName() { return null; }

    void resetType(Monotype type)
    {
      this.type = type;
      if (symbol != null)
        symbol.syntacticType = type;
    }

    Symbol getSymbol()
    {
      symbol = new Symbol(getName(), type);
      return symbol;
    }

    static class Symbol extends MonoSymbol
    {
      private Symbol (LocatedString name, Monotype type)
      {
	super(name, type);
	this.state = NOT_ACCESSIBLE;
      }

      /** The symbol is not accessible.
	  This ensures that there are no forward references to
	  another parameter inside a default value.
      */
      static final int NOT_ACCESSIBLE = 0;

      /** The symbol can be accessed by later arguments, using special means. 
       */
      static final int ARGUMENT_REFERENCE = 1;

      /** The symbol is fully accessible, for the body of the function.
      */
      static final int ACCESSIBLE = 2;

      private int state;

      int getState () 
      { 
	if (state == ARGUMENT_REFERENCE)
          // This parameter is referenced by a later one.
          // We will need to copy its value.
	  copies = new java.util.Stack();

	return state;
      }

      java.util.Stack copies;

      public void setDeclaration (gnu.expr.Declaration declaration, 
				  boolean isThis)
      {
	super.setDeclaration(declaration, isThis);
      }
    }

    private Symbol symbol;
  }

  /**
     A named parameter. 
     It can thus be used out-of-order at the call site.
  */
  public static class NamedParameter extends Parameter
  {
    public NamedParameter(Monotype type, LocatedString name)
    { super(type); this.name = name; }

    public NamedParameter(Monotype type, LocatedString name, boolean nameRequired)
    { this(type, name); this.nameRequired = nameRequired; }

    LocatedString name;

    boolean nameRequired = false;
    boolean requiresName() { return nameRequired; }

    Parameter cloneParam()
    {
      NamedParameter res = new NamedParameter(type, name);
      res.nameRequired = this.nameRequired;
      return res;
    }

    boolean match(String id) { return name.toString().equals(id); }

    public String toString()
    {
      return type + " " + name;
    }

    LocatedString getName() { return name; }
  }

  /**
     A named parameter with a default value.
     It can thus be omitted at the call site.
  */
  public static class OptionalParameter extends NamedParameter
  {
    public OptionalParameter
      (Monotype type, LocatedString name, Expression defaultValue)
    { 
      super(type, name); 
      this.defaultValue = defaultValue;
    }

    public OptionalParameter
      (Monotype type, LocatedString name, boolean nameRequired, 
       Expression value)
    { 
      this(type, name, value); 
      this.nameRequired = nameRequired;
    }

    public OptionalParameter
      (Monotype type, LocatedString name, boolean nameRequired, 
       Expression value, boolean overriden)
    { 
      this(type, name, nameRequired, value);
      this.overriden = overriden;
    }

    Expression value()
    {
      return defaultValue;
    }

    Expression defaultValue;

    boolean overriden;

    boolean isOverriden() { return overriden; }

    Parameter cloneParam()
    {
      return new OptionalParameter(type, name, nameRequired, defaultValue, overriden);
    }

    void resolve(VarScope scope, TypeScope typeScope)
    {
      defaultValue = dispatch.analyse(defaultValue, scope, typeScope);
      super.resolve(scope, typeScope);
    }

    void resolve(Info info)
    {
      defaultValue = dispatch.analyse(defaultValue, info);
      super.resolve(null, null);
    }

    void typecheck(mlsub.typing.Monotype domain)
    {
      defaultValue = defaultValue.noOverloading();
      //defaultValue = defaultValue.resolveOverloading(new mlsub.typing.Polytype(domain));
      bossa.syntax.dispatch.typecheck(defaultValue);

      try {
	mlsub.typing.Typing.leq(defaultValue.getType(), domain);
      }
      catch(mlsub.typing.TypingEx e){
	User.error(name, defaultValue + " is not a value of type " + type);
      }
    }

    public String toString()
    {
      defaultValue = defaultValue.noOverloading();
      return type + " " + name + " = " + defaultValue;
    }
  }

  public static Parameter createParameter(Monotype type, LocatedString name,
					Expression defaultValue)
  {
     if (defaultValue != null)
       return new OptionalParameter(type, name, defaultValue);

     if (name != null)
       return new NamedParameter(type, name);

     return new Parameter(type);
  }

  /****************************************************************
   * Main class
   ****************************************************************/

  public FormalParameters(java.util.List parameters)
  {
    super(Node.none);

    if (parameters == null)
      return;

    this.parameters = 
      (Parameter[]) parameters.toArray(new Parameter[parameters.size()]);
    this.size = this.parameters.length;
  }

  FormalParameters(Parameter[] parameters)
  {
    super(Node.none);

    if (parameters == null)
      return;

    this.parameters = parameters;
    this.size = parameters.length;
  }

  void addThis(Monotype type)
  {
    if (parameters[0] != null)
      Internal.error("No room for \"this\"");
    
    parameters[0] = new NamedParameter(type, thisName);
  }

  boolean hasThis()
  {
    return parameters != null && parameters[0].match("this");
  }

  static final LocatedString thisName = 
    new LocatedString("this", Location.nowhere());

  /**
     @return the name of the <code>rank</code>th parameter,
             or null if it is an anonymous parameter.
  */
  public LocatedString getName(int rank)
  {
    return parameters[rank].getName();
  }

  boolean hasDefaultValue(int rank)
  {
    Parameter p = parameters[rank];
    return p.value() != null && ! p.isOverriden();
  }

  boolean hasDefaultValue()
  {
    for (int i = size; --i>=0; )
      if (parameters[i].value() != null)
        return true;

    return false;
  }

  /****************************************************************
   * Walk methods, used in NiceMethod.create
   ****************************************************************/

  boolean containsAlike()
  {
    for (int i = size; --i>=0; )
      if (parameters[i] != null && parameters[i].type.containsAlike())
	return true;

    return false;
  }
  
  void substitute(java.util.Map map)
  {
    for (int i = size; --i>=0; )
      if (parameters[i] != null) // it is the case for "this" placeholder
	parameters[i].type = parameters[i].type.substitute(map);
  }
  
  Monotype[] types()
  {
    if (parameters == null)
      return null;
    
    Monotype[] res = new Monotype[size];
    for (int i = 0; i < size; i++)
      res[i] = parameters[i].type;

    return res;
  }

  public MonoSymbol[] getMonoSymbols()
  {
    if (parameters == null) return null;
    
    MonoSymbol[] res = new MonoSymbol[size];
    for (int i = 0; i < size; i++)
      res[i] = parameters[i].getSymbol();
 
    return res;
  }

  void typecheck(mlsub.typing.Monotype[] domain)
  {
    for (int i = 0; i<size; i++)
      parameters[i].typecheck(domain[i]);
  }

  void resolve()
  {
    for (int i = 0; i<size; i++)
      parameters[i].resolve(scope, typeScope);

    for (int i = 0; i<size; i++)
      if (parameters[i].symbol != null)
	parameters[i].symbol.state = Parameter.Symbol.ACCESSIBLE;
  }

  /** This is a hack to allow bossa.syntax.analyse to make the
      default values be resolved.
  */
  void resolveCalledFromAnalyse(Info info)
  {
    for (int i = 0; i<size; i++)
      parameters[i].resolve(info);
  }

  /****************************************************************
   * Resolving overloading
   ****************************************************************/

  /**
     Check if arguments match the formal parameters.

     If succesfull, append to <code>args</code>'s applicationExpressions
     the computed array of expressions, that denote the call,
     including the default values of the optional parameters not passed.
  */
  // FIXME change VarSymbol to FunSymbol once converted to nice
  boolean match(Arguments args, VarSymbol symbol)
  {
    int[] map = new int[size];

    // first pass, fill holes with names given at the call site
    for (int i = 0; i< args.size(); i++)
      {
	Arguments.Argument a = args.get(i);
	if (a.name != null)
	  if (fill(map, a.name.toString(), i))
	    return false;
      }

    // second pass, fill with the positional parameters
    for (int i = 0; i< args.size(); i++)
      {
	Arguments.Argument a = args.get(i);
	if (a.name == null)
	  if (fill(map, i))
	    return false;
      }

    // check that each parameter is either supplied or optional
    // stores the invocation expressions
    Expression[] exps = new Expression[size];
    for (int i = 0; i < size; i++)
      if (map[i] == 0)
	{
	  exps[i] = parameters[i].value();
	  if (exps[i] == null)
	    return false;
	}
      else
	exps[i] = args.getExp(map[i] - 1);

    args.applicationExpressions.put(symbol, exps);
    args.usedArguments.put(symbol, map);
    return true;
  }
  
  boolean fill(int[] map, int num)
  {
    int i = 0;
    while (i < map.length && (map[i] != 0 || parameters[i].requiresName()))
      i++;
    
    if (i == map.length)
      return true;

    map[i] = num + 1;
    return false;
  }
  
  boolean fill(int[] map, String id, int num)
  {
    int i = 0;
    while (i < map.length && (map[i] != 0 || !parameters[i].match(id)))
      i++;
    
    if (i == map.length)
      return true;

    map[i] = num + 1;
    return false;
  }
  
  public boolean hasMatchFor(String s)
  {
    for (int i = 0; i<size; i++)
      if (parameters[i].match(s))
	return true;

    return false;
  }  

  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    return Util.map("", ", ", "", parameters);
  }

  public List asList()
  {
    if (parameters != null)
      return Arrays.asList(parameters);
   
    return new ArrayList();
  }

  public List getRequiredParameters()
  {
    LinkedList res = new LinkedList();
    for(int i = 0; i < size; i++) 
      {
        Parameter param = parameters[i];
        if (!(param instanceof OptionalParameter))
          {
            res.add(parameters[i]);
          }
      }
    return res;
  }

  public java.util.Stack[] getParameterCopies()
  {
    java.util.Stack[] res = null;

    for(int i = 0; i < size; i++) 
      {
        Parameter param = parameters[i];
        if (param.symbol.copies != null)
          {
            if (res == null)
              res = new java.util.Stack[parameters.length];
            res[i] = param.symbol.copies;
          }
      }

    return res;
  }

  List getParameters(TypeScope scope)
  {
    ArrayList res = new ArrayList(size);
    for (int i = 0; i < size; i++)
      {
        Parameter p = parameters[i].cloneParam();
        p.type = Monotype.create(p.type.resolve(scope));
        res.add(p);
      }
    return res;
  }

  gnu.bytecode.Attribute asBytecodeAttribute()
  {
    return new gnu.bytecode.MiscAttr("parameters", this.toString().getBytes());
  }

  static FormalParameters readBytecodeAttribute(gnu.bytecode.MiscAttr attr,
                                                bossa.modules.Parser parser)
  {
    String value = new String(attr.data);

    if (Debug.bytecodeAttributes)
      Debug.println("Read attribute " + attr.getName() + "=\"" + value + 
                    "\" from " + attr.getContainer());

    FormalParameters res = parser.formalParameters(value);

    if (res == null)
      throw Internal.error
        ("Could not parse '" + attr.getName() + "' bytecode attribute:\n" +
         "In method: " + attr.getContainer() + "\n" +
         "Value    : " + value);

    return res;
  }

  private Parameter[] parameters;
  int size;
}
