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

// File    : JavaMethodDefinition.java
// Created : Tue Nov 09 11:49:47 1999 by bonniot
//$Modified: Tue Apr 04 16:26:35 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Location;

import java.util.*;

/**
 * A Native Java Method.
 */
public class JavaMethodDefinition extends MethodDefinition
{
  /**
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public JavaMethodDefinition(
			      // Informations about the java method
			      // These 3 args are null if we are in an interface file
			      LocatedString className,
			      String methodName,
			      List /* of LocatedString */ javaTypes,
			      // Bossa information
			      LocatedString name, 
			      Constraint constraint,
			      Monotype returnType,
			      List parameters
			      )
  {
    super(name,constraint,returnType,parameters);
    
    this.className=className;
    this.methodName=methodName;
    this.javaTypes=javaTypes;
  }

  /****************************************************************
   * Converting a bytecode type (coming from reflection
   * into a bossa type.
   * Used for automatic declaration of jav methods.
   ****************************************************************/
  
  static Monotype getMonotype(Type javaType)
  {
    return getMonotype(javaType.getName());
  }
  
  static Monotype getMonotype(String name)
  {
    if(name.endsWith("[]"))
      {
	name=name.substring(0,name.length()-2);
	List params = new LinkedList();
	params.add(getMonotype(name));
	return new MonotypeConstructor
	  (ConstantExp.arrayTC, 
	   new TypeParameters(params),
	   Location.nowhere());
      }
    
    TypeSymbol ts = Node.getGlobalTypeScope().lookup(name);
    if(ts==null)
      Internal.error(name+" is not known");
    if(ts instanceof JavaTypeConstructor)
      return ((JavaTypeConstructor) ts).getMonotype();
    // for primitive types, maybe temporary
    else if(ts instanceof TypeConstructor)
      return new MonotypeConstructor((TypeConstructor) ts,null,
				     Location.nowhere());
    else if(ts instanceof Monotype)
      return (Monotype) ts;
    else
      {
	Internal.error("Bad java type: "+name+" ("+ts.getClass()+")");
	return null;
      }
  }
  
  private static JavaMethodDefinition make(Method m)
  {
    JavaMethodDefinition res;

    Type[] paramTypes = m.getParameterTypes();
    List params;
    if(m.getStaticFlag())
      params = new ArrayList(paramTypes.length);
    else
      {
	params = new ArrayList(paramTypes.length+1);
	params.add(getMonotype(m.getDeclaringClass()));
      }
    
    for(int i=0; i<paramTypes.length; i++)
      params.add(getMonotype(paramTypes[i]));
    
    res = new JavaMethodDefinition(null, m.getName(), null,
				   new LocatedString(m.getName(), 
						     Location.nowhere()),
				   Constraint.True,
				   getMonotype(m.getReturnType()),
				   params);

    res.reflectMethod = m;
    return res;
  }
  
  /****************************************************************
   * Store automatically fetched java methods.
   * Do not take them into account if an explicit java method
   * was declared for the same reflect method.
   ****************************************************************/

  private static Map declaredMethods = new HashMap();
  
  public static JavaMethodDefinition addFetchedMethod(Method m)
  {
    if(declaredMethods.get(m)!=null)
      return null;

    JavaMethodDefinition md = JavaMethodDefinition.make(m);
    Node.getGlobalScope().addSymbol(md.symbol);
    return md;
  }

  public static MethodDefinition addFetchedMethod(Field m)
  {
    MethodDefinition md = StaticFieldAccess.make(m);
    Node.getGlobalScope().addSymbol(md.symbol);
    return md;
  }

  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  {
    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    findReflectMethod();    
    
    return super.buildScope(outer, typeOuter);
  }
  
  private gnu.bytecode.Type type(LocatedString s)
  {
    Type res = type(s.toString());
    if(res==null)
      User.error(s, "Unknown java class "+s);
    return res;
  }
  
  private gnu.bytecode.Type type(String s)
  {
    if(s.length()==0)
      return null;
    
    if(s.charAt(0)=='[')
      {
	Type res = type(s.substring(1));
	if(res==null)
	  return null;
	else
	  return gnu.bytecode.ArrayType.make(res);
      }
    
    if(s.equals("void")) 	return bossa.SpecialTypes.voidType;
    if(s.equals("int"))  	return bossa.SpecialTypes.intType;
    if(s.equals("long")) 	return bossa.SpecialTypes.longType;
    if(s.equals("boolean")) 	return bossa.SpecialTypes.booleanType;
    //if(s.equals("_Array"))	return bossa.SpecialTypes.arrayType;
    
    Class clas = JavaTypeConstructor.lookupJavaClass(s);
    if(clas==null)
      return null;
    return Type.make(clas);
  }
  
  /** The java class this method is defined in */
  LocatedString className;

  /** Its name in the java class */
  String methodName;
  
  private List /* of LocatedString */ javaTypes;
  
  /** Access flags */
  private int flags;
  
  private int javaArity;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private Type javaRetType;
  private Type[] javaArgType;
  private Method reflectMethod;
  
  public gnu.bytecode.Type javaReturnType() 
  { return javaRetType; }

  public gnu.bytecode.Type[] javaArgTypes()
  { return javaArgType; }

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    return new gnu.expr.PrimProcedure(reflectMethod);
  }
  
  private void findReflectMethod()
  {
    if(reflectMethod!=null)
      return;
    
    javaArgType = new Type[javaTypes.size()-1];
    
    for(int i=1;i<javaTypes.size();i++)
      {
	LocatedString t = (LocatedString) javaTypes.get(i);
	
	javaArgType[i-1]=type(t);
	
	// set the fully qualified name back
	javaTypes.set(i,new LocatedString(javaArgType[i-1].getName(),
					  t.location()));
      }

    LocatedString t = (LocatedString) javaTypes.get(0);
    javaRetType = type(t);
    
    // set the fully qualified name of the return type back
    javaTypes.set(0,new LocatedString(javaRetType.getName(),t.location()));
    
    Type holder = type(className.toString());
    className = new LocatedString(holder.getName(),className.location());
    
    if(!(holder instanceof ClassType))
      User.error(this, className+" is a primitive type");
    
    reflectMethod = ((ClassType) holder).getDeclaredMethod(methodName,javaArgType);
    // use the following, or the Type.flushTypeChanges() in SpecialTypes
    //reflectMethod.arg_types = javaArgType;
    //if(!methodName.equals("<init>"))
    //reflectMethod.return_type = javaRetType;

    if(reflectMethod==null)
      User.error(this, this+" was not found");

    declaredMethods.put(reflectMethod, Boolean.TRUE);

    if(reflectMethod.getStaticFlag() || methodName.equals("<init>"))
      javaArity=arity;
    else
      javaArity=arity-1;

    if(javaTypes!=null && javaTypes.size()-1!=javaArity)
      User.error(this,
		 "Native method "+this.symbol.name+
		 " has not the same number of parameters "+
		 "in Java ("+javaArity+
		 ") and in Bossa ("+arity+")");    
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(interfaceString());
  }

  private String interfaceString()
  {
    String res =
      symbol.type.getConstraint().toString()
      + symbol.type.codomain().toString()
      + " "
      + symbol.name.toQuotedString()
      + Util.map("<",", ",">",symbol.type.getTypeParameters())
      + "("
      + Util.map("",", ","",symbol.type.domain())
      + ")"
      + " = native ";
    if(methodName.equals("<init>"))
       res += "new " + className;
    else
      res +=  javaReturnType().getName() 
	+ " " + className 
	+ "." + methodName;
    
    return res
      + mapGetName(javaArgTypes())
      + ";\n";
  }
  
  private static String mapGetName(gnu.bytecode.Type[] types)
  {
    if(types == null)
      return "((NULL))";
    
    String res="(";
    for(int n=0;n<types.length;n++)
      {
	if(n!=0)
	  res+=", ";
	if(types[n] == null)
	  res+="[NULL]";
	else
	  res+=types[n].getName();
      }
    return res+")";
  }
    
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return "native "+className+"."+methodName+mapGetName(javaArgTypes());
  }
}
