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

import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import gnu.expr.QuoteExp;

import bossa.util.*;

/**
   Constant expressions (values) of basic types.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public class ConstantExp extends Expression
{
  ConstantExp()
  {
  }
  
  ConstantExp(Polytype type, TypeConstructor tc, Object value, String representation, 
	      Location location)
  {
    this.type = type;
    this.tc = tc;
    this.value = value;
    this.representation = representation;
    setLocation(location);
  }
  
  ConstantExp(TypeConstructor tc, Object value, String representation,
	      Location location)
  {
    this(new Polytype(Monotype.sure(new MonotypeConstructor(tc,null))),
	 tc, value, representation, location);
  }
  
  ConstantExp(TypeConstructor tc, String representation, Location location)
  {
    this(tc, null, representation, location);
  }

  public ConstantExp(Object value)
  {
    this.value = value;
  }
  
  boolean isZero()
  {
    return ((Number) this.value).intValue() == 0;
  }

  void computeType()
  {
    // The type is known at creation.
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    if(value == null)
      Internal.warning(this+"["+this.getClass()+" has no value");
    if (value instanceof VarSymbol)
      return ((VarSymbol)value).compile();

    return new QuoteExp(value, nice.tools.code.Types.javaType(type));
  }
  
  protected LocatedString className = null;
  
  public Object value;
  private String representation;
  public TypeConstructor tc;  

  public String toString()
  {
    return representation;
  }

  /****************************************************************
   * Creating constant expressions for the parser
   ****************************************************************/

  public static ConstantExp makeChar(LocatedString value)
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
	  throw User.error(value, "Invalid escape sequence: " + value);
	}
      }
    else
      {
	if(s.length()!=1)
	  User.error(value, "Invalid character constant: " + value);

	c = s.charAt(0);
      }
  
    return new ConstantExp(PrimitiveType.charTC, new Character(c), 
			   "'" + c + "'", value.location());
  }

  private static ConstantExp makeInt(long value, boolean isLong, 
				    Location location)
  {
    Polytype type;
    TypeConstructor tc;
    Number object;
    
    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE && !isLong)
      {
	type = PrimitiveType.bytePolytype;
        tc = PrimitiveType.byteTC;
	object = new Byte((byte) value);
      }
    else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE && !isLong)
      {
	type = PrimitiveType.shortPolytype;
        tc = PrimitiveType.shortTC;
	object = new Short((short) value);
      }
    else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE && !isLong)
      {
	type = PrimitiveType.intPolytype;
        tc = PrimitiveType.intTC;
	object = new Integer((int) value);
      }
    else
      {
	type = PrimitiveType.longPolytype;
        tc = PrimitiveType.longTC;
	object = new Long(value);
      }
    
    return new ConstantExp(type, tc, object, value+"", location);
  }
  
  public static ConstantExp makeNumber(LocatedString representation)
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

  public static ConstantExp makeDouble(double value, Location location)
  {
    return new ConstantExp(PrimitiveType.floatTC, new Double(value), value+"",
			   location);
  }
  
  public static ConstantExp makeFloating(LocatedString representation)
  {
    return makeDouble(Double.parseDouble(representation.toString()), representation.location());
  }
  
  public static ConstantExp makeString(LocatedString representation)
  {
    StringConstantExp res = new StringConstantExp(representation.toString());
    res.setLocation(representation.location());
    return res;
  }
  
  public static ConstantExp makeType(LocatedString representation)
  {
    return new TypeConstantExp(representation);
  }

  /****************************************************************
   * Booleans
   ****************************************************************/

  public static ConstantExp makeBoolean(boolean value, Location location)
  {
    return new ConstantExp.Boolean(value, location);
  }

  private static class Boolean extends ConstantExp
  {
    Boolean(boolean value, Location location)
    {
      super(PrimitiveType.boolTC, value ? "true" : "false", location);
      compiledValue = value ? QuoteExp.trueExp : QuoteExp.falseExp;
    }

    boolean isFalse()
    {
      return compiledValue == QuoteExp.falseExp;
    }

    boolean isTrue()
    {
      return compiledValue == QuoteExp.trueExp;
    }

    protected gnu.expr.Expression compile()
    {
      return compiledValue;
    }

    private QuoteExp compiledValue;
  }

  public long longValue()
  {
    if (value instanceof Character)
      return ((Character)value).charValue();

    return ((Number)value).longValue();
  }

  public boolean equals(Object other)
  {
    return other instanceof ConstantExp &&
	value.equals(((ConstantExp)other).value);
  }

}
