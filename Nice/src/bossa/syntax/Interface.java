/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Interface.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Wed Oct 13 17:57:37 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/** 
 * An interface symbol.
 */
public class Interface extends Node
  implements Located, TypeSymbol
{
  /**
   * Constructs a new Interface symbol
   *
   * @param name the name of the interface
   */
  public Interface(LocatedString name)
  {
    super(Node.global);
    this.name=name;
  }

  Interface(InterfaceDefinition def)
  {
    super(Node.global);
    this.definition=def;
  }
  
  public TypeSymbol cloneTypeSymbol()
  {
    if(definition==null)
      return new Interface(name);
    else
      return new Interface(definition);
  }
  
  public boolean hasName(LocatedString s)
  {
    return name.equals(s);
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    if(definition==null)
      {
	TypeSymbol s;
	// Should not be usefull anymore
	/*
	  if(name.content.startsWith("Top"))
	  s=InterfaceDefinition.top(Integer.parseInt(name.content.substring(3)));
	  else
	*/
	s=typeScope.lookup(name);
	if(s==null)
	  User.error(name,"Interface "+name+" is not defined"," in "+typeScope);
	if(s instanceof InterfaceDefinition)
	  definition=(InterfaceDefinition)s;
	else
	  User.error(name,name+" is not an interface but a "
		     +s.getClass()); 
      }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }

  public String toString()
  {
    if(definition==null)
      return "\""+name+"\"";
    else 
      return definition.name.toString();
  }

  public LocatedString getName()
  {
    return name;
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  LocatedString name;
  public InterfaceDefinition definition;
}
