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

package nice.tools.compiler;

/**
   Keep track of messages reported to the user.

   @version $Date$
   @author Daniel Bonniot
 */

public final class OutputMessages
{
  /** Should be called when the compilation starts, before any message is printed. */
  public static void start()
  {
    statusCode = OK;
  }

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
  
  /** A bug has occured inside the compiler. */
  public static void bug(String message)
  {
    setStatusCode(BUG);
    System.err.println(message);
  }
  
  /** If errors have been reported, throw an exception to stop the compilation. */
  public static void exitIfErrors()
  {
    if (statusCode == ERROR || statusCode == BUG)
      throw new Exit(statusCode);
  }

  public static class Exit extends RuntimeException
  {
    public final int statusCode;

    Exit(int statusCode)
    {
      this.statusCode = statusCode;
    }
  }

  public static final int 
    OK      = 0, // Normal exit (compilation sucessful, version message)
    BUG     = 1, // Abnormal termination (bug in the compiler)
    ERROR   = 2, // Error reported (file missing, type error, ...)
    WARNING = 3; // Warning reported

  public static int getStatusCode() { return statusCode; }

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
