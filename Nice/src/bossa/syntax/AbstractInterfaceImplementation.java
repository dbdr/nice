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

import mlsub.typing.*;
import bossa.util.User;

/**
   Declaration that an existing class implements an abstract interface.

   The abstract interface must belong to the current package.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class AbstractInterfaceImplementation extends Definition
{
  LocatedString className;
  LocatedString interfaceName;

  public AbstractInterfaceImplementation
    (LocatedString className, LocatedString interfaceName)
  {
    super(className, Node.none);

    this.className = className;
    this.interfaceName = interfaceName;
  }

  TypeConstructor classTC;
  Interface interfaceITF;

  public void resolve()
  {
    classTC = new TypeIdent(className).resolveToTC(typeScope);
    TypeIdent ident = new TypeIdent
      (new LocatedString(module.getName() + '.' + interfaceName.content, 
			 interfaceName.location()));
    interfaceITF = ident.resolveToItf(typeScope);

    createContext();
  }

  private void createContext()
  {
    try {
      Typing.assertImp(classTC, interfaceITF, true);
    }
    catch(TypingEx ex) {
      User.error(this, "Class " + classTC + " cannot implement " + 
		 interfaceITF + ": they do not have the same number or kind of type parameters");

    }
  }

  public void printInterface(java.io.PrintWriter w)
  {
    w.print("class ");
    w.print(classTC);
    w.print(" implements ");
    w.print(interfaceName);
    w.println(";");
  }

  public void compile()
  {
  }
}
