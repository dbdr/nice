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

import nice.tools.code.Types;

class FunSymbol extends PolySymbol
{
  FunSymbol(LocatedString name, 
	    Constraint constraint, FormalParameters parameters,
	    Monotype returnType, int arity)
  {
    super(name, 
	  new Polytype(constraint, 
		       new FunType(parameters.types(), returnType)));
    this.parameters = parameters;
    this.arity = arity;
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

  String explainWhyMatchFails(Arguments arguments)
  {
    String name = this.name.toString();

    // If parameters == null, 
    // this is a native method or a native constructor.
    boolean isConstructor = parameters != null && 
      "<init>".equals(name);
    if (! isConstructor)
      return "Method " + name + " expects parameters (" + 
	describeParameters() + ")";

    // Here we assume that ident for constructors is formatted as
    // "new ClassName". We might want something more robust.
    return "Class " + name.substring(4) + 
      " has the following fields:\n" +
      parameters + "\n" +
      "Please provide values for the fields, at least for those with no default value.\nThe syntax is:\n  " + name + "(field1: value1, ..., fieldN: valueN)";
  }
}
