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

package bossa.syntax;


class FunSymbol extends PolySymbol
{
  FunSymbol(LocatedString name, Polytype type, FormalParameters parameters,
	    int arity)
  {
    super(name, type);
    this.parameters = parameters;
    this.arity = arity;
  }

  FormalParameters parameters;
  int arity;

  /****************************************************************
   * Overloading resolution
   ****************************************************************/
    
  /**
     @return
     0 : doesn't match
     1 : wasn't even a function
     2 : matches
  */
  int match(Arguments arguments)
  {
    if (parameters == null)
      // true for constructors, for instance. case might be removed
      if (!arguments.plainApplication(arity))
	return 0;
      else
	return 2;
    else if (!parameters.match(arguments))
      return 0;
    else
      return 2;
  }
}
