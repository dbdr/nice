/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.typing;

import bossa.util.*;
import java.util.*;

import mlsub.typing.*;

import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;

import nice.tools.code.SpecialTypes;

/**
   
*/
public final class PrimitiveType
{

  public static gnu.bytecode.Type register(String name, TypeConstructor tc)
  {
    if(name.equals("nice.lang.char"))
      {
	charTC = tc;
	charType = Types.sureMonotype(new MonotypeConstructor(tc, null));
        charPolytype = new Polytype(charType);
	return SpecialTypes.charType;
      }
    
    if(name.equals("nice.lang.byte"))
      {
	byteTC = tc;
	byteType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	bytePolytype = new Polytype(byteType);
	return SpecialTypes.byteType;
      }
    
    if(name.equals("nice.lang.short"))
      {
	shortTC = tc;
	shortType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	shortPolytype = new Polytype(shortType);
	return SpecialTypes.shortType;
      }
    
    if(name.equals("nice.lang.int"))
      {
	intTC = tc;
	intType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	intPolytype = new Polytype(intType);
	return SpecialTypes.intType;
      }
    
    if(name.equals("nice.lang.long"))
      {
	longTC = tc;
	longType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	longPolytype = new Polytype(longType);
	return SpecialTypes.longType;
      }
    
    if(name.equals("nice.lang.boolean"))
      {
	boolTC = tc;
	boolType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	boolPolytype = new Polytype(boolType);
	return SpecialTypes.booleanType;
      }
    
    if(name.equals("nice.lang.double"))
      {
	doubleTC = tc;
	doubleType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	doublePolytype = new Polytype(doubleType);
	return SpecialTypes.doubleType;
      }
    
    if(name.equals("nice.lang.float"))
      {
	floatTC = tc;
	floatType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	floatPolytype = new Polytype(floatType);
	return SpecialTypes.floatType;
      }
    
    if(name.equals("nice.lang.void"))
      {
	voidTC = tc;
	mlsub.typing.lowlevel.Engine.setTop(tc);
	voidType = Types.sureMonotype(new MonotypeConstructor(tc, null));
	voidPolytype = new Polytype(Constraint.True, voidType);
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
	mlsub.typing.NullnessKind.setMaybe(tc);
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }
    
    if (name.equals("nice.lang.Sure"))
      {
	sureTC = tc;
	mlsub.typing.NullnessKind.setSure(tc);
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
	// to differ with the null result, which signals error
	return gnu.bytecode.Type.pointer_type;
      }

    return null;
  }
  
  public static TypeConstructor byteTC, charTC, intTC, longTC, boolTC, shortTC, doubleTC, floatTC, arrayTC, voidTC;

  public static mlsub.typing.Monotype byteType, charType, intType, longType, boolType, shortType, doubleType, floatType, voidType;
  public static Polytype voidPolytype, boolPolytype, charPolytype ,bytePolytype, shortPolytype, intPolytype, longPolytype, doublePolytype, floatPolytype;

  private static Polytype objectPolytype;
  public static Polytype objectPolytype()
  {
    if (objectPolytype == null)
      objectPolytype = new Polytype(mlsub.typing.Constraint.True, 
                                    Types.sureMonotype(TopMonotype.instance));

    return objectPolytype;
  }

  public static void reset() { objectPolytype = null; }

  public static TypeConstructor maybeTC, sureTC, nullTC;

  public static TypeConstructor classTC;
  public static TypeConstructor collectionTC;
  public static TypeConstructor throwableTC;
}