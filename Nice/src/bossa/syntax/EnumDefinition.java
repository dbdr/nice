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
  public EnumDefinition(LocatedString name, List/*LocatedString*/ elements)
  {
    super(name, Node.global);
    shortName = name.toString();
    classDef = ClassDefinition.makeClass
	(name,true,false, null, new ArrayList(0),null,null,null);
    NiceClass impl = new NiceClass(classDef);
    impl.setFields(new ArrayList(0));
    classDef.setImplementation(impl);

    addChild(classDef);    

    this.elements = elements;
    
    symbols = new LinkedList();
    for (Iterator it = elements.iterator(); it.hasNext(); )
      {
        Monotype type = new TypeIdent(name);
        type.nullness = Monotype.absent;
        symbols.add(new EnumSymbol(name, (LocatedString)it.next(), type));
      }
    addChildren(symbols);
    
  }

  class EnumSymbol extends MonoSymbol 
  {
    EnumSymbol(LocatedString enumName, LocatedString name, Monotype type)
    {
      super(name, type);
      this.value = new NewExp(new TypeIdent(enumName), Arguments.noArguments());
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
          res = module.addGlobalVar(name.toString(), Types.javaType(type), true);
          setDeclaration(res);
        }
    
      return res;
    }
    
    Expression getValue()
    {
      return value;
    }

    Definition getDefinition()
    {
      return EnumDefinition.this;
    }
    
    Expression value;
  }

  public Collection associatedDefinitions()
  {
    return null;
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
    return "enum " + shortName + Util.map(" {", " , ", " }", elements);
  }

  String shortName;
  ClassDefinition classDef;
  List elements;
  List symbols;
}
