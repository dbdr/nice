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

// File    : ImplementsCst.java
// Created : Fri Aug 27 10:45:33 1999 by bonniot
//$Modified: Mon Apr 03 16:29:20 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * A type constructor implements an interface.
 * 
 * @author bonniot
 */

public class ImplementsCst extends AtomicConstraint
{
  public ImplementsCst(TypeConstructor tc, Interface itf)
  {
    this.tc=tc;
    this.itf=itf;
    this.itfDef=null;
    addChild(itf);
  }

  public ImplementsCst(TypeConstructor tc, InterfaceDefinition itfDef)
  {
    if(itfDef==null)
      Internal.error("Null interface definition");
    this.tc=tc;
    this.itf=null;
    this.itfDef=itfDef;
    // We know the variance of tc
    tc.setVariance(itfDef.variance);
  }

  InterfaceDefinition def()
  {
    if(itfDef!=null)
      return itfDef;
    else
      return itf.definition;
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  AtomicConstraint resolve(TypeScope scope)
  {
    tc=tc.resolve(scope);
    return this;
  }

  AtomicConstraint substitute(Map map)
  {
    return new ImplementsCst(tc.substitute(map),def());
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  /**
   * Enter the constraint into the typing context.
   */
  void assert() throws bossa.typing.TypingEx
  {
    bossa.typing.Typing.assertImp(tc,def(),false);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return tc+":"+def();
  }

  String getParentFor(TypeConstructor tc)
  {
    if(this.tc==tc)
      return def().toString();
    else
      return null;
  }

  TypeConstructor tc;
  Interface itf;
  private InterfaceDefinition itfDef;
}
