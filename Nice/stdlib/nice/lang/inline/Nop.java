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

package nice.lang.inline;

import gnu.expr.*;
import gnu.bytecode.*;

/**
   Just compiles its argument, producing no bytecode itself.
   
   @version $Date$
   @author Daniel Bonniot
 */

public class Nop 
extends gnu.mapping.Procedure1 implements Inlineable
{
  public static Nop create(String param)
  {
    return nop;
  }
  
  private static Nop nop = new Nop();
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    exp.getArgs()[0].compile(comp, target);
  }
  
  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return args[0].getType();
  }

  public Object apply1 (Object arg1)
  {
    return arg1;
  }
}
