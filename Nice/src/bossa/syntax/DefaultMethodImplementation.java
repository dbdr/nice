/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   A the default implementation of a method.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

import bossa.util.User;

class DefaultMethodImplementation extends MethodImplementation
{
  DefaultMethodImplementation 
    (LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     FormalParameters parameters,
     Contract contract,
     Statement body)
  {
    super(name, body, getAnyPatterns(parameters, name.location()));
    this.declaration = new NiceMethod.WithDefault
      (name, constraint, returnType, parameters, contract, this);
    addChild(declaration);
  }

  public static Pattern[] getAnyPatterns(FormalParameters parameters,
                                         bossa.util.Location loc)
  {
    Pattern[] res = new Pattern[parameters.size];
    for (int i = 0; i < res.length; i++)
      {
        LocatedString name = parameters.getName(i);
        if (name == null)
          name = new LocatedString("_", loc);

        res[i] = Pattern.any(name);
      }
    return res;
  }

  void doResolve()
  {
    //scope = declaration.scope;
    typeScope = declaration.typeScope;

    declaration.doResolve();

    buildSymbols();

    // Do not reset scope and typeScope to null.
  }

  void typedResolve()
  {
    declaration.typedResolve();
  }

  void resolveBody()
  {
    super.resolveBody();
    /*alternative =*/ new bossa.link.SourceAlternative(this);
  }

  void innerTypecheck() throws mlsub.typing.TypingEx
  {
    Node.currentFunction = this;
    if (hasThis())
      Node.thisExp = new SymbolExp(parameters[0], location());

    try{ 
      bossa.syntax.dispatch.typecheck(body);
    }
    finally{ 
      Node.currentFunction = null; 
      Node.thisExp = null;
    }
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(declaration.toString());
    s.print("= ...\n");
  }

  protected gnu.bytecode.Type[] javaArgTypes()
  {
    return declaration.javaArgTypes();
  }
}
