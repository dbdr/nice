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
  Constructor(NiceClass classe, NiceClass.Field[] fields, 
              MethodDeclaration parent,
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
    this.parent = parent;

    mlsub.typing.Polytype type = new mlsub.typing.Polytype
      (getType().getConstraint(), 
       new mlsub.typing.FunType(getArgTypes(), PrimitiveType.voidType));
    classe.addConstructorCallSymbol
      (new MethodDeclaration.Symbol(name, type) {
          gnu.expr.Expression compileInCallPosition()
          {
            getCode();
            return initializeFromConstructor;
          }
        });
  }

  private NiceClass classe;
  private NiceClass.Field[] fields;
  private MethodDeclaration parent;

  Expression getConstructorInvocation(boolean omitDefaults)
  {
    getCode();
    return 
      // lambdaOmitDefaults is null if the two versions are identical
      omitDefaults && initializeOmitDefaults != null 
      ? initializeOmitDefaults : initialize;
  }

  /** Call the constructor, with all the arguments. */
  private Expression initialize;

  /** Call the constructor, with all the arguments, with an implicit this argument. */
  private Expression initializeFromConstructor;

  /** Call the constructor, with only non-default arguments. */
  private Expression initializeOmitDefaults;

  /** Instantiate the class, calling the constructor with all the arguments. */
  private Expression instantiate;

  protected Expression computeCode()
  {
    createBytecode(true);
    createBytecode(false);
    return instantiate;
  }

  /**
     @param omitDefaults if true, do not take the value of fields with
            default values as parameters, but use that default instead.
  */
  private void createBytecode(boolean omitDefaults)
  {
    ClassType thisType = (ClassType) javaReturnType();
    Declaration thisDecl = new Declaration("this");
    thisDecl.setType(thisType);
    Expression thisExp = new ThisExp(thisDecl);

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
      return;

    Type[] argTypesArray = (Type[]) argTypes.toArray(new Type[argTypes.size()]);
    MonoSymbol[] argsArray = (MonoSymbol[]) args.toArray(new MonoSymbol[args.size()]);

    if (classe.definition.inInterfaceFile())
      throw new Error("Constructors are loaded from the compiled package");

    ConstructorExp lambda = Gen.createConstructor
      (thisDecl, argTypesArray, argsArray);
    lambda.setSuperCall(callSuper(thisExp, fullArgs, omitDefaults));

    Gen.setMethodBody(lambda, body(thisExp, fullArgs, omitDefaults));
    classe.getClassExp().addMethod(lambda);
    if (! omitDefaults)
      lambda.addBytecodeAttribute(parameters.asBytecodeAttribute());

    if (omitDefaults)
      {
        initializeOmitDefaults = new QuoteExp(new InitializeProc(lambda));
      }
    else
      {
        initialize = new QuoteExp(new InitializeProc(lambda));
        initializeFromConstructor = new QuoteExp(new InitializeProc(lambda, true));
        instantiate = new QuoteExp(new InstantiateProc(lambda));
      }
  }

  private static gnu.expr.Expression objectConstructor =
    new gnu.expr.QuoteExp
    (new gnu.expr.InitializeProc
     (gnu.bytecode.Type.pointer_type.getDeclaredMethod("<init>", 0)));

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

    // A null parent means no parent class: call the Object constructor.
    Expression superExp = parent == null ? 
      objectConstructor : parent.getConstructorInvocation(omitDefaults);
    return new ApplyExp(superExp, (Expression[])
                        superArgs.toArray(new Expression[superArgs.size()]));
  }

  private Expression body(Expression thisExp,
                          MonoSymbol[] fullArgs, boolean omitDefaults)
  {
    int len = fields.length + classe.nbInitializers();

    if (len == 0)
      return QuoteExp.voidExp;

    Expression[] body = new Expression[len];

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

        body[i] = fields[i].method.compileAssign(thisExp, fieldValue);
      }

    classe.setThisExp(thisExp);
    for (int i = 0; i < classe.nbInitializers(); i++)
      body[fields.length + i] = classe.compileInitializer(i);

    return new BeginExp(body);
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
