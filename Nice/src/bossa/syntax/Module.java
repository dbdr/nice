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

  static VarScope javaScope;

  public Module(bossa.modules.Package pkg, String name, VarScope scope)
  {
    this.pkg = pkg;
    this.name = name;
    this.scope = scope;

    javaScope = scope;
  }

  bossa.modules.Compilation compilation() { return pkg.getCompilation(); }

  public boolean compiled() { return pkg.interfaceFile(); }
}
