// -*-Java-*-
// Copyright (c) 2001  Per M.A. Bothner and Brainfood Inc.
// This is free software;  for terms and warranty disclaimer see ./COPYING.
package gnu.lists;

import java.util.NoSuchElementException;

/**
 * A position in a sequence (list).
 *
 * Conceptually similar to Java2's ListIterator, but we use the name "Position"
 * to indicate that it can be used to both indicate a position in a sequence
 * and to iterate through a sequence.  If you use a SeqPosition as a
 * "position", you would not modify if (though it is possible the offset
 * of the position in the sequence may change due to other update operations
 * on the sequence).  If you use a SeqPosition as an "iterator", you would
 * initialize it to some beginnning position, and then modify the current
 * position of the SeqPosition so it refers to successive elements.
 *
 * See the <a href="package-summary.html#iteration">package overview</a>
 * for more information.
 */

public class SeqPosition
implements
    java.util.ListIterator,
    PositionContainer, java.util.Enumeration
{
  /**
   * The Sequence relative to which ipos and xpos have meaning.
   * This is normally the same as the Sequence we iterate through.
   * However, if this is a TreePosition, it may an ancestor instead.
   */
  public AbstractSequence sequence;

  /**
   * An integer that (together with xpos) indicates the current position.
   * The actual value has no meaning, except as interpreted by sequence.
   */
  public int ipos;

  /**
   * An Object that (together with ipos) indicates the current position.
   * The actual value has no meaning, except as interpreted by sequence.
   */
  public Object xpos;

  public SeqPosition()
  {
  }

  public SeqPosition(AbstractSequence seq)
  {
    this.sequence = seq;
  }

  public SeqPosition(AbstractSequence seq, int offset, boolean isAfter)
  {
    this.sequence = seq;
    seq.makePosition(offset, isAfter, this, 0);
  }

  public SeqPosition(AbstractSequence seq, int ipos, Object xpos)
  {
    this.sequence = seq;
    this.ipos = ipos;
    this.xpos = xpos;
  }

  /** Creates a new SeqPosition, from a position triple.
   * The position triple is copied (using copyPosition).
   */
  public static SeqPosition make(AbstractSequence seq, int ipos, Object xpos)
  {
    SeqPosition pos = new SeqPosition();
    seq.copyPosition(ipos, xpos, pos, 0);
    return pos;
  }
 
  public final void gotoStart(AbstractSequence seq)
  {
    if (sequence != null)
      sequence.releasePosition(ipos, xpos);
    sequence = seq;
    seq.makeStartPosition(this, 0);
  }

  public final void gotoEnd(AbstractSequence seq)
  {
    if (sequence != null)
      sequence.releasePosition(ipos, xpos);
    sequence = seq;
    seq.makeEndPosition(this, 0);
  }

  /** True if there is an element following the current position.
   * False if we are at the end.  See java.util.Enumeration. */
  public boolean hasMoreElements()
  {
    return sequence.hasNext(ipos, xpos);
  }

  /** See java.util.Iterator. */
  public boolean hasNext()
  {
    return sequence.hasNext(ipos, xpos);
  }

  /** Return a code (defined in Sequence) for the type of the next element. */
  public int getNextKind()
  {
    return sequence.getNextKind(ipos, xpos);
  }

  /** Get the "tag name" for the next element, if any. */
  public String getNextTypeName()
  {
    return sequence.getNextTypeName(ipos, xpos);
  }

  /** Get the "tag object" for the next element, if any. */
  public Object getNextTypeObject()
  {
    return sequence.getNextTypeObject(ipos, xpos);
  }

  /** See java.util.Iterator. */
  public boolean hasPrevious()
  {
    return sequence.hasPrevious(ipos, xpos);
  }

  /** See java.util.ListIterator. */
  public Object next()
  {
    Object result = sequence.getNext(ipos, xpos);
    if (result == Sequence.eofValue || ! sequence.gotoNext(this))
      throw new NoSuchElementException();
    return result;
  }

  /** Move one element forwards, if possible.
   * @returns if we succeeded in moving forwards (i.e. not at end of sequence).
   */
  public final boolean gotoNext()
  {
    return sequence.gotoNext(this);
  }

  /** See java.util.ListIterator. */
  public Object previous()
  {
    Object result = sequence.getPrevious(ipos, xpos);
    if (result == Sequence.eofValue || ! sequence.gotoPrevious(this, 0))
      throw new NoSuchElementException();
    return result;
  }

  /** See java.util.Enumeration. */
  public Object nextElement()
    throws NoSuchElementException
  {
    Object result = sequence.getNext(ipos, xpos);
    if (result == Sequence.eofValue || ! sequence.gotoNext(this))
      throw new NoSuchElementException();
    return result;
  }

  /**
   * Get element following current position.
   * Does not move the position, in contrast to nextElement method.
   * @return EOF if at end of sequence, otherwise the value following.
   */
  public Object getNext()
  {
    return sequence.getNext(ipos, xpos);
  }

  public Object getPrevious()
  {
    return sequence.getPrevious(ipos, xpos);
  }

  /** See java.util.Iterator. */
  public final int nextIndex()
  {
    return sequence.nextIndex(ipos, xpos);
  }

  public final int fromEndIndex()
  {
    return sequence.fromEndIndex(ipos, xpos);
  }

  public int getContainingSequenceSize()
  {
    return sequence.getContainingSequenceSize(ipos, xpos);
  }

  /** See java.util.Iterator. */
  public final int previousIndex()
  {
    return sequence.nextIndex(ipos, xpos) - 1;
  }

  /** Does the position pair have the "isAfter" property?
   * I.e. if something is inserted at the position, will
   * the iterator end up being after the new data?
   * A toNext() or next() command should set isAfter() to true;
   * a toPrevious or previous command should set isAfter() to false.
   */
  public final boolean isAfter()
  {
    return sequence.isAfter(ipos, xpos);
  }

  public final void set(Object value)
  {
    if (sequence.isAfter(ipos, xpos))
      sequence.setPrevious(ipos, xpos, value);
    else
      sequence.setNext(ipos, xpos, value);
  }

  public void remove()
  {
    sequence.remove(ipos, xpos, isAfter() ? -1 : 1);
  }

  public void add(Object o)
  { 
    sequence.add(this, 0, o);
  }

  /** Implements PositionContainer. */
  public int getPositionInt(int positionNumber) { return ipos; }
  /** Implements PositionContainer. */
  public Object getPositionPtr(int positionNumber) { return xpos; }
  /** Implements PositionContainer. */
  public void setPosition(int positionNumber, int ipos, Object xpos)
  { this.ipos = ipos;  this.xpos = xpos; }
  /** Implements PositionContainer. */
  public void setSequence(int positionNumber, AbstractSequence seq)
  { sequence = seq; }
  /** Implements PositionContainer. */
  public int countPositions() { return 1; }

  public void init(AbstractSequence seq, int index, boolean isAfter)
  {
    if (sequence != null)
      sequence.releasePosition(ipos, xpos);
    sequence = seq;
    seq.makePosition(index, isAfter, this, 0);
  }

  public void init(SeqPosition pos)
  {
    if (sequence != null)
      sequence.releasePosition(ipos, xpos);
    sequence = pos.sequence;
    sequence.copyPosition(pos.ipos, pos.xpos, this, 0);
  }

  public void release()
  {
    if (sequence != null)
      {
	sequence.releasePosition(ipos, xpos);
	sequence = null;
      }
  }

  public void finalize()
  {
    release();
  }

  public String toString()
  {
    StringBuffer sbuf = new StringBuffer(60);
    sbuf.append('{');
    if (sequence == null)
      sbuf.append("null sequence");
    else
      {
	sbuf.append(sequence.getClass().getName());
	sbuf.append('@');
	sbuf.append(System.identityHashCode(sequence));
      }
    sbuf.append(" ipos: ");
    sbuf.append(ipos);
    sbuf.append(" xpos: ");
    sbuf.append(xpos);
    sbuf.append('}');
    return sbuf.toString();
  }
}
// This is for people using the Emacs editor:
// Local Variables:
// c-file-style: "gnu"
// tab-width: 8
// indent-tabs-mode: t
// End:
