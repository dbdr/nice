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

import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;
import mlsub.typing.MonotypeConstructor;

import nice.tools.code.SpecialTypes;

import bossa.util.*;

/**
   Constant expressions (values) of basic types.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class ConstantExp extends Expression
{
  ConstantExp()
  {
  }
  
  ConstantExp(TypeConstructor tc, Object value, String representation,
	      Location location)
  {
    // XXX pass a polytype instead
    this.type = new mlsub.typing.Polytype
      (mlsub.typing.Constraint.True, 
       Monotype.sure(new MonotypeConstructor(tc,null)));
    this.value = value;
    this.representation = representation;
    setLocation(location);
  }
  
  void computeType()
  {
    //Already done during resolution
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
  
  protected LocatedString className = null;
  
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
	if(s.length()!=1)
	  User.error(value, "Invalid character constant: " + value);

	c = s.charAt(0);
      }
  
    return new ConstantExp(primChar, new Character(c), "'" + c + "'",
			   value.location());
  }

  private static Expression makeInt(long value, boolean isLong, 
				    Location location)
  {
    TypeConstructor type;
    Number object;
    
    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE && !isLong)
      {
	type = primByte;
	object = new Byte((byte) value);
      }
    else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE && !isLong)
      {
	type = primShort;
	object = new Short((short) value);
      }
    else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE && !isLong)
      {
	type = primInt;
	object = new Integer((int) value);
      }
    else
      {
	type = primLong;
	object = new Long(value);
      }
    
    return new ConstantExp(type, object, value+"", location);
  }
  
  public static Expression makeNumber(LocatedString representation)
  {
    String rep = representation.toString();

    int lastCharIndex = rep.length() - 1;
    char last = rep.charAt(lastCharIndex);
    boolean isLong = last == 'l' || last == 'L';
    if (isLong)
      rep = rep.substring(0, lastCharIndex);

    try{
      //long value = Long.decode(rep).longValue();
      long value = parse(rep);
      return makeInt(value, isLong, representation.location());
    }
    catch(NumberFormatException e){
      e.printStackTrace();
      User.error(representation, rep + " is not a valid number");
      return null;
    }
  }
  
  private static long parse(String rep) throws NumberFormatException 
  {
    int radix = 10;
    int index = 0;
    boolean negative = false;
    long result;

    // Leading minus
    if (rep.startsWith("-")) {
      negative = true;
      index++;
    }

    // Radix specifier
    if (rep.startsWith("0x", index) || rep.startsWith("0X", index)) 
      {
	index += 2;
	radix = 16;
      }
    else if (rep.startsWith("#", index)) 
      {
	index++;
	radix = 16;
      }
    else if (rep.startsWith("0", index) && rep.length() > 1 + index) 
      {
	index++;
	radix = 8;
      }

    if (rep.startsWith("-", index))
      throw new NumberFormatException("Negative sign in wrong position");

    try {
      result = Long.parseLong(rep.substring(index), radix);
      if (negative) 
	result = -result;
    } catch (NumberFormatException e) {
      // Handle the case Long.MIN_VALUE:
      // the absolute value overflows, but it should be valid
      String constant = negative ? new String("-" + rep.substring(index))
	: rep.substring(index);
      result = Long.parseLong(constant, radix);
    }
    return result;
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
  
  public static Expression makeString(LocatedString representation)
  {
    StringConstantExp res = new StringConstantExp(representation.toString());
    res.setLocation(representation.location());
    return res;
  }
  
  public static Expression makeType(LocatedString representation)
  {
    return new TypeConstantExp(representation);
  }
  
  /****************************************************************
   * Primitive Types
   ****************************************************************/

  static gnu.bytecode.Type registerPrimType(String name, TypeConstructor tc)
  {
    if(name.equals("nice.lang.char"))
      {
	primChar = tc;
	charType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.charType;
      }
    
    if(name.equals("nice.lang.byte"))
      {
	primByte = tc;
	byteType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.byteType;
      }
    
    if(name.equals("nice.lang.int"))
      {
	primInt = tc;
	intType = Monotype.sure(new MonotypeConstructor(tc, null));
	intPolytype = new mlsub.typing.Polytype(intType);
	return SpecialTypes.intType;
      }
    
    if(name.equals("nice.lang.long"))
      {
	primLong = tc;
	longType = Monotype.sure(new MonotypeConstructor(tc, null));
	longPolytype = new mlsub.typing.Polytype(longType);
	return SpecialTypes.longType;
      }
    
    if(name.equals("nice.lang.boolean"))
      {
	primBool = tc;
	boolType = Monotype.sure(new MonotypeConstructor(primBool, null));
	boolPolytype = new mlsub.typing.Polytype(boolType);
	return SpecialTypes.booleanType;
      }
    
    if(name.equals("nice.lang.short"))
      {
	primShort = tc;
	shortType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.shortType;
      }
    
    if(name.equals("nice.lang.double"))
      {
	primDouble = tc;
	doubleType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.doubleType;
      }
    
    if(name.equals("nice.lang.float"))
      {
	primFloat = tc;
	floatType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.floatType;
      }
    
    if(name.equals("nice.lang.void"))
      {
	voidType = Monotype.sure(new MonotypeConstructor(tc, null));
	voidPolytype = new mlsub.typing.Polytype
	  (mlsub.typing.Constraint.True, voidType);
	synVoidType = Monotype.create(voidType);
	return SpecialTypes.voidType;
      }
    
    if (name.equals("nice.lang.Array"))
      {
	arrayTC = tc;
	return nice.tools.code.SpecialArray.wrappedType();
      }

    if (name.equals("nice.lang.Maybe"))
      {
	maybeTC = tc;
	// Reset the cached type of Null, since it is a persistent expression.
	NullExp.instance.computeType();
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }
    
    if (name.equals("nice.lang.Sure"))
      {
	sureTC = tc;
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }
    
    if (name.equals("nice.lang.Null"))
      {
	nullTC = tc;
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }
    
    if (name.equals("nice.lang.Type"))
      {
	typeTC = tc;
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }

    return null;
  }
  
  public static TypeConstructor primByte, primChar, primInt, primLong, primBool, primShort, primDouble, primFloat, arrayTC;
  public static mlsub.typing.Monotype byteType, charType, intType, longType, boolType, shortType, doubleType, floatType, voidType;
  static mlsub.typing.Polytype voidPolytype, boolPolytype, intPolytype, longPolytype;

  public static TypeConstructor maybeTC, sureTC, nullTC;

  // syntatic types
  public static Monotype synVoidType;

  static TypeConstructor typeTC;
  static TypeConstructor throwableTC;
  static TypeConstructor throwableTC()
  {
    return throwableTC;
  }
	
  private static mlsub.typing.Polytype throwableType;
  static mlsub.typing.Polytype throwableType()
  {
    if (throwableType == null)
      {
	throwableType = new mlsub.typing.Polytype
	  (mlsub.typing.Constraint.True, 
	   Monotype.sure(new MonotypeConstructor(throwableTC(), null)));
      }
    return throwableType;
  }
}
