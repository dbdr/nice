/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package gnu.expr;

import gnu.bytecode.*;

/**
   A LambdaExp member of a class (with a this argument).

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class MemberLambdaExp extends LambdaExp
{
  public MemberLambdaExp(Declaration thisDecl)
  {
    this.thisDecl = thisDecl;
    thisDecl.context = this;
  }

  private Declaration thisDecl;

  void enterFunction (Compilation comp)
  {
    // Do the normal stuff.
    super.enterFunction(comp);

    // Save 'this' if it is captured
    if (thisDecl.field != null)
      {
        CodeAttr code = comp.getCode();
        thisDecl.loadOwningObject(comp);
        code.emitPushThis();
        code.emitPutField(thisDecl.field);
      }
  }
}
