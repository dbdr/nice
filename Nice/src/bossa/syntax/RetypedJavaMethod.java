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

import gnu.bytecode.*;

import nice.tools.code.Types;
import nice.tools.code.TypeImport;

import java.util.*;

/**
   A Java method which is explicitely given a more precise type
   in the Nice source program.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class RetypedJavaMethod extends JavaMethod
{
  /**
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public RetypedJavaMethod
    (
     // Informations about the java method
     // These 3 args are null if we are in an interface file
     LocatedString className,
     String methodName,
     List /* of LocatedString */ javaTypes,
     // Nice information
     LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     FormalParameters parameters
    )
  {
    super(name,constraint,returnType,parameters);
    
    this.className = className;
    this.methodName = methodName;
    this.javaTypes = javaTypes;
  }

  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    findReflectMethod();    
    
    super.buildScope(outer, typeOuter);
  }
  
  /** The java class this method is defined in */
  LocatedString className;

  /** Its name in the java class */
  private String methodName;
  
  private List /* of LocatedString */ javaTypes;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private static gnu.bytecode.Type type(LocatedString s)
  {
    Type res = Types.type(s);
    if(res == null)
      User.error(s, "Unknown java class " + s);
    return res;
  }
  
  private Type javaRetType;
  private Type[] javaArgType;
  
  public gnu.bytecode.Type javaReturnType() 
  { return javaRetType; }

  public gnu.bytecode.Type[] javaArgTypes()
  { return javaArgType; }

  private void findReflectMethod()
  {
    if(reflectMethod != null)
      return;
    
    Type holderType = TypeImport.lookup(className);
    if (holderType == null)
      User.error(this,
		 "Class " + className + " was not found");
    
    if (!(holderType instanceof ClassType))
      User.error(className, className + " is a primitive type");
    ClassType holder = (ClassType) holderType;
    
    className = new LocatedString(holder.getName(), className.location());
    
    javaArgType = new Type[javaTypes.size()-1];
    
    for(int i = 1; i<javaTypes.size(); i++)
      {
	LocatedString t = (LocatedString) javaTypes.get(i);
	
	javaArgType[i-1] = type(t);
	
	// set the fully qualified name back
	javaTypes.set(i,new LocatedString(javaArgType[i-1].getName(),
					  t.location()));
      }
    
    LocatedString retTypeString = (LocatedString) javaTypes.get(0);
    javaRetType = type(retTypeString);
    
    // set the fully qualified name of the return type back
    javaTypes.set(0, new LocatedString(javaRetType.getName(), 
				       retTypeString.location()));
    
    reflectMethod = holder.getDeclaredMethod(methodName, javaArgType);
    
    if (reflectMethod == null)
      {
	try {
          reflectMethod = 
            holder.getDeclaredMethod(methodName, javaArgType.length);
        } catch (Error e) {
          User.error(this, "The types of the arguments don't exactly match any of the declarations");
	}
	if (reflectMethod == null)
	  {
            if (methodName.equals("<init>"))
              User.error(className, "class " + holder.getName() + " has no constructor with " + javaArgType.length + " arguments");
	    else
              User.error(className, "No method named " + methodName + " with " +
		javaArgType.length + " arguments was found in class " +
		holder.getName());
	  }

        User.error(className, "The types of the arguments don't match the declaration:\n"
			 + reflectMethod);

      }
    // use the following, or the Type.flushTypeChanges() in SpecialTypes
    //reflectMethod.arg_types = javaArgType;
    //if(!methodName.equals("<init>"))
    //reflectMethod.return_type = javaRetType;

    if (! reflectMethod.isConstructor() &&
	! reflectMethod.return_type.getName().equals(javaRetType.getName()))
      User.error(retTypeString,
		 "Method " + methodName +
		 " has not the indicated return type." +
		 "\nIndicated: " + javaRetType.getName() +
		 "\nFound    : " + reflectMethod.return_type.getName());
    
    JavaClasses.registerNativeMethod(this, reflectMethod);    

    if (reflectMethod.isConstructor())
      {
	try {
	  mlsub.typing.TypeConstructor tc = Types.typeConstructor(holder);
	  if (tc != null)
	    TypeConstructors.addConstructor(tc, this);
	}
	catch(Types.NotIntroducedClassException ex) {}
      }
    
    int javaArity;
    if(reflectMethod.getStaticFlag() || reflectMethod.isConstructor())
      javaArity = javaTypes.size()-1;
    else
      javaArity = javaTypes.size();
    
    if(javaTypes != null && javaArity != arity)
      User.error(this,
		 "Native method " + this.getSymbol().name + 
		 " has not the same number of parameters " +
		 "in Java (" + javaArity +
		 ") and in Nice (" + arity + ")");
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() +
	    " = native " +
	    (methodName.equals("<init>") 
	     ?  "new " + className
	     : javaTypes.get(0) 
	     + " " + className 
	     + "." + methodName) +
	    mapGetName(javaArgTypes()) + ";\n");
  }
  
  private static String mapGetName(gnu.bytecode.Type[] types)
  {
    if(types == null)
      return "((NULL))";
    
    String res = "(";
    for(int n = 0; n<types.length; n++)
      {
	if(n != 0)
	  res += ", ";
	if(types[n] == null)
	  res += "[NULL]";
	else
	  res += types[n].getName();
      }
    return res + ")";
  }
}
