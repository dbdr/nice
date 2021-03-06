// Copyright (c) 2001  Per M.A. Bothner and Brainfood Inc.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.lists;
import java.io.*;

/** A "pair" object, as used in Lisp-like languages.
 * When used to implement a list, the 'car' field contains an
 * element's value, and the 'cdr' field points to either the next Pair
 * or LList.Empty (which represents the end of the list).  (The names
 * "car" and "cdr" [pronounced "coulder"] are historical; better names
 * might be "value" and "next".)  While a Pair is normally usued to
 * implement a linked list, sometimes the 'cdr' field ponus to some
 * other non-list object; this is traditionally callled a "dotted list".
 */

public class Pair extends LList implements Externalizable
{
   public Object car;
   public Object cdr;

  public Pair (Object carval, Object cdrval)
  {
    car = carval;
    cdr = cdrval;
  }

  public Pair ()
  {
  }

  public int size()
  {
    int n = listLength(this, true);
    if (n >= 0)
      return n;
    if (n == -1)
      return Integer.MAX_VALUE;
    throw new RuntimeException("not a true list");
  }

  public boolean isEmpty()
  {
    return false;
  }

  // A generalization of LList.list_length
  // FIXME is this needed, given listLength?
  public int length ()
  {
    // Based on list-length implementation in
    // Guy L Steele jr: "Common Lisp:  The Language", 2nd edition, page 414
    int n = 0;
    Object slow = this;
    Object fast = this;
    for (;;)
      {
	if (fast == Empty)
	  return n;
	if (! (fast instanceof Pair))
	  {
	    if (fast instanceof Sequence)
	      {
		int j = ((Sequence) fast).size();
		return j >= 0 ? n + j : j;
	      }
	    return -2;
	  }
	Pair fast_pair = (Pair) fast;
	if (fast_pair.cdr == Empty)
	  return n+1;
	if (fast == slow && n > 0)
	  return -1;
	if (! (fast_pair.cdr instanceof Pair))
	  {
	    n++;
	    fast = fast_pair.cdr;
	    continue;
	  }
	if (!(slow instanceof Pair))
	  return -2;
	slow = ((Pair)slow).cdr;
	fast = ((Pair)fast_pair.cdr).cdr;
	n += 2;
      }
  }

  /*
  public void print(java.io.PrintWriter ps)
  {
    ps.print("(");
    printNoParen(this, ps);
    ps.print(")");
  }
  */

  public int hashCode()
  {
    // Compatible with the AbstractSequence hashCode for true lists.
    int hash = 1;
    Object list = this;
    while (list instanceof Pair)
      {
	Pair pair = (Pair) list;
	Object obj = pair.car;
	hash = 31*hash + (obj==null ? 0 : obj.hashCode());
	list = pair.cdr;
      }
    if (list != LList.Empty && list != null)
      hash = hash ^ list.hashCode();
    return hash;
  }

  static public boolean equals (Pair pair1, Pair pair2)
  {
    if (pair1 == pair2)
      return true;
    if (pair1 == null || pair2 == null)
      return false;
    for (;;)
      {
	Object x1 = pair1.car;
	Object x2 = pair2.car;
	if (x1 != x2 && (x1 == null || ! x1.equals(x2)))
	  return false;
	x1 = pair1.cdr;
	x2 = pair2.cdr;
	if (x1 == x2)
	  return true;
	if (x1 == null || x2 == null)
	  return false;
	if (! (x1 instanceof Pair) || !(x2 instanceof Pair))
	  return x1.equals(x2);
	pair1 = (Pair) x1;
	pair2 = (Pair) x2;
      
      }
  }

  public Object get (int index)
  {
    Pair pair = this;
    int i = index;
    while (i > 0)
      {
	i--;
	if (pair.cdr instanceof Pair)
	  pair = (Pair)pair.cdr;
	else if (pair.cdr instanceof Sequence)
	  return ((Sequence)pair.cdr).get(i);
	else
	  break;
      }
    if (i == 0)
      return pair.car;
    else
      throw new IndexOutOfBoundsException ();
  }

  public boolean equals (Object obj)
  {
    if ((obj != null) && (obj instanceof Pair))
      return equals (this, (Pair) obj);
    else
      return false;
  }

  public static Pair make (Object car, Object cdr)
  {
    return new Pair (car, cdr);
  }

  public Object[] toArray()
  {
    int len = size(); // size() 
    Object[] arr = new Object[len];
    int i = 0;
    Sequence rest = this;
    for ( ;  i < len && rest instanceof Pair;  i++)
    {
      Pair pair = (Pair) rest;
      arr[i] = pair.car;
      rest = (Sequence) pair.cdr;
    }
    int prefix = i;
    for ( ;  i < len;  i++)
    {
      arr[i] = rest.get(i - prefix);
    }
    return arr;
  }

  public Object[] toArray(Object[] arr)
  {
    int alen = arr.length;
    int len = length();
    if (len > alen)
    {
      // FIXME Collection spec requires arr to keep same run-time type
      arr = new Object[len];
      alen = len;
    }
    int i = 0;
    Sequence rest = this;
    for ( ;  i < len && rest instanceof Pair;  i++)
    {
      Pair pair = (Pair) rest;
      arr[i] = pair.car;
      rest = (Sequence) pair.cdr;
    }
    int prefix = i;
    for ( ;  i < len;  i++)
    {
      arr[i] = rest.get(i - prefix);
    }
    if (len < alen)
      arr[len] = null;
    return arr;
  }

  /**
   * @serialData Write the car followed by the cdr.
   */
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(car);
    out.writeObject(cdr);
  }

  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException
  {
    car = in.readObject();
    cdr = in.readObject();
  }
};
