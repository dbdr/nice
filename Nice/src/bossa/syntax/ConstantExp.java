 /**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : ConstantExp.java
// Created : Thu Jul 08 15:36:40 1999 by bonniot
//$Modified: Tue Sep 05 19:18:18 2000 by Daniel Bonniot $

package bossa.syntax;

import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;
import mlsub.typing.MonotypeConstructor;

import nice.tools.code.SpecialTypes;

import bossa.util.*;

/**
   Abstract class for constant values of basic types.
 */
public class ConstantExp extends Expression
{
  ConstantExp()
  {
  }
  
  ConstantExp(TypeConstructor tc, Object value, String representation,
	      Location location)
  {
    this.type = new mlsub.typing.Polytype
      (mlsub.typing.Constraint.True, 
       new MonotypeConstructor(tc,null));
    this.value = value;
    this.representation = representation;
    setLocation(location);
  }
  
  void resolve()
  {
    if(type!=null)
      return;
    
    TypeSymbol s = typeScope.lookup(className);
    
    if(s==null)
      Internal.error("Base class "+className+
		     " was not found in the standard library");

    if(!(s instanceof TypeConstructor))
      Internal.error("Base class "+className+
		     " is not a class !");
    
    TypeConstructor tc = (TypeConstructor) s;
    type = new mlsub.typing.Polytype
      (mlsub.typing.Constraint.True, 
       new MonotypeConstructor(tc,null));
  }

  void computeType()
  {
    //Already done in resolve()
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    if(value == null)
      Internal.warning(this+"["+this.getClass()+" has no value");
    
    return new gnu.expr.QuoteExp(value);
  }
  
  protected String className = "undefined class name";
  
  protected Object value;
  private String representation;
  
  public String toString()
  {
    return representation;
  }

  /****************************************************************
   * Creating constant expressions for the parser
   ****************************************************************/

  public static Expression makeChar(LocatedString value)
  {
    String s = value.toString();
    char c;

  findChar:
    if (s.length() != 0 && s.charAt(0) == '\\')
      // \ escape sequence. See JLS 3.10.6
      {
	if (s.length() == 2)
	  {
	    char c2 = s.charAt(1);
	    switch(c2)
	      {
	      case 'b' : c = '\b'; break findChar;
	      case 't' : c = '\t'; break findChar;
	      case 'n' : c = '\n'; break findChar;
	      case 'f' : c = '\f'; break findChar;
	      case 'r' : c = '\r'; break findChar;
	      case '\"': c = '\"'; break findChar;
	      case '\'': c = '\''; break findChar;
	      case '\\': c = '\\'; break findChar;
	      }
	  }

	try{
	  int code = Integer.parseInt(s.substring(1), 8);
	  if(code<0 || code>255)
	    throw new NumberFormatException();
	  c = (char) code;    
	}
	catch(NumberFormatException e){
	  User.error(value, "Invalid escape sequence: " + value);
	  c = ' '; // compiler complains about c not being initialized...
	}
      }
    else
      {
	if(value.toString().length()!=1)
	  User.error(value, "Invalid character constant: " + value);

	c = value.toString().charAt(0);
      }
  
    return new ConstantExp(primChar, new Character(c), "'" + c + "'",
			   value.location());
  }

  private static Expression makeInt(long value, Location location)
  {
    TypeConstructor type;
    Number object;
    
    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
      {
	type = primByte;
	object = new Byte((byte) value);
      }
    else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
      {
	type = primShort;
	object = new Short((short) value);
      }
    else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
      {
	type = primInt;
	object = new Integer((int) value);
      }
    else
      {
	User.error(location, value + " is not in the range of int values");
	return null;
      }
    
    return new ConstantExp(type, object, value+"", location);
  }
  
  public static Expression makeNumber(LocatedString representation)
  {
    String rep = representation.toString();

    try{
      long value = Long.decode(rep).longValue();
      return makeInt(value, representation.location());
    }
    catch(NumberFormatException e){
      User.error(representation, rep + " is not a valid number");
      return null;
    }
  }
  
  public static Expression makeDouble(double value, Location location)
  {
    return new ConstantExp(primFloat, new Double(value), value+"",
			   location);
  }
  
  public static Expression makeFloating(LocatedString representation)
  {
    return makeDouble(Double.parseDouble(representation.toString()), representation.location());
  }
  
  static gnu.bytecode.Type registerPrimType(String name, TypeConstructor tc)
  {
    if(name.equals("nice.lang.char"))
      {
	primChar = tc;
	charType = new MonotypeConstructor(tc, null);
	return SpecialTypes.charType;
      }
    
    if(name.equals("nice.lang.byte"))
      {
	primByte = tc;
	byteType = new MonotypeConstructor(tc, null);
	return SpecialTypes.byteType;
      }
    
    if(name.equals("nice.lang.int"))
      {
	primInt = tc;
	intType = new MonotypeConstructor(tc, null);
	intPolytype = new mlsub.typing.Polytype(intType);
	return SpecialTypes.intType;
      }
    
    if(name.equals("nice.lang.long"))
      {
	primLong = tc;
	longType = new MonotypeConstructor(tc, null);
	return SpecialTypes.longType;
      }
    
    if(name.equals("nice.lang.boolean"))
      {
	primBool = tc;
	boolType = new MonotypeConstructor(primBool, null);
	boolPolytype = new mlsub.typing.Polytype(boolType);
	return SpecialTypes.booleanType;
      }
    
    if(name.equals("nice.lang.short"))
      {
	primShort = tc;
	shortType = new MonotypeConstructor(tc, null);
	return SpecialTypes.shortType;
      }
    
    if(name.equals("nice.lang.double"))
      {
	primDouble = tc;
	doubleType = new MonotypeConstructor(tc, null);
	return SpecialTypes.doubleType;
      }
    
    if(name.equals("nice.lang.float"))
      {
	primFloat = tc;
	floatType = new MonotypeConstructor(tc, null);
	return SpecialTypes.floatType;
      }
    
    if(name.equals("nice.lang.void"))
      {
	voidType = new MonotypeConstructor(tc, null);
	voidPolytype = new mlsub.typing.Polytype
	  (mlsub.typing.Constraint.True, voidType);
	synVoidType = Monotype.create(voidType);
	return SpecialTypes.voidType;
      }
    
    return null;
  }
  
  public static TypeConstructor primByte, primChar, primInt, primLong, primBool, primShort, primDouble, primFloat, arrayTC;
  public static mlsub.typing.Monotype byteType, charType, intType, longType, boolType, shortType, doubleType, floatType, voidType;
  static mlsub.typing.Polytype voidPolytype, boolPolytype, intPolytype;

  // syntatic types
  static Monotype synVoidType;
}
