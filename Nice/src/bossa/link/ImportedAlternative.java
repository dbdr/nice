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
import bossa.syntax.VarSymbol;
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

    try {
      Pattern p;
      while ((p = bossa.syntax.dispatch.readPattern(rep, at)) != null)
        {
          if (p.getTC() == bossa.syntax.PrimitiveType.arrayTC)
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

      Alternative alt = 
        new ImportedAlternative(method.getName(), (Pattern[]) 
                                patterns.toArray(new Pattern[patterns.size()]),
                                new QuoteExp(new PrimProcedure(method)),
                                location);

      alt.add(nice.tools.util.System.split
              (fullName, MethodDeclaration.methodListSeparator));
    }
    catch(Pattern.Unknown ex) {
      // This can happen if the class exists only in a later version
      // of the JDK. We just ignore this alternative.
    }
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
       bossa.util.Location.nowhere());

    List methods = bossa.syntax.Node.getGlobalScope().lookup(methodName);
    for (Iterator i = methods.iterator(); i.hasNext();)
      {
	VarSymbol next = (VarSymbol)i.next();
	if (next.getMethodDeclaration() == null)
	  continue;
	MethodDeclaration md = next.getMethodDeclaration();
	if (md.getFullName().equals(fullName))
	  {
	    ((JavaMethod) md).registerForDispatch();
	    return;
	  }
      }
  }

  private ImportedAlternative(String name, Pattern[] patterns,
			      gnu.expr.Expression code, Location location)
  {
    super(name, patterns);
    this.code = code;
    this.location = location;
  }

  public Expression methodExp() { return code; }

  private Expression code;

  public Location location() { return location; }

  private Location location;
}
