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
import nice.tools.code.Gen;

import mlsub.typing.*;
import mlsub.typing.Constraint;
import mlsub.typing.Monotype;

import gnu.bytecode.*;
import gnu.expr.*;
import gnu.expr.Expression;

/**
   An object constructor.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

class Constructor extends MethodDeclaration
{
  Constructor(NiceClass classe, NiceClass.Field[] fields, int index,
	      Location location, 
	      FormalParameters formals,
	      Constraint cst,
	      Monotype[] parameters, 
	      Monotype returnType)
  {
    super(new LocatedString("<init>", location), formals, 
	  cst, parameters, returnType);

    this.classe = classe;
    this.fields = fields;
    this.index = index;
  }

  private NiceClass classe;
  private NiceClass.Field[] fields;
  private int index;

  Expression getConstructorInvocation(boolean omitDefaults)
  {
    getCode();
    ConstructorExp lambda = 
      // lambdaOmitDefaults is null if the two versions are identical
      omitDefaults && lambdaOmitDefaults != null 
      ? lambdaOmitDefaults : this.lambda;
    return new QuoteExp(new InitializeProc(lambda));
  }

  /** The constructor, with all the arguments. */
  private ConstructorExp lambda;

  /** The constructor, with only non-default. */
  private ConstructorExp lambdaOmitDefaults;

  protected Expression computeCode()
  {
    this.lambdaOmitDefaults = createBytecode(true);
    this.lambda = createBytecode(false);
    return new QuoteExp(new InstantiateProc(this.lambda));
  }

  /**
     @param omitDefaults if true, do not take the value of fields with
            default values as parameters, but use that default instead.
  */
  private ConstructorExp createBytecode(boolean omitDefaults)
  {
    ClassType thisType = (ClassType) javaReturnType();
    Expression thisExp = new ThisExp(thisType);

    MonoSymbol[] fullArgs = parameters.getMonoSymbols();
    Type[] fullArgTypes = javaArgTypes();

    List args = new LinkedList();
    List argTypes = new LinkedList();

    for (int i = 0; i < parameters.size; i++)
      {
        if (omitDefaults && parameters.hasDefaultValue(i))
          continue;

        args.add(fullArgs[i]);
        argTypes.add(fullArgTypes[i]);
      }

    // Do not create a second constructor omiting defaults if there is
    // no default to omit!
    if (omitDefaults && args.size() == fullArgs.length)
      return null;

    ConstructorExp lambda = Gen.createConstructor
      (thisType, 
       (Type[]) argTypes.toArray(new Type[argTypes.size()]), 
       (MonoSymbol[]) args.toArray(new MonoSymbol[args.size()]));

    Expression[] body = new Expression[1 + fields.length];
    body[0] = callSuper(thisExp, fullArgs, omitDefaults);

    final int superArgs = fullArgs.length - fields.length;

    for (int i = 0; i < fields.length; i++)
      {
        bossa.syntax.Expression value = fields[i].value;

        Expression fieldValue;
        if (!omitDefaults || value == null)
          // Use the provided parameter.
          fieldValue = fullArgs[superArgs + i].compile();
        else
          // Use the default value.
          fieldValue = value.compile();

        body[ i + 1] = fields[i].method.compileAssign(thisExp, fieldValue);
      }

    Gen.setMethodBody(lambda, new BeginExp(body));
    classe.getClassExp().addMethod(lambda);

    return lambda;
  }

  private Expression callSuper(Expression thisExp, MonoSymbol[] args,
                               boolean omitDefaults)
  {
    int len = args.length - fields.length;
    List/*Expression*/ superArgs = new LinkedList();
    superArgs.add(thisExp);
    for (int i = 0; i < len; i++)
      {
        if (! (omitDefaults && parameters.hasDefaultValue(i)))
          superArgs.add(args[i].compile());
      }

    Expression superExp = classe.getSuper(index, omitDefaults);
    return new ApplyExp(superExp, (Expression[])
                        superArgs.toArray(new Expression[superArgs.size()]));
  }

  public void printInterface(java.io.PrintWriter s)
  { throw new Error("Should not be called"); }

  String explainWhyMatchFails(Arguments arguments)
  {
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

    //Required fields missing - two different messages depending on whether
    //an explanation of the syntax is necessary
    res = new StringBuffer();
    res.append("Fields of class ").append(name).append(" require initial values.\n");      

    Iterator missingFields = null;
      
    if (arguments.size() == 0)
      {
        res.append(syntaxExample())
          .append("Class ").append(name).append(" has the following fields:\n");
        missingFields = parameters.iterator();
      }
    else 
      {
        res.append("These fields are missing:\n");
        missingFields = arguments.missingNamedArgs(parameters, false).iterator();
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
    
    Iterator params = parameters.getRequiredNamedParameters().iterator();
    int paramCount = 0;
    int len = name.length();
    while(params.hasNext())
      {
        FormalParameters.NamedParameter param = 
          (FormalParameters.NamedParameter)params.next();
        if (paramCount != 0) 
          res.append(", ");
        if (paramCount == 3 && params.hasNext())
          {
            res.append('\n');
            for(int i = 0; i < len; i++) {res.append(' ');}
            res.append("        ");
            paramCount = 0;
          }
        res.append(param.getName()).append(": value");
        paramCount++;
      }
    res.append(")\n\n");
    return res.toString();
  }
}
