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

// File    : NewExp.java
// Created : Thu Jul 08 17:15:15 1999 by bonniot
//$Modified: Wed Jun 21 19:19:08 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * Call of an object constructor.
 */
public class NewExp extends CallExp
{
  public NewExp(TypeIdent ti, List arguments)
  {
    super(null, arguments);
    this.ti = ti;
  }

  // we cannot assume resolveTC is called from findJavaClasses
  // for instance, if this new exp belongs to a global variable definition
  // it is not the case.
  private void resolveTC()
  {
    if (tc != null)
      return;
    
    tc = ti.resolveToTC(typeScope);
    ti = null;
  }
  
  void findJavaClasses()
  {
    super.findJavaClasses();
    resolveTC();
  }
  
  void resolve()
  {
    super.resolve();
    resolveTC();
    
    List constructors = TypeConstructors.getConstructors(tc);
    if (constructors == null)
      User.error(this, tc + " has no constructor");
    
    fun = new ExpressionRef // not necessary
      (new OverloadedSymbolExp(constructors,
			       new LocatedString("new "+tc.toString(),
// not tc.location() which is the location of the definition of the class
						 location()), 
			       null));
  }

  void typecheck()
  {
    if(!TypeConstructors.instantiable(tc))
      if(TypeConstructors.constant(tc))
	User.error(this,
		   tc+" is abstract, it can't be instantiated");
      else
	User.error(this,
		   tc+" is a type variable, it can't be instantiated");
    super.typecheck();
  }  

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String cl = (ti == null ? tc.toString() : ti.toString());
    return "new "+cl+"("+Util.map("", ", ", "", parameters)+")";
  }

  private TypeIdent ti;
  private mlsub.typing.TypeConstructor tc;
}
