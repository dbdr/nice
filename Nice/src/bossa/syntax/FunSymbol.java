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

import java.util.*;
import nice.tools.typing.Types;

class FunSymbol extends PolySymbol
{
  FunSymbol(LocatedString name, 
	    Constraint constraint, FormalParameters parameters,
	    Monotype returnType)
  {
    super(name, 
	  new Polytype(constraint, 
		       new FunType(parameters.types(), returnType)));
    this.parameters = parameters;
    this.arity = parameters.size;
  }

  FunSymbol(LocatedString name, 
	    mlsub.typing.Polytype type, FormalParameters parameters,
	    int arity)
  {
    super(name, null);
    this.parameters = parameters;
    this.arity = arity;
    this.type = type;
  }

  FormalParameters parameters;
  int arity;

  String describeParameters()
  {
    if (parameters != null)
      return parameters.toString();

    mlsub.typing.Monotype m = Types.rawType(getType().getMonotype());
    if (m instanceof mlsub.typing.FunType)
      return bossa.util.Util.map("", ", ", "", ((mlsub.typing.FunType) m).domain());

    bossa.util.Internal.warning(this, "Non functional type in a functional symbol");
    return "";
  }

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
      {
        // true for constructors, for instance. case might be removed
        if (!arguments.plainApplication(arity, this))
	  return 0;
      }
    else if (!parameters.match(arguments, this))
      return 0;

    return 2;
  }

  String explainWhyMatchFails(Arguments arguments)
  {
    if (isFieldAccess())
      {
        if (arguments.size() == 0)
          return name + " is not defined";

        return name + " is a field of class " + describeParameters();
      }

    return "Method " + name + " expects parameters (" + 
      describeParameters() + ")";
  }
}
