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

/**
   A type used as an expression.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

class TypeConstantExp extends ConstantExp
{
  TypeConstantExp(LocatedString name)
  {
    super(PrimitiveType.typeTC, name, name.toString(), name.location());
  }

  private TypeConstantExp(LocatedString name, gnu.bytecode.Type type)
  {
    this(name);
    this.value = type;
  }

  /**
     @return an Expression representing ident as a type or package literal.
  */
  static Expression create(LocatedString ident)
  {
    return create(null, ident);
  }
  
  /**
     @return an Expression representing [root].[name]
  */
  static Expression create(PackageExp root, LocatedString name)
  {
    String fullName = name.toString();
    if (root != null)
      fullName = root.name.append(".").append(fullName).toString();
    
    mlsub.typing.TypeConstructor tc = 
      Node.getGlobalTypeScope().globalLookup(fullName, name.location());

    if(tc != null)
      {
	gnu.bytecode.Type type = nice.tools.code.Types.javaType(tc);
	// type might not be a class
	// for instance if the ident was "int"
	if (type instanceof gnu.bytecode.ClassType)
	  {
	    Expression res = new TypeConstantExp(name, type);
	    res.setLocation(root == null ? name.location() : root.location());
	    return res;
	  }
      }

    if (root != null)
      // name has been appended to root's name
      return root;

    root = new PackageExp(fullName);
    root.setLocation(name.location());
    return root;
  }
  
  gnu.bytecode.ClassType staticClass()
  {
    return (gnu.bytecode.ClassType) value;
  }
  
  mlsub.typing.TypeConstructor representedType;
}
