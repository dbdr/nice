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

package bossa.syntax;

/**
   A variable (local, field of a class, parameter of a method or function).
   temporarily abstract superclass of VarSymbol
*/

public abstract class Symbol extends Node
{
  public Symbol(LocatedString name)
  {
    super(Node.upper);
    this.name = name;
    addSymbol(this);
  }

  LocatedString name;

  // commenting this triggers a bug
  abstract mlsub.typing.Polytype getType();
}