/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.util;

public class UserError extends RuntimeException
{
  public UserError()
  {
    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();    
  }

  UserError(String message)
  {
    this.message = message;

    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();    
  }

  public UserError(Located responsible, String message)
  {
    this(responsible.location(), message);
  }

  public UserError(gnu.expr.Expression responsible, String message)
  {
    this(new Location(responsible.getFile(), responsible.getLine(), 
		      responsible.getColumn()), 
	 message);
  }

  public UserError(Location location, String message)
  {
    this(message);
    this.location = location;
  }

  public String getMessage()
  {
    if (location == null)
      return "\n"+message;
    else
      return "\n"+location + ":\n" + message;
  }

  public String message;
  public Location location;
}
