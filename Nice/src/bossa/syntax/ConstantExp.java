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
//$Modified: Mon Jul 24 20:19:47 2000 by Daniel Bonniot $

package bossa.syntax;

import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;

import bossa.util.*;

/**
 * Abstract class for constant values of basic types.
 *
 * Numeric types come from {@link gnu.math gnu.math}.
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
       new mlsub.typing.MonotypeConstructor(tc,null));
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
       new mlsub.typing.MonotypeConstructor(tc,null));
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
    
    return new ConstantExp(primChar, gnu.text.Char.make(c), "'" + c + "'",
			   value.location());
  }

  public static Expression makeNumber(int value)
  {
    return new ConstantExp(primInt, gnu.math.IntNum.make(value), value+"",
			   Location.nowhere());
  }
  
  public static Expression makeNumber(LocatedString representation)
  {
    String rep = representation.toString();

    // Is in a valid int ?
    try{
      int i = Integer.decode(rep).intValue();
      
      TypeConstructor type = primInt;
      
      if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE)
	type = primByte;
      else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE)
	type = primShort;
      
      return new ConstantExp(type, gnu.math.IntNum.make(i), 
			     i+"", representation.location());
    }
    catch(NumberFormatException e){
    }

    User.error(representation,
	       rep+" is not a valid number");
    return null;
  }
  
  public static Expression makeDouble(double value, Location location)
  {
    return new ConstantExp(primFloat, gnu.math.DFloNum.make(value), value+"",
			   location);
  }
  
  static gnu.bytecode.Type registerPrimType(String name, TypeConstructor tc)
  {
    if(name.equals("nice.lang.char"))
      {
	primChar = tc;
	return bossa.SpecialTypes.charType;
      }
    
    if(name.equals("nice.lang.byte"))
      {
	primByte = tc;
	return bossa.SpecialTypes.byteType;
      }
    
    if(name.equals("nice.lang.int"))
      {
	primInt = tc;
	return 
	  bossa.SpecialTypes.intType;
      }
    
    if(name.equals("nice.lang.long"))
      {
	primLong = tc;
	return 
	  bossa.SpecialTypes.longType;
      }
    
    if(name.equals("nice.lang.boolean"))
      {
	primBool = tc;
	boolType = new mlsub.typing.MonotypeConstructor(primBool, null);
	boolPolytype = new mlsub.typing.Polytype(boolType);
	return bossa.SpecialTypes.booleanType;
      }
    
    if(name.equals("nice.lang.short"))
      {
	primShort = tc;
	return bossa.SpecialTypes.shortType;
      }
    
    if(name.equals("nice.lang.double"))
      {
	primDouble = tc;
	return bossa.SpecialTypes.doubleType;
      }
    
    if(name.equals("nice.lang.float"))
      {
	primFloat = tc;
	return bossa.SpecialTypes.floatType;
      }
    
    if(name.equals("nice.lang.void"))
      {
	voidType = new mlsub.typing.MonotypeConstructor(tc, null);
	voidPolytype = new mlsub.typing.Polytype
	  (mlsub.typing.Constraint.True, voidType);
	synVoidType = Monotype.create(voidType);
	return bossa.SpecialTypes.voidType;
      }
    
    return null;
  }
  
  public static TypeConstructor primByte, primChar, primInt, primLong, primBool, 
    primShort, primDouble, primFloat, arrayTC;
  public static mlsub.typing.Monotype voidType, boolType;
  static mlsub.typing.Polytype voidPolytype, boolPolytype;
  static Monotype synVoidType;
}
