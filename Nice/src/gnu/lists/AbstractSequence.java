// -*-Java-*-
// Copyright (c) 2001  Per M.A. Bothner and Brainfood Inc.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.lists;
import java.util.*;
import java.util.Enumeration;

/**
 * An AbstractSequence is used to implement Sequences, and almost all
 * classes that extend AbstractSequence will implement Sequence.
 * However, AbstractSequence itself does not implement Sequence.
 * This is so we can use AbstractSequence to implement classes that are
 * "sequence-like" (such as multi-dimesnional arrays) but are not Sequences.
 *
 * Additionally, a sequence may have zero or more attributes, which are
 * name-value pairs.  A sequence may also have a named "type".  These
 * extensions are to support XML functionality - it might be cleaner to
 * moe them to a sub-class of Sequence or some interface.
 *
 * Many of the protected methods in Sequence (such as nextIndex) are
 * only intended to be called from SeqPosition or TreePosition, see those.
 *
 * @author Per Bothner
 */

public abstract class AbstractSequence
{
  /** See java.util.List. */
  public abstract int size();

  public boolean isEmpty()
  {
    return size() > 0;
  }

  public int rank()
  {
    return 1;
  }

  /** See java.util.List. */
  public abstract Object get (int index);

  public int getEffectiveIndex(int[] indexes)
  {
    return indexes[0];
  }

  public Object get(int[] indexes)
  {
    return get(indexes[0]);
  }

  public Object set(int[] indexes, Object value)
  {
    return set(indexes[0], value);
  }

  protected RuntimeException unsupported (String text)
  {
    text = getClass().getName() + " does not implement " + text;
    return new UnsupportedOperationException(text);
  }

  public Object set(int index, Object element)
  {
    throw unsupported("set");
  }

  public void fill(Object value)
  {
    SeqPosition it = getIterator();
    while (gotoNext(it))
      setPrevious(it.ipos, it.xpos, value);
    it.finalize();
  }

  public void fill(int fromIndex, int toIndex, Object value)
  {
    for (int i = fromIndex;  i < toIndex;  i++)
      set(i, value);
  }

  // FIXME?
  //public final Object elementAt (int index) { return get(index); }

  /** See java.util.List. */
  public int indexOf(Object o)
  {
    int i = 0;
    SeqPosition it = getIterator();
    for (; it.hasNext();  i++)
      {
        Object e = it.next();
        if (o==null ? e==null : o.equals(e))
          return i;
      }
    return -1;
  }

  /** See java.util.List. */
  public int lastIndexOf(Object o)
  {
    // FIXME use iterator?
    for (int n = size();  --n >= 0; )
      {
        Object e = get(n);
        if (o==null ? e == null : o.equals(e))
          return n;
      }
    return -1;
  }

  /** See java.util.List. */
  public boolean contains(Object o)
  {
    SeqPosition i = getIterator();
    while (i.hasNext())
      {
        Object e = i.next();
        if (o==null ? e==null : o.equals(e))
          return true;
      }
    return false;
  }

  /** See java.util.List. */
  public boolean containsAll(Collection c)
  {
    SeqPosition i = getIterator();
    while (i.hasNext())
      {
        Object e = i.next();
        if (! contains(e))
          return false;
      }
    return true;
  }

  public Enumeration elements()
  {
    SeqPosition it = new SeqPosition();
    makeStartPosition(it);
    return it;
  }

  public SeqPosition getIterator()
  {
    SeqPosition it = new SeqPosition();
    makeStartPosition(it);
    return it;
  }

  public Iterator iterator()
  {
    SeqPosition it = new SeqPosition();
    makeStartPosition(it);
    return it;
  }

  public ListIterator listIterator()
  {
    return listIterator(0);
  }

  public ListIterator listIterator(int index)
  {
    SeqPosition it = new SeqPosition();
    makePosition(index, it);
    return it;
  }

  protected void add(PositionContainer posSet, int posNumber, Object value)
  {
    throw unsupported("add");
  }

  /** See java.util.Collection. */
  public boolean add(Object o)
  {
    add(size(), o);
    return true;
  }

  /** See java.util.List. */
  public void add(int index, Object o)
  {
    throw unsupported("add single Object at index");
  }

  /** See java.util.Collection. */
  public boolean addAll(Collection c)
  {
    return addAll(size(), c);
  }

  /** See java.util.Collection. */
  public boolean addAll(int index, Collection c)
  {
    boolean changed = false;
    for (Iterator it = c.iterator();  it.hasNext(); )
      {
	add(index++, it.next());
	changed = true;
      }
    return changed;
  }

  /**
   * Remove one or more elements.
   * @param ipos integer part of position where elements should be removed
   * @param xpos object part of position where elements should be removed
   * @param count if non-negative, remove that number of elements
   * following (poses, posNumber); if negative the negative of the number
   * of elements to remove before (poses, posNumber).
   * @return number of elements actually removed (non-negative)
   * @exception java.lang.IndexOutOfBoundsException
   *   if (count >= 0 ? (index < 0 || index + count > size())
   *       : (index + count < 0 || index > size())),
   *   where index == nextIndex(ipos, xpos).
   */
  protected void remove(int ipos, Object xpos, int count)
  {
    SeqPosition it = new SeqPosition(this);
    makeRelativePosition(ipos, xpos, count, true, it, 0);
    if (count >= 0)
      remove (ipos, xpos, it.ipos, it.xpos);
    else
      remove (it.ipos, it.xpos, ipos, xpos);
  }

  /** Remove a range where each end-point is a position in a container.
   * @param ipos0 integer part of start of range
   * @param xpos0 object part of start of range
   * @param ipos1 integer part of end of range
   * @param xpos1 object part of end of range
   * @exception java.lang.IndexOutOfBoundsException
   *   if nextIndex(ipos0, xpos0) > nextIndex(ipos1, xpos1)
   *   || nextIndex(ipos0, xpos0) < 0 || nextIndex(ipos1, xpos1) > size()
   */
  protected void remove(int ipos0, Object xpos0, int ipos1, Object xpos1)
  {
    throw unsupported("remove (with range)");
  }

  public Object remove(int index)
  {
    if (index < 0 || index >= size())
      throw new IndexOutOfBoundsException();
    SeqPosition it = new SeqPosition(this);
    makePosition(index, it);
    Object result = getNext(it.ipos, it.xpos);
    remove(it.ipos, it.xpos, 1);
    return result;
  }

  public boolean remove(Object o)
  {
    int index = indexOf(o);
    if (index < 0)
      return false;
    SeqPosition it = new SeqPosition();
    makePosition(index, it);
    remove(it.ipos, it.xpos, 1);
    return true;
  }

  public boolean removeAll(Collection c)
  {
    boolean changed = false;
    for (SeqPosition it = getIterator();  it.hasNext(); )
      {
        Object value = it.next();
        if (c.contains(value))
          {
            it.remove();
            changed = true;
          }
      }
    return changed;
  }

  public boolean retainAll(Collection c)
  {
    boolean changed = false;
    for (SeqPosition it = getIterator();  it.hasNext(); )
      {
        Object value = it.next();
        if (! c.contains(value))
          {
            it.remove();
            changed = true;
          }
      }
    return changed;
  }

  public void clear()
  {
    SeqPosition it = new SeqPosition();
    makeStartPosition(it);
    remove(it.ipos, it.xpos, size());
  }

  /** Does the position pair have the "isAfter" property?
   * I.e. if something is inserted at the position, will
   * the iterator end up being after the new data? */
  protected boolean isAfter(int ipos, Object xpos)
  {
    return false;
  }

  protected final void makePosition(int index, SeqPosition pos)
  {
    makePosition(index, true, pos);
  }

  /** Generate a position at a given index.
   * @param index offset from beginning of desired position
   * @param isAfter should the position have the isAfter property
   * @param pos where to store the generated position
   *   This old position should be already released, if needed.
   * @exception IndexOutOfBoundsException if index is out of bounds
   */
  public void makePosition(int index, boolean isAfter, SeqPosition pos)
  {
    makePosition(index, isAfter, pos, 0);
  }

  /** Generate a position at a given index.
   * @param index offset from beginning of desired position
   * @param isAfter should the position have the isAfter property
   * @param posSet where to store the generated position
   * @param posNumber index in posSet for the generated position.  Any old
   *   position there should be alread released, if needed.
   * @exception IndexOutOfBoundsException if index is out of bounds
   */
  protected abstract void
  makePosition(int index, boolean isAfter,
	       PositionContainer posSet, int posNumber);

  /** Generate a position relative to an existing position.
   * @param istart int part of initial position
   * @param xstart object part of initial position
   * @param offset offset from (istart,xstart) of desired position
   * @param isAfter should the position have the isAFter property
   * @param posSet where to store the generated position
   * @param positionNumber index in posSet for the generated position.  Any
   *   old position there should be alread released, if needed.
   * @exception IndexOutOfBoundsException if resulting index is out of bounds
   */
  protected void makeRelativePosition(int istart, Object xstart,
				      int offset, boolean isAfter,
				      PositionContainer posSet,
				      int posNumber)
  {
    makePosition(nextIndex(istart, xstart) + offset, isAfter, posSet, posNumber);
  }

  public void makeStartPosition(SeqPosition pos)
  {
    makeStartPosition(pos, 0);
  }

  /** Set a position to the start of this sequence. */
  protected void makeStartPosition(PositionContainer poses, int positionNumber)
  {
    makePosition(0, false, poses, positionNumber);
  }

  /** Set a position to the end of this sequence. */
  public void makeEndPosition(SeqPosition pos)
  {
    makeEndPosition(pos, 0);
    pos.setSequence(0, this); // FIXME - handled by caller?
  }

  protected void makeEndPosition(PositionContainer poses, int positionNumber)
  {
    makePosition(size(), true, poses, positionNumber);
  }

  /**
   * Reclaim any resources used by the given position pair.
   * @param ipos integer part of the position being free'd.
   * @param xpos Object part of the position being free'd.
   */
  protected void releasePosition(int ipos, Object xpos)
  {
  }

  protected final void releasePosition(SeqPosition pos)
  {
    releasePosition(pos.ipos, pos.xpos);
  }

  protected void releasePosition(PositionContainer posSet, int posNumber)
  {
    int ipos = posSet.getPositionInt(posNumber);
    Object xpos = posSet.getPositionPtr(posNumber);
    releasePosition(ipos, xpos);
  }

  /** Make a copy of a position pair.
   * For simple positions this is a simple copy (assignment).
   * However, if the positions are magic cookies that are actively managed
   * by the sequence (as opposed to for example a simple index), then making
   * a copy may need to increment a reference count, or maybe allocate a
   * new position pair.  In any case, the new pair is initialized to the
   * same offset (and isAfter property) as the original.
   * @param ipos integer part of the position being copied.
   * @param xpos Object part of the position being copied.
   * @param posSet where to put the new copy
   * @param posNumber which psoition in posSet that gets the copy.
   */
  public void copyPosition(int ipos, Object xpos,
			   PositionContainer posSet, int posNumber)
  {
    /*
    makePosition(nextIndex(ipos, xpos), isAfter(ipos, xpos),
		 posSet, posNumber);
    */
    posSet.setSequence(posNumber, this);
    posSet.setPosition(posNumber, ipos, xpos);
  }

  /** Get offset of (ipos1,xpos1) relative to (ipos0,xpos0). */
  protected int getIndexDifference(int ipos1, Object xpos1,
                                   int ipos0, Object xpos0)
  {
    return nextIndex(ipos1, xpos1) - nextIndex(ipos0, xpos0);
  }

  /**
   * Get the offset from the beginning corresponding to a position pair.
   * Note default implementation only works for array-like sequences!
   */
  protected int nextIndex(int ipos, Object xpos)
  {
    throw unsupported("nextIndex");
  }

  protected int fromEndIndex(int ipos, Object xpos)
  {
    return size() - nextIndex(ipos, xpos);
  }

  /**
   * Get the size of the (sub-) sequence containing a given position.
   * Normally the same as size(), but may be different if this Sequence
   * is a tree and the position points at an interior node.
   */
  protected int getContainingSequenceSize(int ipos, Object xpos)
  {
    return size();
  }

  /** Called by SeqPosition.hasNext. */
  protected boolean hasNext(int ipos, Object xpos)
  {
    return nextIndex(ipos, xpos) != size();
  }

  public int getNextKind(int ipos, Object xpos)
  {
    return hasNext(ipos, xpos) ? Sequence.OBJECT_VALUE : Sequence.EOF_VALUE;
  }

  public String getNextTypeName(int ipos, Object xpos)
  {
    return null;
  }

  public Object getNextTypeObject(int ipos, Object xpos)
  {
    return null;
  }

  /** Called by SeqPosition.hasPrevious. */
  protected boolean hasPrevious(int ipos, Object xpos)
  {
    return nextIndex(ipos, xpos) != 0;
  }

  /** Move forward one element position.
   * @return true unless at end of sequence.
   */
  public boolean gotoNext(PositionContainer posSet, int posNumber)
  {
    int ipos = posSet.getPositionInt(posNumber);
    Object xpos = posSet.getPositionPtr(posNumber);
    if (! hasNext(ipos, xpos))
      return false;
    if (true) // FIXME if not managed positions
      makeRelativePosition(ipos, xpos, 1, true, posSet, posNumber);
    else
      {
	int index = nextIndex(ipos, xpos);
	releasePosition(posSet, posNumber);
	makePosition(index + 1, true, posSet, posNumber);
      }
    return true;
  }

  /** Potential optimization. */
  public boolean gotoNext(SeqPosition pos)
  {
    return gotoNext(pos, 0);
  }

  /** Move backwards one element.
   * @return false iff already at beginning.
   */
  protected boolean gotoPrevious(PositionContainer posSet, int posNumber)
  {
    int ipos = posSet.getPositionInt(posNumber);
    Object xpos = posSet.getPositionPtr(posNumber);
    if (! hasPrevious(ipos, xpos))
      return false;
    if (true) // FIXME if not managed positions
      makeRelativePosition(ipos, xpos, -1, false, posSet, posNumber);
    else
      {
	int index = nextIndex(ipos, xpos);
	releasePosition(posSet, posNumber);
	makePosition(index - 1, false, posSet, posNumber);
      }
    return true;
  }

  /** Set position before first child (of the element following position).
   * @return true if there is a child sequence (which might be empty);
   *   false if current position is end of sequence or following element
   *   is atomic (cannot have children).
   */
  public boolean gotoChildrenStart(TreePosition pos)
  {
    return false;
  }

  protected boolean gotoParent(TreePosition pos)
  {
    if (pos.depth < 0)
      return false;
    pos.pop();
    return true;
  }

  public int getAttributeLength()
  {
    return 0;
  }

  public Object getAttribute(int index)
  {
    return null;
  }

  protected boolean gotoAttributesStart(TreePosition pos)
  {
    return false;
  }

  /** Get the element following the specified position.
   * @param ipos integer part of the specified position.
   * @param xpos Object part of the specified position.
   * @return the following element, or eofValue if there is none.
   * Called by SeqPosition.getNext. */
  protected  Object getNext(int ipos, Object xpos)
  {
    // wrong result if out of bounds FIXME
    return get(nextIndex(ipos, xpos));
  }

  /** Get the element before the specified position.
   * @param ipos integer part of the specified position.
   * @param xpos Object part of the specified position.
   * @return the following element, or eofValue if there is none.
   * Called by SeqPosition.getNext. */
  protected Object getPrevious(int ipos, Object xpos)
  {
    return get(nextIndex(ipos, xpos) - 1);
  }

  protected void setNext(int ipos, Object xpos, Object value)
  {
    int index = nextIndex(ipos, xpos);
    if (index >= size())
      throw new IndexOutOfBoundsException();
    set(index, value);
  }

  protected void setPrevious(int ipos, Object xpos, Object value)
  {
    int index = nextIndex(ipos, xpos);
    if (index == 0)
      throw new IndexOutOfBoundsException();
    set(index - 1, value);
  }

  public final int nextIndex(SeqPosition pos)
  {
    return nextIndex(pos.ipos, pos.xpos);
  }

  /** Compare two positions, and indicate if the are the same position. */
  public boolean equals(int ipos1, Object xpos1, int ipos2, Object xpos2)
  {
    return compare(ipos1, xpos1, ipos2, xpos2) == 0;
  }

  /** Compare two positions, and indicate their relative order. */
  public int compare(int ipos1, Object xpos1, int ipos2, Object xpos2)
  {
    int i1 = nextIndex(ipos1, xpos1);
    int i2 = nextIndex(ipos2, xpos2);
    return i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
  }

  public final int compare(SeqPosition i1, SeqPosition i2)
  {
    return compare(i1.ipos, i1.xpos, i2.ipos, i2.xpos);
  }

  public Object[] toArray() 
  { 
    int len = size(); 
    Object[] arr = new Object[len];
    
    java.util.Enumeration e = elements();
    for (int i = 0;  e.hasMoreElements(); i++)
      arr[i] = e.nextElement();
    return arr;
  } 

  public Object[] toArray(Object[] arr) 
  { 
    int alen = arr.length; 
    int len = size(); 
    if (len > alen) 
    { 
      Class componentType = arr.getClass().getComponentType();
      arr = (Object[]) java.lang.reflect.Array.newInstance(componentType, len);
      alen = len; 
    }
    
    java.util.Enumeration e = elements();
    for (int i = 0;  e.hasMoreElements(); i++)
    {
      arr[i] = e.nextElement();
    } 
    if (len < alen) 
      arr[len] = null; 
    return arr;
  }

  public int hashCode()
  {
    // Implementation specified by the Collections specification. 
    int hash = 1;
    SeqPosition i = getIterator();
    while (i.hasNext())
      {
        Object obj = i.next();
        hash = 31*hash + (obj==null ? 0 : obj.hashCode());
      }
    return hash;
  }

  public boolean equals(Object o)
  {
    // Compatible with the Collections specification.
    // FIXME should also depend on class?
    if (! (o instanceof java.util.List))
      return false;
    Iterator it1 = iterator();
    Iterator it2 = ((java.util.List) o).iterator();
    for (;;)
      {
        boolean more1 = it1.hasNext();
        boolean more2 = it2.hasNext();
        if (more1 != more2)
          return false;
        if (! more1)
          return true;
        Object e1 = it1.next();
        Object e2 = it2.next();
        if (e1 == null)
          {
            if (e2 != null)
              return false;
          }
        else if (! e1.equals(e2))
          return false;
      }
  }

  public Sequence subSequence(SeqPosition start, SeqPosition end)
  {
    return subSequence(start.ipos, start.xpos, end.ipos, end.xpos);
  }

  protected Sequence subSequence(int ipos0, Object xpos0,
				 int ipos1, Object xpos1)
  {
    SubSequence sub = new SubSequence(this);
    copyPosition(ipos0, xpos0, sub, 0);
    copyPosition(ipos1, xpos1, sub, 1);
    return sub;
  }

  public List subList(int fromIx, int toIx)
  {
    SubSequence sub = new SubSequence(this);
    makePosition(fromIx, false, sub, 0);
    makePosition(toIx, true, sub, 1);
    return sub;
  }

  /** Copy an element specified by a position pair to a Consumer.
   * @return if hasNext(ipos, xpos). */
  public boolean consumeNext(int ipos, Object xpos, Consumer out)
  {
    if (! hasNext(ipos, xpos))
      return false;
    out.writeObject(getNext(ipos, xpos));
    return true;
  }

  protected void consume(int iposStart, Object xposStart,
			 int iposEnd, Object xposEnd, Consumer out)
  {
    if (out.ignoring())
      return;
    SeqPosition it = new SeqPosition();
    copyPosition(iposStart, xposStart, it, 0);
    while (! equals(it.ipos, it.xpos, iposEnd, xposEnd))
      {
	if (! it.hasNext())
	  throw new RuntimeException();
	out.writeObject(it.nextElement());
      }
    it.finalize();
  }

  /*
  Consume wlements, without any beginGroup/endGroup.
  public void consumeElements() ...
  */

  public void consume(Consumer out)
  {
    String typeName = "#sequence"; 
    String type = typeName;
    out.beginGroup(typeName, type);
    java.util.Enumeration e = elements();
    for (int i = 0;  e.hasMoreElements(); i++)
      out.writeObject(e.nextElement());
    out.endGroup(typeName);
  }
}
