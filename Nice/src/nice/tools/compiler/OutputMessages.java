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

package nice.tools.compiler;

/**
   Keep track of messages reported to the user.

   @version $Date$
   @author Daniel Bonniot
 */

public final class OutputMessages
{
  public static void warning(String message)
  {
    setStatusCode(WARNING);
    System.out.println(message);
  }
  
  public static void error(String message)
  {
    setStatusCode(ERROR);
    System.err.println(message);
  }
  
  public static void fatal(String message)
  {
    error(message);
    exitIfErrors();
  }
  
  public static void exitIfErrors()
  {
    if (statusCode == ERROR || statusCode == BUG)
      exit();
  }

  public static void exit()
  {
    // A non-zero code is interpreted as error, e.g. by make
    if (statusCode == WARNING)
      System.exit(OK);
    else
      System.exit(statusCode);
  }
  
  public static final int 
    OK      = 0, // Normal exit (compilation sucessful, version message)
    BUG     = 1, // Abnormal termination (bug in the compiler)
    ERROR   = 2, // Error reported (file missing, type error, ...)
    WARNING = 3; // Warning reported

  private static int statusCode = OK;

  private static void setStatusCode(int status)
  {
    if (worse(status, statusCode))
      statusCode = status;
  }

  private static boolean worse(int status, int than)
  {
    switch(than){
    case OK: return true;
    case BUG: return false;
    case ERROR: return false;
    case WARNING: return status == BUG || status == ERROR;
    default: return false;
    }
  }
  
  private OutputMessages(){}  
}
