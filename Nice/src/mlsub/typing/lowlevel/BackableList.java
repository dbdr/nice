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

// File    : BackableList.java
// Created : Wed Aug 25 12:52:31 1999 by bonniot
//$Modified: Wed Jun 07 18:55:46 2000 by Daniel Bonniot $

package mlsub.typing.lowlevel;

import java.util.*;

/**
 * List with mark/backtrack facility
 * 
 * @author bonniot
 */

public class BackableList
{
  public BackableList()
  {
    content=new ArrayList();
  }

  public BackableList(int capacity)
  {
    content=new ArrayList(capacity);
  }

  /****************************************************************
   * Markup/Backtrack
   ****************************************************************/

  public void mark()
  {
    backups.push(content);
    content=(ArrayList)content.clone();
  }
  
  public void backtrack()
  {
    content=(ArrayList)backups.pop();
  }
  
  /****************************************************************
   * List implementation
   ****************************************************************/

  public void add(Object element)
  {
    if(iterationInProgress)
      waitingElements.push(element);
    else
      content.add(element);
  }
  
  public void remove(Object element)
  {
    if(iterationInProgress)
      throw new InternalError("remove during iteration in BackableList");

    content.remove(element);
  }
  
  public boolean contains(Object element)
  {
    return content.contains(element);
  }
  
  public void clear()
  {
    content.clear();
  }
  
  public Iterator iterator()
  {
    if(iterationInProgress)
      throw new InternalError("Concurrent iterations in BackableList");

    iterationInProgress=true;
    return content.iterator();
  }
  
  public void endOfIteration()
  {
    if(!iterationInProgress)
      throw new InternalError("No iterations to end in BackableList");

    iterationInProgress=false;
    while(!waitingElements.empty())
      content.add(waitingElements.pop());
  }
  
  public String toString()
  {
    return content.toString();
  }

  private ArrayList content;
  private Stack backups=new Stack();

  // We want to allow the addition of elements
  // while iterating on the list.
  // It is done by postponing the addition 
  // until the iteration is declared explicitely to be done
  // ( with endOfIteration() ).
  private boolean iterationInProgress=false;
  private Stack waitingElements=new Stack();
}
