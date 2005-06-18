/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2005                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   The smallest unit containing code (typically, the content of a file).

   Definitions marked as 'private' are only visible inside their Module.
 */

public class Module
{
  public final bossa.modules.Package pkg;
  VarScope scope;
  String name;

  public Module(bossa.modules.Package pkg, String name,
                nice.tools.visibility.Scope scope)
  {
    this.pkg = pkg;
    this.name = name;
    this.scope = dispatch.createGlobalVarScope(0, scope);
  }

  bossa.modules.Compilation compilation() { return pkg.getCompilation(); }

  public boolean compiled() { return pkg.interfaceFile(); }

  public void addStaticImport(LocatedString value)
  {
    String fullName = value.toString();

    int lastDot = fullName.lastIndexOf('.');
    if (lastDot == -1)
      bossa.util.User.error(value, "Missing field or method name");

    String methodName = fullName.substring(lastDot+1);
    fullName = fullName.substring(0, lastDot);

    mlsub.typing.TypeConstructor tc =
      Node.getGlobalTypeScope().globalLookup(fullName, value.location());

    if(tc == null)
      bossa.util.User.error(value, "Unknown class: " + fullName);

    gnu.bytecode.Type type = nice.tools.code.Types.javaType(tc);
    if (! (type instanceof gnu.bytecode.ClassType))
      bossa.util.User.error(value, fullName + " is not a class");

    java.util.List symbols = dispatch.findJavaMethods
      ((gnu.bytecode.ClassType) type, methodName);

    if (symbols.size() == 0)
      bossa.util.User.error(value, methodName + " is not a static field or method of class " + fullName);

    for (java.util.Iterator i = symbols.iterator(); i.hasNext();)
      {
        /*Var*/Symbol s = (/*Var*/Symbol) i.next();
        scope.addSymbol(s);
      }
  }
}
