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

package nice.tools.util;

/**
   Implementations of features that were introduced in recent JDKs 
   (1.4 and later).

   We don't want to call them directly, as this would break compatibility with
   JDK 1.3, but we implement replacements here. If we decide to raise the
   requirements, we can simply delete these methods and replace them with the
   JDK implementation.

   @author Daniel Bonniot (bonniot@users.sf.net)
 */

public class JDK
{
  /* JDK 1.4 */

  /** Replacement for String.replaceAll(String,String) */
  public static String replaceAll(String source, String what, String with)
  {
    int index = source.indexOf(what);

    if (index == -1)
      return source;

    int len = what.length();
    int last = 0;

    StringBuffer res = new StringBuffer(source.length() * 2);

    while (index != -1)
      {
        res.append(source.substring(last, index));
        res.append(with);
        last = index + len;
        index = source.indexOf(what, last);
      }
    res.append(source.substring(last));

    return res.toString();
  }
}
