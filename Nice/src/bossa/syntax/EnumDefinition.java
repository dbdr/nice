/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;
import nice.tools.code.Types;

import gnu.bytecode.*;
import gnu.expr.Declaration;

import java.util.*;

/**
*/
public class EnumDefinition extends Definition
{
  public EnumDefinition(LocatedString name, List/*LocatedString*/ elements,
		List/*MonoSymbol*/ fields, List/*List<Expression>*/ argsList,
		List globalDefs, List/*TypeIdent*/ interfaces)
  {
    super(name, Node.global);
    shortName = name.toString();
    classDef = ClassDefinition.makeClass
	(name,true,false, null, new ArrayList(0),
	new TypeIdent(new LocatedString("nice.lang.Enum",name.location())),
	interfaces,null);
    NiceClass impl = new NiceClass(classDef);
    int fieldsCount = fields.size();
    if (fieldsCount > 0)
      {
        List newFields = new ArrayList(fieldsCount);
        for (Iterator it = fields.iterator(); it.hasNext(); )
           newFields.add(impl.makeField((MonoSymbol)it.next(), null, true, 
		false, false, null));

        impl.setFields(newFields);
      }
    else    
      impl.setFields(null);

    impl.setOverrides(null);

    if (! inInterfaceFile())
      {
        // create the method implementation of family()
	List exps = new LinkedList();
        for (Iterator it = elements.iterator(); it.hasNext(); )
          exps.add(new IdentExp((LocatedString)it.next()));

        Statement body = bossa.syntax.dispatch.createReturnStmt(bossa.syntax.dispatch.createLiteralArrayExp(exps), true);
        LocatedString mName = new LocatedString("family", bossa.util.Location.nowhere());
        Definition mBodyDef = new MethodBodyDefinition(impl, mName, null,
				new LinkedList(), body);
        globalDefs.add(mBodyDef);
      }

    classDef.setImplementation(impl);

    addChild(classDef);    

    this.elements = elements;
    this.fields = fields;
    this.elementsArgs = argsList;
    this.interfaces = interfaces;    

    symbols = new LinkedList();
    for (int ord = 0; ord<elements.size(); ord++ )
      {
        List args = (List) argsList.get(ord);
        LocatedString elemName = (LocatedString)elements.get(ord);
        if (args.size() != fieldsCount)
	  User.error(elemName, "the number of arguments doesn't match the number of enum fields");
        
        Monotype type = new TypeIdent(name);
        type.nullness = Monotype.absent;
        symbols.add(new EnumSymbol(name, elemName, type, ord, fields, args));
      }
    addChildren(symbols);

  }

  class EnumSymbol extends MonoSymbol 
  {
    EnumSymbol(LocatedString enumName, LocatedString name, Monotype type,
	int ordinal, List fields, List argExps)
    {
      super(name, type);
      List args = new ArrayList(2 + fields.size());
      args.add(new Arguments.Argument(new StringConstantExp(name.toString()),
		new LocatedString("name",name.location)));
      Integer val = new Integer(ordinal);
      args.add(new Arguments.Argument(new ConstantExp(PrimitiveType.intTC, val,
		val.toString(), name.location()),
		new LocatedString("ordinal",name.location)));
      for (int i = 0; i < fields.size(); i++)
         args.add(new Arguments.Argument((Expression)argExps.get(i),
		((MonoSymbol)fields.get(i)).getName()));

      this.value = bossa.syntax.dispatch.createNewExp(new TypeIdent(enumName), new Arguments(args));
    }
      
    boolean isAssignable()
    {
      return false;
    }

    Declaration getDeclaration()
    {
      Declaration res = super.getDeclaration();
      
      if (res == null)
        {
          res = new gnu.expr.Declaration
            (name.toString(), Types.javaType(type));
          setDeclaration(res);
          module.addGlobalVar(res, true);
        }
    
      return res;
    }
    
    Expression getValue()
    {
      return value;
    }

    public Definition getDefinition()
    {
      return EnumDefinition.this;
    }
    
    Expression value;
  }

  void resolve()
  {
    for (Iterator it = symbols.iterator(); it.hasNext(); )
      {
        EnumSymbol symbol = (EnumSymbol)it.next();
        symbol.value = bossa.syntax.dispatch.analyse(symbol.value, scope, typeScope);
      }
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/
  
  void typecheck()
  {
    for (Iterator it = symbols.iterator(); it.hasNext(); )
      {
        EnumSymbol symbol = (EnumSymbol)it.next();
        try{
          symbol.value = symbol.value.resolveOverloading(symbol.getType());
          bossa.syntax.dispatch.typecheck(symbol.value);
          Typing.leq(symbol.value.getType(),symbol.getType());
        }
        catch(TypingEx e){
          Internal.error(this,"Typing error in enum:");
        }
      }
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(toString()+"\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    for (Iterator it = symbols.iterator(); it.hasNext(); )
      {
        EnumSymbol symbol = (EnumSymbol)it.next();
        gnu.expr.Declaration declaration = symbol.getDeclaration();
        declaration.setFlag(Declaration.IS_CONSTANT);
        declaration.noteValue(symbol.value.compile());
      }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if (fields.isEmpty())
      return "enum " + shortName + Util.map(" {", " , ", " }", elements);

    String res = "enum " + shortName + Util.map("(", ", ", ")", fields);
    if (interfaces != null)
      res += " implements " + Util.map(""," , ","", interfaces);

    res += " {";
    for (int i = 0; i < elements.size(); i++)
      {
	if (i != 0)
	  res += ", ";

	res += elements.get(i) + Util.map("(", ", ", ")", (List)elementsArgs.get(i));
      }

    return res + "}";
  }

  String shortName;
  ClassDefinition classDef;
  List elements;
  List symbols;
  List fields;
  List elementsArgs;
  List interfaces;
}
