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
import nice.tools.typing.PrimitiveType;

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
    this.representation = value.toString();
  }
  
  boolean isZero()
  {
    return ((Number) this.value).intValue() == 0;
  }
 
  public boolean isNumber()
  {
    return this.value instanceof Number;
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
    
    return new ConstantExp(type, tc, object,
	""+value+(isLong ? "L" : ""), location);
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
      long value = parse(removeUnderscores(rep));
      return makeInt(value, isLong, representation.location());
    }
    catch(NumberFormatException e){
      e.printStackTrace();
      User.error(representation, rep + " is not a valid number");
      return null;
    }
  }

  public ConstantExp makeNegative()
  {
    LocatedString newRepres = new LocatedString("-"+representation, location());

    if (value instanceof Float || value instanceof Double)
      return ConstantExp.makeFloating(newRepres);

    return ConstantExp.makeNumber(newRepres);
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

    result = new java.math.BigInteger(rep.substring(index), radix).longValue();

    if (negative) 
      result = -result;

    return result;
  }

  static String removeUnderscores(String s)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i<s.length(); i++)
      if (s.charAt(i) != '_')
        sb.append(s.charAt(i));
    
    return sb.toString();
  }

  public static ConstantExp makeFloating(LocatedString representation)
  {
    String repres = removeUnderscores(representation.toString());
    if (repres.endsWith("F") || repres.endsWith("f"))
      {
	float value = Float.parseFloat(repres);
	return new ConstantExp(PrimitiveType.floatTC, new Float(value), value+"f",
				representation.location());
      }	

    double value = Double.parseDouble(repres);
    return new ConstantExp(PrimitiveType.doubleTC, new Double(value), value+"",
				representation.location());
  }
  
  public long longValue()
  {
    return ((Number)value).longValue();
  }

  public boolean equals(Object other)
  {
    return other instanceof ConstantExp &&
	value.equals(((ConstantExp)other).value);
  }

}
