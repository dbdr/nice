package bossa.util;

import java.util.*;

/**
 * A hash table that may contain several elements for the same key
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 */
public class HashMultiTable {
  // underlying hash table that contains linked lists of objects (Bucket)
  private Hashtable table = new Hashtable();
  
  private static class Bucket {
    Bucket(Object value, Bucket next) {
      this.value = value;
      this.next = next;
    }
    Object value;
    Bucket next;

    static int size(Bucket bucket) {
      int result = 0;
      Bucket walker = bucket;
      while (walker != null) {
        result++;
        walker = walker.next;
      }
      return result;
    }
    
    public String toString() {
      if (next == null) {
        return String.valueOf(value);
      } else {
        return value + ";" + next.toString();
      }
    }
  }
  private int elementCount = 0;

  public boolean containsKey(Object key) {
    return table.containsKey(key);
  }

  /**
   * Get the last entered value to which key is mapped
   * or null if no such element
   */
  public Object getLast(Object key) {
    Bucket bucket = (Bucket)table.get(key);
    if (bucket == null) {
      return null;
    } else {
      return bucket.value;
    }
  }

  /**
   * Add a mapping key -> value
   */
  public void put(Object key, Object value) {
    Bucket bucket = (Bucket)table.get(key);
    table.put(key, new Bucket(value, bucket));
    elementCount++;
  }

  /**
   * Returns a collection of all the values mapped from key
   * Last entered elements are enumerated first
   */
  public List getAll(final Object key) {
    if(!containsKey(key))
      return null;
    List res = new ArrayList();

    Bucket walker = (Bucket)HashMultiTable.this.table.get(key);
    while(walker != null)
      {
	res.add(walker.value);
	walker = walker.next;
      }
    return res;
  }

//    /**
//     * Returns an enumeration of all the elements in this table
//     * (Arbitrary order)
//     */
//    public Enumeration elements() {
//      final Enumeration buckets = table.elements();
//      return new Enumeration() {
//        // walker along one element of table
//        Bucket walker = null;
//        public boolean hasMoreElements() {
//          return walker != null || buckets.hasMoreElements();
//        }
//        public Object nextElement() {
//          if (walker == null) {
//            walker = (Bucket)buckets.nextElement();
//          }
//          Object result = walker.value;
//          walker = walker.next;
//          return result;
//        }
//      };
//    }

  /**
   * Returns the number of elements mapped from key
   **/
  public int elementCount(Object key) {
    return Bucket.size((Bucket)table.get(key));
  }

  /**
   * Returns the number of elements in this table
   */
  public int elementCount() {
    return elementCount;
  }

  /**
   * Returns the number of different keys in this table
   */
  public int keyCount() {
    return table.size();
  }

  
  public String toString() {
    return table.toString();
  }
}
