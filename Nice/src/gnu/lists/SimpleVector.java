// -*-Java-*-
// Copyright (c) 2001  Per M.A. Bothner and Brainfood Inc.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.lists;
import java.util.*;

/** A SimpleVector implement as a simple array plus a current size.
 *
 * Methods with the word "Buffer" are methods which use the underlying
 * array, ignoring the 'size' field.
 *
 * Can be used to implement CommonLisp simple vectors, but all simple
 * vectors are also adjustable (by re-allocating the buffer)
 * and have a fill pointer (the size field). */

public abstract class SimpleVector extends AbstractSequence implements Sequence
{
  /** The (current) number of elements.
   * Must always have size() >= 0 && size() <= getBufferLength(). */
  public int size;

  public final int size() { return size; }

  /**
   * Set the size to a specified value.
   * The data buffer is grown if needed, with new elements set to zero/null. If
   * size is less than the current value, removed values are set to zero/null..
   * (This is because if you decrease and then increase the vector the
   * should be zero/null, and it is cleaner and better for gc to do the
   * zeroing/nulling on remove rather than add.)
   * If you need to change the size without setting removed elements to
   * zero/null (e.g. to change Common Lisp's fill pointer) set size directly.
   */
  public void setSize(int size)
  {
    int oldSize = this.size;
    this.size = size;
    if (size < oldSize)
      clearBuffer(size, oldSize - size);
    else
      {
	int oldLength = getBufferLength();
	if (size > oldLength)
	  {
	    int newLength = oldLength < 16 ? 16 : 2 * oldLength;
	    setBufferLength(size > newLength ? size : newLength);
	  }
      }
  }

  /** Get the allocated length of the data buffer. */
  public abstract int getBufferLength();

  public abstract void setBufferLength(int length);

  protected boolean isAfter(int ipos, Object xpos)
  {
    return (ipos & 1) != 0;
  }

  protected int nextIndex(int ipos, Object xpos)
  {
    return ipos >>> 1;
  }

  public void makePosition(int index, boolean isAfter,
			   PositionContainer posSet, int posNumber)
  {
    posSet.setPosition(posNumber, (index << 1) | (isAfter ? 1 : 0), null);
    posSet.setSequence(posNumber, this);
  }

  /*
  protected void ensureSize(int space)
  {
    int oldLength = data.length;
    int newLength = size +
    if (size > space)
      setBufferLength(space < 16 ? 16 : 2 * space);
    this.size = size;
  }
  */

  protected abstract Object getBuffer();

  public Object get(int index)
  {
    if (index >= size)
      throw new IndexOutOfBoundsException();
    return getBuffer(index);
  }

  protected  Object getNext(int ipos, Object xpos)
  {
    int index = ipos >>> 1;
    return index >= size ? eofValue : getBuffer(index);
  }

  public int intAtBuffer(int index)
  {
    return Convert.toInt(getBuffer(index));
  }

  public int intAt(int index)
  {
    if (index >= size)
      throw new IndexOutOfBoundsException();
    return intAtBuffer(index);
  }

  public long longAt(int index)
  {
    if (index >= size)
      throw new IndexOutOfBoundsException();
    return longAtBuffer(index);
  }

  public long longAtBuffer(int index)
  {
    return Convert.toLong(getBuffer(index));
  }

  protected abstract Object getBuffer(int index);

  public Object set(int index, Object value)
  {
    if (index >= size)
      throw new IndexOutOfBoundsException();
    Object old = getBuffer(index);
    setBuffer(index, value);
    return old;
  }

  protected abstract Object setBuffer(int index, Object value);

  public void fill(Object value)
  {
    for (int i = size;  --i >= 0; )
      setBuffer(i, value);
  }

  public void fill(int fromIndex, int toIndex, Object value)
  {
    if (fromIndex < 0 || toIndex > size)
      throw new IndexOutOfBoundsException();
    for (int i = fromIndex;  i < toIndex;  i++)
      setBuffer(i, value);
  }

  public void shift(int srcStart, int dstStart, int count)
  {
    Object data = getBuffer();
    System.arraycopy(data, srcStart, data, dstStart, count);
  }

  public boolean add(Object o)
  {
    add(size, o);
    return true;
  }

  protected void add(PositionContainer posSet, int posNumber, Object value)
  {
    int ipos = posSet.getPositionInt(posNumber);
    add(ipos >>> 1, value);
    posSet.setPosition(posNumber, ipos + 2, null);
  }

  public void add(int index, Object o)
  {
    int newSize = size + 1;
    size = newSize;
    int length = getBufferLength();
    if (newSize > length)
      setBufferLength(length < 16 ? 16 : 2 * length);
    this.size = newSize;
    if (size != index)
      shift(index, index + 1, size - index);
    set(index, o);
  }

  public boolean addAll(int index, Collection c)
  {
    boolean changed = false;
    int count = c.size();
    setSize(size + count);
    shift(index, index + count, size - count - index);
    for (Iterator it = c.iterator();  it.hasNext(); )
      {
	set(index++, it.next());
	changed = true;
      }
    return changed;
  }

  protected abstract void clearBuffer(int start, int count);

  protected void remove(int ipos0, Object xpos0, int ipos1, Object xpos1)
  {
    ipos0 = ipos0 >>> 1;
    ipos1 = ipos1 >>> 1;
    if (ipos0 < 0 || ipos0 > ipos1 || ipos1 >= size)
      throw new IndexOutOfBoundsException();
    shift(ipos1, ipos0, size - ipos1);
    int count = ipos1 - ipos0;
    size = size - count;
    clearBuffer(size, count);
  }

  protected void remove(int ipos, Object xpos, int count)
  {
    int index = ipos >>> 1;
    int ipos0, ipos1;
    if (count >= 0)
      {
	ipos0 = index;
	ipos1 = index + count;
      }
    else
      {
	ipos0 = index + count;
	ipos1 = index;
	count = - count;
      }
    if (ipos0 < 0 || ipos1 >= size)
      throw new IndexOutOfBoundsException();
    shift(ipos1, ipos0, size - ipos1);
    size = size - count;
    clearBuffer(size, count);
  }

  public Object remove(int index)
  {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException();
    Object result = get(index);
    shift(index + 1, index, 1);
    size = size - 1;
    clearBuffer(size, 1);
    return result;
  }

  public boolean remove(Object o)
  {
    int index = indexOf(o);
    if (index < 0)
      return false;
    Object result = get(index);
    shift(index + 1, index, 1);
    size = size - 1;
    clearBuffer(size, 1);
    return true;
  }

  public boolean removeAll(Collection c)
  {
    boolean changed = false;
    int j = 0;
    for (int i = 0;  i < size;  i++)
      {
        Object value = get(i);
        if (c.contains(value))
          {
            changed = true;
          }
	else
	{
	  if (changed)
	    set(j, value);
	  j++;
	}
      }
    setSize(j);
    return changed;
  }

  public boolean retainAll(Collection c)
  {
    boolean changed = false;
    int j = 0;
    for (int i = 0;  i < size;  i++)
      {
        Object value = get(i);
        if (! c.contains(value))
          {
            changed = true;
          }
	else
	  {
	    if (changed)
	      set(j, value);
	    j++;
	  }
      }
    setSize(j);
    return changed;
  }

  public void clear ()
  {
    setSize(0);
  }

  /** This is convenience hack for printing "uniform vectors" (srfi 4).
   * It may go away without notice! */
  public String getTag() { return null; }

  public void consume(int start, int length, Consumer out)
  {
    consume(start << 1, null, (start + length) << 1, null, out);
  }

  public boolean consumeNext(int ipos, Object xpos, Consumer out)
  {
    int index = ipos >>> 1;
    if (index >= size)
      return false;
    out.writeObject(getBuffer(index));
    return true;
  }

  protected void consume(int iposStart, Object xposStart,
			 int iposEnd, Object xposEnd, Consumer out)
  {
    if (out.ignoring())
      return;
    int i = iposStart >>> 1;
    int end = iposEnd >>> 1;
    for (;  i < end;  i++)
      out.writeObject(getBuffer(i));
  }

  public int getNextKind(int ipos, Object xpos)
  {
    return hasNext(ipos, xpos) ? getElementKind() : EOF_VALUE;
  }

  public int getElementKind()
  {
    return OBJECT_VALUE;
  }
}
