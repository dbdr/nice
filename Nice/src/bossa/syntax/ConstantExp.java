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
//$Modified: Tue Mar 14 11:52:38 2000 by Daniel Bonniot $

package bossa.syntax;

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
    this.type = new Polytype(new MonotypeConstructor(tc,null,null));
    this.value = value;
    this.representation = representation;
    setLocation(location);
  }
  
  void resolve()
  {
    if(type!=null)
      return;
    
    TypeSymbol s=typeScope.lookup(className);
    
    if(s==null)
      Internal.error("Base class "+className+
		     " was not found in the standard library");

    if(!(s instanceof TypeConstructor))
      Internal.error("Base class "+className+
		     " is not a class !");
    
    TypeConstructor tc=(TypeConstructor) s;
    type=new Polytype(new MonotypeConstructor(tc,null,null));
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
    if(value.toString().length()!=1)
      User.error(value, "Invalid character constant: "+value);

    char c = value.toString().charAt(0);
    return new ConstantExp(primChar, gnu.kawa.util.Char.make(c), "'"+c+"'",
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

    // Does it fit in an int ?
    try{
      int i = Integer.decode(rep).intValue();
      return new ConstantExp(primInt, gnu.math.IntNum.make(i), i+"",
			     representation.location());
    }
    catch(NumberFormatException e){
    }

    User.error(representation,
	       rep+" is not a valid number");
    return null;
  }
  
  static gnu.bytecode.Type registerPrimType(String name, TypeConstructor tc)
  {
    if(name.equals("bossa.lang.char"))
      {
	primChar = tc;
	return bossa.SpecialTypes.charType;
      }
    
    if(name.equals("bossa.lang.byte"))
      {
	primByte = tc;
	return bossa.SpecialTypes.byteType;
      }
    
    if(name.equals("bossa.lang.int"))
      {
	primInt = tc;
	return 
	  bossa.SpecialTypes.intType;
      }
    
    if(name.equals("bossa.lang.long"))
      {
	primLong = tc;
	return 
	  bossa.SpecialTypes.longType;
      }
    
    if(name.equals("bossa.lang.boolean"))
      {
	primBool = tc;
	boolType = new MonotypeConstructor(primBool, null, tc.location());
	return bossa.SpecialTypes.booleanType;
      }
    
    if(name.equals("bossa.lang.short"))
      {
	primShort = tc;
	return bossa.SpecialTypes.shortType;
      }
    
    if(name.equals("bossa.lang.double"))
      {
	primDouble = tc;
	return bossa.SpecialTypes.doubleType;
      }
    
    if(name.equals("bossa.lang.float"))
      {
	primFloat = tc;
	return bossa.SpecialTypes.floatType;
      }
    
    if(name.equals("bossa.lang.void"))
      {
	voidType = new MonotypeConstructor(tc, null, tc.location());
	return bossa.SpecialTypes.voidType;
      }
    
    return null;
  }
      
  static TypeConstructor primByte, primChar, primInt, primLong, primBool, 
    primShort, primDouble, primFloat, arrayTC;
  static Monotype voidType, boolType;
}
