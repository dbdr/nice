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

import bossa.util.*;
import java.util.*;

import mlsub.typing.*;

import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;

import nice.tools.code.SpecialTypes;

/**
   A primitive type.
   
   @version $Date$
   @author Daniel Bonniot
*/

public class PrimitiveType extends ClassImplementation
{
  public PrimitiveType(TypeDefinition definition)
  {
    gnu.bytecode.Type t = registerPrimType(definition.name.toString(), definition.getTC());
    if (t == null)
      User.error(definition, definition.name + " is not a known primitive type");
    definition.setJavaType(t);
  }

  void resolveClass()
  {
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(" = native ;\n");
  }

  static gnu.bytecode.Type registerPrimType(String name, TypeConstructor tc)
  {
    if(name.equals("nice.lang.char"))
      {
	charTC = tc;
	charType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.charType;
      }
    
    if(name.equals("nice.lang.byte"))
      {
	byteTC = tc;
	byteType = Monotype.sure(new MonotypeConstructor(tc, null));
	bytePolytype = new Polytype(byteType);
	return SpecialTypes.byteType;
      }
    
    if(name.equals("nice.lang.short"))
      {
	shortTC = tc;
	shortType = Monotype.sure(new MonotypeConstructor(tc, null));
	shortPolytype = new Polytype(shortType);
	return SpecialTypes.shortType;
      }
    
    if(name.equals("nice.lang.int"))
      {
	intTC = tc;
	intType = Monotype.sure(new MonotypeConstructor(tc, null));
	intPolytype = new Polytype(intType);
	return SpecialTypes.intType;
      }
    
    if(name.equals("nice.lang.long"))
      {
	longTC = tc;
	longType = Monotype.sure(new MonotypeConstructor(tc, null));
	longPolytype = new Polytype(longType);
	return SpecialTypes.longType;
      }
    
    if(name.equals("nice.lang.boolean"))
      {
	boolTC = tc;
	boolType = Monotype.sure(new MonotypeConstructor(tc, null));
	boolPolytype = new Polytype(boolType);
	return SpecialTypes.booleanType;
      }
    
    if(name.equals("nice.lang.double"))
      {
	doubleTC = tc;
	doubleType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.doubleType;
      }
    
    if(name.equals("nice.lang.float"))
      {
	floatTC = tc;
	floatType = Monotype.sure(new MonotypeConstructor(tc, null));
	return SpecialTypes.floatType;
      }
    
    if(name.equals("nice.lang.void"))
      {
	voidTC = tc;
	mlsub.typing.lowlevel.Engine.setTop(tc);
	voidType = Monotype.sure(new MonotypeConstructor(tc, null));
	voidPolytype = new Polytype(Constraint.True, voidType);
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
  static Polytype voidPolytype, boolPolytype, bytePolytype, shortPolytype, intPolytype, longPolytype;

  private static Polytype objectPolytype;
  static Polytype objectPolytype()
  {
    if (objectPolytype == null)
      objectPolytype = new Polytype(mlsub.typing.Constraint.True, 
                                    Monotype.sure(TopMonotype.instance));

    return objectPolytype;
  }

  public static void reset() { objectPolytype = null; }

  public static TypeConstructor maybeTC, sureTC, nullTC;

  // syntatic types
  public static Monotype synVoidType;

  public static TypeConstructor classTC;
  static TypeConstructor collectionTC;
  static TypeConstructor throwableTC;

  private static Polytype throwableType;
  static Polytype throwableType()
  {
    if (throwableType == null)
      {
	throwableType = new Polytype
	  (Constraint.True, 
	   Monotype.sure(new MonotypeConstructor(throwableTC, null)));
      }
    return throwableType;
  }
}
