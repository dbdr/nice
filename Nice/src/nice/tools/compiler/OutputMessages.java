/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : OutputMessages.java
// Created : Wed Oct 04 11:30:44 2000 by Daniel Bonniot

package nice.tools.compiler;

import bossa.util.*;

/**
   Keep track of messages reported to the user.

   @version $Date$
   @author Daniel Bonniot
 */

public final class OutputMessages
{
  /**
   Exit code table:
   0 : Normal exit (compilation sucessful, version message)
   1 : Abnormal termination (bug in the compiler)
   2 : Error reported (file missing, type error, ...)
   3 : Warning reported
  */
  public static int statusCode()
  {
    return statusCode;
  }
  
  public static void warning(String message)
  {
    setStatusCode(3);
    System.out.println(message);
  }
  
  private static int statusCode = 0;
  
  private static void setStatusCode(int status)
  {
    if (worse(status, statusCode))
      statusCode = status;
  }

  private static boolean worse(int status, int than)
  {
    switch(than){
    case 0: return true;
    case 1: return false;
    case 2: return false;
    case 3: return status == 1 || status == 2;
    default: return false;
    }
  }
  
  private OutputMessages(){}  
}
