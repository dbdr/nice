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

import java.util.*;
import bossa.util.*;
import bossa.util.Location;
import mlsub.typing.Constraint;
import mlsub.typing.Monotype;

/**
   An object constructor.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

abstract class Constructor extends MethodDeclaration
{
  Constructor(NiceClass classe,
              boolean isDefault,
              Location location,
              FormalParameters formals,
              Constraint cst,
              Monotype[] parameters,
              Monotype returnType)
  {
    super(new LocatedString("<init>", location), formals,
	  cst, parameters, returnType);

    this.classe = classe;
    this.isDefault = isDefault;
  }

  Constructor(NiceClass classe,
              boolean isDefault,
              Location location,
              FormalParameters formals,
              bossa.syntax.Constraint cst,
              bossa.syntax.Monotype returnType)
  {
    super(new LocatedString("<init>", location), cst, returnType, formals);

    this.classe = classe;
    this.isDefault = isDefault;
  }

  protected NiceClass classe;
  final protected boolean isDefault;

  abstract gnu.expr.Expression getInitializationCode(boolean implicitThis);

  void addConstructorCallSymbol()
  {
    mlsub.typing.Polytype type = new mlsub.typing.Polytype
      (getType().getConstraint(),
       new mlsub.typing.FunType(getArgTypes(), PrimitiveType.voidType));
    classe.addConstructorCallSymbol
      (new MethodDeclaration.Symbol(name, type) {
          gnu.expr.Expression compileInCallPosition()
          {
            return getInitializationCode(true);
          }
        });
  }

  public void printInterface(java.io.PrintWriter s)
  { throw new Error("Should not be called"); }

  String explainWhyMatchFails(Arguments arguments)
  {
    if (! isDefault)
      return super.explainWhyMatchFails(arguments);

    String name = classe.getName();

    StringBuffer res = new StringBuffer();
    res.append("Class ").append(name);


    //Arguments where none expected
    if (parameters.size == 0)
      {
        res.append(" has no fields. Therefore its constructor takes no arguments.");
        return res.toString();
      }

    //No such field
    List nonmatching = arguments.noMatchByName(parameters);
    if (!nonmatching.isEmpty())
      {
        res.append(" has no field named "+nonmatching.get(0));
        return res.toString();
      }

    //Required fields missing, or else too many arguments
    // - three different messages depending on whether
    //an explanation of the syntax is necessary
    res = new StringBuffer();
    List missing = arguments.missingArgs(parameters);
    Iterator missingFields = null;

    if (arguments.size() == 0 || missing.size() > 0)
      {
        res.append("Fields of class ").append(name).append(" require initial values.\n");
        if (arguments.size() == 0)
          {
            res.append(syntaxExample())
              .append("Class ").append(name).append(" has the following fields:\n");
            missingFields = parameters.iterator();
          }
        else
          {
            res.append("These fields are missing:\n");
            missingFields = missing.iterator();
          }
      }
    else
      {
        res.append("Too many arguments when constructing new instance of class ")
          .append(name)
          .append(".\n")
          .append("The constructor accepts the following arguments:\n" );
        missingFields = parameters.iterator();
      }
    while(missingFields.hasNext())
      {
        res.append("  ")
          .append(missingFields.next())
          .append("\n");
      }
    return res.toString();

  }

  private String syntaxExample()
  {
    String name = classe.getName();
    StringBuffer res = new StringBuffer();
    res.append("Use the following syntax:\n")
      .append("  new ").append(name).append("(");

    Iterator params = parameters.getRequiredParameters().iterator();
    int paramCount = 0;
    int len = name.length();
    while(params.hasNext())
      {
        FormalParameters.Parameter param =
          (FormalParameters.Parameter)params.next();
        if (paramCount != 0)
          res.append(", ");
        if (paramCount == 3 && params.hasNext())
          {
            res.append('\n');
            for(int i = 0; i < len; i++) {res.append(' ');}
            res.append("        ");
            paramCount = 0;
          }
        if (param instanceof FormalParameters.NamedParameter)
          res.append(param.getName()).append(": value");
        else
          res.append("value");
        paramCount++;
      }
    res.append(")\n\n");
    return res.toString();
  }
}
