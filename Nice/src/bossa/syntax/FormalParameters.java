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
  public static class Parameter extends Node
  {
    public Parameter(Monotype type) { super(Node.down); this.type = type; }

    Monotype type;

    boolean match(String id) { return false; }
    boolean requiresName() { return false; }

    void typecheck(mlsub.typing.Monotype domain)
    {
    }

    public String toString()
    {
      return type.toString();
    }

    LocatedString getName() { return null; }
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
      (Monotype type, LocatedString name, boolean nameRequired, Expression value)
    { this(type, name, value); this.nameRequired = nameRequired; }

    Expression defaultValue;

    void resolve()
    {
      defaultValue = dispatch.analyse(defaultValue, scope, typeScope);
    }

    void typecheck(mlsub.typing.Monotype domain)
    {
      defaultValue = defaultValue.noOverloading();
      //defaultValue = defaultValue.resolveOverloading(new mlsub.typing.Polytype(domain));

      try {
	mlsub.typing.Typing.leq(defaultValue.getType(), domain);
      }
      catch(mlsub.typing.TypingEx e){
	User.error(name, defaultValue + " is not a value of type " + type);
      }
    }

    public String toString()
    {
      return type + " " + name + " = " + defaultValue;
    }
  }
  
  /****************************************************************
   * Main class
   ****************************************************************/

  public FormalParameters(java.util.List parameters)
  {
    super(Node.down);

    if (parameters == null)
      return;

    this.parameters = 
      (Parameter[]) parameters.toArray(new Parameter[parameters.size()]);
    this.size = this.parameters.length;
    
    for (int i = 0; i < size; i++)
      if (this.parameters[i] != null)
	addChild(this.parameters[i]);
  }

  FormalParameters(Parameter[] parameters)
  {
    super(Node.down);

    if (parameters == null)
      return;

    this.parameters = parameters;
    this.size = parameters.length;
    
    for (int i = 0; i < size; i++)
      if (parameters[i] != null)
	addChild(parameters[i]);
  }

  void addThis(Monotype type)
  {
    if (parameters[0] != null)
      Internal.error("No room for \"this\"");
    
    parameters[0] = new NamedParameter(type, thisName);
    addChild(parameters[0]);
  }
  
  private LocatedString thisName = 
    new LocatedString("this", Location.nowhere());

  /**
     @return the name of the <code>rank</code>th parameter,
             or null if it is an anonymous parameter.
  */
  public LocatedString getName(int rank)
  {
    return parameters[rank].getName();
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
      res[i] = new MonoSymbol(parameters[i].getName(), parameters[i].type);
    return res;
  }

  void typecheck(mlsub.typing.Monotype[] domain)
  {
    for (int i = 0; i<size; i++)
      parameters[i].typecheck(domain[i]);
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
  boolean match(Arguments args)
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
	if (parameters[i] instanceof OptionalParameter)
	  exps[i] = ((OptionalParameter) parameters[i]).defaultValue;
	else
	  return false;
      else
	exps[i] = args.getExp(map[i] - 1);
    args.applicationExpressions.add(exps);
    return true;
  }
  
  boolean fill(int[] map, int num)
  {
    int i = 0;
    while (i < map.length && (map[i] != 0 || parameters[i].requiresName()))
      i++;
    
    if (i == map.length)
      return true;
    else
      {
	map[i] = num + 1;
	return false;
      }
  }
  
  boolean fill(int[] map, String id, int num)
  {
    int i = 0;
    while (i < map.length && (map[i] != 0 || !parameters[i].match(id)))
      i++;
    
    if (i == map.length)
      return true;
    else
      {
	map[i] = num + 1;
	return false;
      }
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    return Util.map("", ", ", "", parameters);
  }

  private Parameter[] parameters;
  int size;
}
