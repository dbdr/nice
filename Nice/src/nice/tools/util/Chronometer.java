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

package nice.tools.util;

import java.util.*;

/**
   Tool to mesure time cumulatively. Used for benchmarking.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class Chronometer
{
  public static Chronometer make(String name)
  {
    Chronometer res = new Chronometer(name);
    chronometers.add(res);
    return res;
  }

  public static void printAll()
  {
    for (Iterator i = chronometers.iterator(); i.hasNext();)
      java.lang.System.out.println(((Chronometer) i.next()).toString());
  }

  private static List chronometers = new LinkedList();

  private Chronometer(String name)
  {
    this.name = name;
  }

  public String toString()
  {
    return name + ": " + getTotalTime() + " ms";
  }

  private String name;

  private long total = 0;
  private long startTime = -1;
  private long stopTime = -1;
  private boolean running = false;

  public void start()
  {
    if (running)
      throw new Error("Already running");
    running = true;
    startTime = java.lang.System.currentTimeMillis();
  }

  public void stop()
  {
    stopTime = java.lang.System.currentTimeMillis();
    if (!running)
      throw new Error("Not running");
    running = false;
    total += stopTime - startTime;
  }

  /** returns elapsed time in milliseconds
   * if the watch has never been started then
   * return zero
   */
  public long getTotalTime()
  {
    if (running)
      throw new Error("Running");
    return total;
  }
}
