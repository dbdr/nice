/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : FormalParameters.java
// Created : Mon Oct 09 10:36:00 2000 by Daniel Bonniot

package bossa.syntax;

import bossa.util.*;

/**
   Parameters of a method declaration.

   @version $Date$
   @author Daniel Bonniot
 */

public class FormalParameters
{
  /**
     An anonymous formal parameter.
   */
  public static class Parameter
  {
    public Parameter(Monotype type) { this.type = type; }

    Monotype type;

    boolean match(String id) { return false; }

    public String toString()
    {
      return type.toString();
    }
  }
  public static class NamedParameter extends Parameter
  {
    public NamedParameter(Monotype type, LocatedString name)
    { super(type); this.name = name; }

    LocatedString name;

    boolean match(String id) { return name.toString().equals(id); }

    public String toString()
    {
      return type + " " + name;
    }
  }
  public static class OptionalParameter extends NamedParameter
  {
    public OptionalParameter
      (Monotype type, LocatedString name, Expression defaultValue)
    { super(type, name); this.defaultValue = defaultValue; }

    Expression defaultValue;

    public String toString()
    {
      return type + " " + name + " = " + defaultValue;
    }
  }
  
  public FormalParameters(java.util.List parameters)
  {
    if (parameters != null)
      {
	this.parameters = 
	  (Parameter[]) parameters.toArray(new Parameter[parameters.size()]);
	this.size = this.parameters.length;
      }
    else
      this.size = 0;
  }

  void addThis(Monotype type)
  {
    if (parameters[0] != null)
      Internal.error("No room for \"this\"");
    
    parameters[0] = new Parameter(type);
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
  
  java.util.List /* of Monotype */ types()
  {
    if (parameters == null)
      return null;
    
    java.util.List res = new java.util.ArrayList(size);
    for (int i = 0; i < size; i++)
      res.add(parameters[i].type);
    return res;
  }
  
  /****************************************************************
   * Resolving overloading
   ****************************************************************/

  boolean match(Arguments args)
  {
    int[] map = new int[size];
    
    for (int i = 0; i< args.size(); i++)
      {
	Arguments.Argument a = args.get(i);
	if (a.name == null)
	  {
	    if (fill(map, i))
	      return false;
	  }
	else
	  {
	    if (fill(map, a.name.toString(), i))
	      return false;
	  }
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
    while (i < map.length && map[i] != 0)
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
  private int size;
}
