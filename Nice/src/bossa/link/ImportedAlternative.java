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
import nice.tools.code.*;

import gnu.bytecode.*;
import gnu.expr.*;
import java.util.*;
import bossa.util.*;

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
  public static void read(ClassType c, Method method)
  {
    MiscAttr attr = (MiscAttr) Attribute.get(method, "definition");
    if (attr == null)
      // this must be a toplevel function, a constructor, ...
      return;

    String methodName = new String(attr.data);

    attr = (MiscAttr) Attribute.get(method, "patterns");
    if (attr == null)
      Internal.error("Method " + method.getName() + 
		     " in class " + c.getName() + " has no patterns");
    String rep = new String(attr.data);

    int[]/*ref*/ at = new int[]{ 0 };

    ArrayList patterns = new ArrayList(5);

    Pattern p;
    while ((p = Pattern.read(rep, at, methodName)) != null)
      {
	if (p.tc == bossa.syntax.ConstantExp.arrayTC)
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
    
    new ImportedAlternative(methodName, (Pattern[]) 
			    patterns.toArray(new Pattern[patterns.size()]),
			    new QuoteExp(new PrimProcedure(method)));
  }

  private ImportedAlternative(String name, Pattern[] patterns, 
			      gnu.expr.Expression code)
  {
    super(name, patterns);
    this.code = code;
  }  

  public Expression methodExp() { return code; }

  private Expression code;
}
