package bossa.util;


/**
 * A growable array of integers.
 * Same API as for TVect
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey.
 **/
public class IntVect {
  private int elementCount;
  private int[] elementData;
  private int capacityIncrement;
  private int defaultValue;

  public IntVect(int initialCapacity, int capacityIncrement, int defaultValue) {
    this.elementData = new int[initialCapacity];
    this.capacityIncrement = capacityIncrement;
    this.defaultValue = defaultValue;
  }
  public IntVect(int initialCapacity, int defaultValue) {
    this(initialCapacity, 0, defaultValue);
  }
  public IntVect(int defaultValue) {
    this(10, defaultValue);
  }
  public IntVect() {
    this(0);
  }

  // picked from Vector.java
  private void ensureCapacity(int minCapacity) {
    int oldCapacity = elementData.length;
    if (minCapacity > oldCapacity) {
      int[] oldData = elementData;
      int newCapacity = (capacityIncrement > 0) ?
        (oldCapacity + capacityIncrement) : (oldCapacity * 2);
      if (newCapacity < minCapacity) {
        newCapacity = minCapacity;
      }
      elementData = new int[newCapacity];
      System.arraycopy(oldData, 0, elementData, 0, elementCount);
      if (defaultValue != 0) {
        for (int i = oldCapacity; i < newCapacity; i++) {
          elementData[i] = defaultValue;
        }
      }
    }
  }

  public void setSize(int newSize, int defaultValue) {
    if (newSize > elementCount) {
      ensureCapacity(newSize);
      for (int i = elementCount; i < newSize; i++) {
        elementData[i] = defaultValue;
      }
    } else {
      for (int i = elementCount; i < newSize; i++) {
        elementData[i] = defaultValue;
      }
    }
    elementCount = newSize;
  }

  public void add(int x) {
    ensureCapacity(elementCount+1);
    elementData[elementCount++] = x;
  }
  private void checkIndex(int index) {
    if (index >= elementCount) {
      throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
    }
    if (index < 0) {
      throw new ArrayIndexOutOfBoundsException(index + " < 0");
    }
  }
  public int get(int index) {
    checkIndex(index);
    return elementData[index];
  }

  public void set(int index, int x) {
    checkIndex(index);
    elementData[index] = x;
  }
  
  public int[] toArray() {
    int[] result = new int[elementCount];
    System.arraycopy(elementData, 0, result, 0, elementCount);
    return result;
  }
  public int size() {
    return elementCount;
  }

  public String toString() {
    return Misc.arrayToString(elementData, elementCount, "[", ", ", "]");
  }
}
