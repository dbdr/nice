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

package bossa.link;

import bossa.syntax.Pattern;
import bossa.syntax.LocatedString;
import bossa.syntax.MethodDeclaration;
import bossa.syntax.JavaMethod;
import nice.tools.code.*;

import gnu.bytecode.*;
import gnu.expr.*;
import java.util.*;
import bossa.util.*;
import bossa.util.Location;

/**
   An alternative imported from a compiled package.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class ImportedAlternative extends Alternative
{
  /**
   * When read from a bytecode file.
   */
  public static void read(ClassType c, Method method, Location location)
  {
    MiscAttr attr = (MiscAttr) Attribute.get(method, "definition");
    if (attr == null)
      // this must be a toplevel function, a constructor, ...
      return;

    String fullName = new String(attr.data);

    registerJavaMethod(fullName);

    attr = (MiscAttr) Attribute.get(method, "patterns");
    if (attr == null)
      Internal.error("Method " + method.getName() + 
		     " in class " + c.getName() + " has no patterns");
    String rep = new String(attr.data);

    int[]/*ref*/ at = new int[]{ 0 };

    ArrayList patterns = new ArrayList(5);

    Pattern p;
    while ((p = Pattern.read(rep, at, fullName)) != null)
      {
	if (p.tc == bossa.syntax.PrimitiveType.arrayTC)
	  /* Special treatment for arrays:
	     they are compiled into Object,
	     but we want a SpecialArray in the method bytecode type.
	  */
	  {
	    int argnum = patterns.size();
	    if (method.arg_types[argnum] == Type.pointer_type)
	      method.arg_types[argnum] = SpecialArray.unknownTypeArray();
	  }

	patterns.add(p);
      }
    
    new ImportedAlternative(method.getName(), fullName, (Pattern[]) 
			    patterns.toArray(new Pattern[patterns.size()]),
			    new QuoteExp(new PrimProcedure(method)),
			    location);
  }

  /**
     If this full name refers to a java method, make sure it participates
     to the link tests and dispatch code generation.
  */
  private static void registerJavaMethod(String fullName)
  {
    if (! fullName.startsWith(JavaMethod.fullNamePrefix))
      return;

    int end = fullName.lastIndexOf(':');
    LocatedString methodName = new LocatedString
      (fullName.substring(JavaMethod.fullNamePrefix.length(), end),
       bossa.util.Location.nowhereAtAll());

    List methods = bossa.syntax.Node.getGlobalScope().lookup(methodName);
    for (Iterator i = methods.iterator(); i.hasNext();)
      {
	Object next = i.next();
	if (! (next instanceof MethodDeclaration.Symbol))
	  continue;
	MethodDeclaration md = ((MethodDeclaration.Symbol) next).getDefinition();
	if (md.getFullName().equals(fullName))
	  {
	    ((JavaMethod) md).registerForDispatch();
	    return;
	  }
      }
  }

  private ImportedAlternative(String name, String fullName, Pattern[] patterns,
			      gnu.expr.Expression code, Location location)
  {
    super(name, fullName, patterns);
    this.code = code;
    this.location = location;
  }

  public Expression methodExp() { return code; }

  private Expression code;

  public Location location() { return location; }

  private Location location;
}
