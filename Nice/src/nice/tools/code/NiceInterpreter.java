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

// File    : NiceInterpreter.java
// Created : Thu Sep 07 18:03:49 2000 by Daniel Bonniot

package nice.tools.code;

import gnu.bytecode.*;
import gnu.expr.*;

/**
   Implements language dependant methods of gnu.expr.Interpreter.

   But Nice has no interpreter at the time being!

   @version $Date$
   @author Daniel Bonniot
 */

public class NiceInterpreter extends gnu.expr.Interpreter
{
  public String getName()
  {
    return "Nice";
  }

  public Type getTypeFor(Class clas)
  {
    return Type.make(clas);
  }

  /** Must be called before code generation. */
  public static void init()
  {
    if (Interpreter.defaultInterpreter == null)
      {
	Interpreter.defaultInterpreter = new NiceInterpreter();
      }
  }
  
  /** Not implemented. */
  public Object read (gnu.mapping.InPort in)
    throws java.io.IOException, gnu.text.SyntaxException
  {
    throw new Error("Not implemented");
  }
  
  /** Not implemented. */
  public void print (Object obj, gnu.mapping.OutPort out)
  {
    throw new Error("Not implemented");
  }    

  /** Not implemented. */
  public gnu.text.Lexer getLexer(gnu.mapping.InPort inp, 
				 gnu.text.SourceMessages messages)
  {
    throw new Error("Not implemented");
  }
}
