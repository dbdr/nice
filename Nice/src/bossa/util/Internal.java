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

// File    : Internal.java
// Created : Wed Jul 07 18:23:19 1999 by bonniot
//$Modified: Wed Oct 04 11:47:18 2000 by Daniel Bonniot $
// Description : Internal errors...

package bossa.util;

/**
   Internal messages are sent through this static class
 */

public final class Internal
{
  public static void printStackTrace()
  {
    try{  
      throw new Exception();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
        
  public static void warning(Located loc, String message)
  {
    Location l = loc.location();
    if(l==null)
      warning(message);
    else
      warning(l+":\n"+message);
  }
  
  public static void warning(String message)
  {
    if (Debug.powerUser)
      {
	if(Debug.alwaysDumpStack)
	  printStackTrace();
        if (bossa.modules.Package.currentCompilation != null)
          bossa.modules.Package.currentCompilation.warning
            (null, "[Internal warning] " + message);
        else
          System.err.println("[Internal warning] " + message);
      }
  }

  public static Error error(Located loc, String message, String dbgMsg)
  {
    if(Debug.powerUser)
      return error(loc, message + dbgMsg);
    else
      return error(loc, message);
  }
  
  public static Error error(Located loc, String message)
  {
    Location l = loc.location();
    if (l == null)
      return error(message);
    else
      return error(l + ":\n" + message);
  }

  public static Error error(String message, String dbgMsg)
  {
    if (Debug.powerUser)
      return error(message + dbgMsg);
    else
      return error(message);
  }
  
  public static Error error(String message)
  {
    throw new InternalError(message);
  }
  
  public static Error error(Throwable e)
  {
    if(e instanceof ExceptionInInitializerError)
      {
	System.out.println("Exception in initializer");
	e.printStackTrace();
	e = ((ExceptionInInitializerError) e).getException();
      }
    
    String msg = e.getMessage();
    if (msg == null)
      msg = "";
    
    System.out.println("[Internal error]\n" + msg);
    System.out.println("Upstream error:\n");
    e.printStackTrace();
    System.out.println("Internal error:\n");
    printStackTrace();
    System.exit(1);
    return null;
  }

}

class InternalError extends Error
{
  InternalError(String message)
  {
    super(message);
  }
}
