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

package bossa.syntax;

import gnu.bytecode.Type;
import mlsub.typing.Interface;
import mlsub.typing.TypeConstructor;

/**
   Abstract syntax for a type definition.

 */
public abstract class TypeDefinition extends MethodContainer
{
  TypeDefinition(LocatedString name, int propagate, Constraint classConstraint)
  {
    super(name, propagate, classConstraint);
  }
	
  public abstract ClassImplementation getImplementation( );
  public abstract void setImplementation( ClassImplementation impl );
	
  public abstract void precompile();
  public abstract void typecheckClass();
  public abstract void recompile();
	
  public abstract mlsub.typing.Polytype getConstrainedType();

  public abstract mlsub.typing.Interface getAssociatedInterface();
    
  public abstract TypeScope getLocalScope();
    
  public abstract TypeConstructor getTC();

  public abstract void setJavaType(Type javaType);
    
  public abstract Type getJavaType();
   
  public abstract boolean isConcrete();

  public abstract void addInterfaceImplementation(Interface interfaceITF);
}
