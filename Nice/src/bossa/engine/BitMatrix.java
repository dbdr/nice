package bossa.engine;

import java.util.Vector;

/**
 * A square matrix of bits, used to represent a relation between integers.
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
final public class BitMatrix implements Cloneable {
  /**
   * a vector of BitVectors. rows.elementAt(i) is the ith line of
   * the matrix. 
   **/
  private Vector rows;

  /**
   * Constructs an empty matrix
   **/
  public BitMatrix() {
    this(10);
  }
  private BitMatrix(int rowCapacity) {
    rows = new Vector(rowCapacity);
  }

  /**
   * Returns the number of rows and columns of this matrix
   **/
  public int size() {
    return rows.size();
  }

  /**
   * Set the size of the matrix. If the new size is greater than the current
   * size, new empty rows and columns are added. If the new size is less than
   * the current size, all rows and columns at index greater than newSize are
   * discarded.
   **/
  public void setSize(int newSize) {
    int oldSize = size();
    if (newSize < oldSize) {
      // clear the columns beyond newSize, so that subsequent calls to extend()
      // or setSize will find them empty.
      for (int i = 0; i < newSize; i++) {
        BitVector row = (BitVector)rows.elementAt(i);
        if (row != null) {
          row.truncate(newSize);
        }
      }
    }
    rows.setSize(newSize);
  }

  /**
   * Grow the matrix by one column and one row, initially empty. Returns the
   * index of the new row.
   **/
  public int extend() {
    int n = size();
    rows.addElement(null);
    return n;
  }
  
  /**
   * Get ith row. May return null if it is empty or if row is beyond
   * size()
   **/
  BitVector getRow(int i) {
    if (i < rows.size()) {
      return (BitVector)rows.elementAt(i);
    } else {
      return null;
    }
  }

  /**
   * get element at position (i, j), i.e., returns the value of i < j.
   * Assume i and j are valid indexes
   **/
  public boolean get(int i, int j) {
    BitVector row = (BitVector)rows.elementAt(i);
    return row != null && row.get(j);
  }

  /**
   * Set element at position (i, j) to true. Assume i and j are valid indexes.
   **/
  public void set(int i, int j) {
    BitVector row = (BitVector)rows.elementAt(i);
    if (row == null) {
      row = new BitVector(size());
      rows.setElementAt(row, i);
    }
    row.set(j);
  }

  /**
   * Set element at position (i, j) to false. Assume i and j are valid indexes.
   **/
  public void clear(int i, int j) {
    BitVector row = (BitVector)rows.elementAt(i);
    if (row != null) {
      row.clear(j);
    }
  }

  /**
   * Performs in-place reflexive transitive closure of the relation.
   **/
  public void closure() {
    // Warshall algorithm
    int n = size();
    for (int k = 0; k < n; k++) {
      BitVector row_k = (BitVector)rows.elementAt(k);
      if (row_k != null) {
        for (int i = 0; i < n; i++) {
          BitVector row_i = (BitVector)rows.elementAt(i);
          if (row_i != null && row_i.get(k)) { // i < k
            /* for all j such that k < j, add i < j */
            row_i.or(row_k);
          }
        }
      }
    }
    for (int i = 0; i < n; i++) {
      set(i, i);
    }
  }

  /**
   * Returns a newly-allocated BitMatrix initialized with the transpose
   * of this BitMatrix
   **/
  public BitMatrix transpose() {
    int n = rows.size();
    BitMatrix m = new BitMatrix(n);
    m.setSize(n);
    for (int i = 0; i < n; i++) {
      BitVector row = (BitVector)rows.elementAt(i);
      if (row != null) {
        int ncols = row.size();
        for (int j = 0; j < ncols; j++) {
          if (row.get(j)) {
            m.set(j, i);
          }
        }
      }
    }
    return m;
  }

  public Object clone() {
    try {
      BitMatrix m = (BitMatrix)super.clone();
      Vector v = (Vector)rows.clone();
      int nrows = rows.size();
      for (int i = 0; i < nrows; i++) {
        BitVector row = (BitVector)v.elementAt(i);
        if (row != null) {
          v.setElementAt(row.clone(), i);
        }
      }
      m.rows = v;
      return m;
    } catch (CloneNotSupportedException e) {
      /* Should never happen, since BitMatrix implements Cloneable */
      throw new InternalError();
    }
  }

  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    int rowCount = rows.size();
    boolean needSeparator = false;
    for (int row = 0; row < rowCount; row++) {
      BitVector rowBitVector = (BitVector)rows.elementAt(row);
      if (rowBitVector != null) {
        int ncols = rowBitVector.size();
        for (int col = 0; col < ncols; col++) {
          if (rowBitVector.get(col)) {
            if (needSeparator) {
              sb.append(", ");
            } else {
              needSeparator = true;
            }
            sb.append("(").append(row).append(",").append(col).append(")");
          }
        }
      }
    }
    sb.append("}");
    return sb.toString();
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof BitMatrix)) {
      return false;
    } else {
      Vector rows1 = this.rows;
      Vector rows2 = ((BitMatrix)obj).rows;
      int nrows1 = rows1.size();
      int nrows2 = rows2.size();
      int n = Math.min(nrows1, nrows2);
      for (int i = 0; i < n; i++) {
        BitVector row1 = (BitVector)rows1.elementAt(i);
        BitVector row2 = (BitVector)rows2.elementAt(i);
        if (row1 == null && row2 == null) continue;
        if (row1 == null && row2.isEmpty()) continue;
        if (row2 == null && row1.isEmpty()) continue;
        if (row1.equals(row2)) continue;
        return false;
      }
      if (nrows1 > n) {
        for (int i = n; i < nrows1; i++) {
          BitVector row1 = (BitVector)rows1.elementAt(i);
          if (row1 != null && !row1.isEmpty()) {
            return false;
          }
        }
      } else if (nrows2 > n) {
        for (int i = n; i < nrows2; i++) {
          BitVector row2 = (BitVector)rows2.elementAt(i);
          if (row2 != null && !row2.isEmpty()) {
            return false;
          }
        }
      }
      return true;
    } 
  }

  /**
   * Returns true if this BitMatrix is transitively closed
   **/
  public boolean isClosed() {
    int nrows = rows.size();
    for (int k = 0; k < nrows; k++) {
      BitVector row_k = getRow(k);
      if (row_k != null) {
        for (int i = 0; i < nrows; i++) {
          BitVector row_i = getRow(i);
          if (row_i != null && row_i.get(k)) {
            if (!row_k.includedIn(row_i)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }
  
  /**
   * Fills the array S with a topological sort of the relation on [m, size()[
   * described by this matrix. Assume that S is large enough to hold size() -
   * m integers. Assume 0 <= m <= this.size().
   *
   * <p>After a call to topologicalSort() and if the relation is a DAG, for
   * all i, j such that this.get(i, j) is true, i appears before j in S
   **/
  public void topologicalSort(int m, int[] S) {
    int n = size();
    int[] sortStack = new int[n - m];
    int sp = -1;                // index in sortStack
    int s = n - m;              // index in S
    BitVector touched = new BitVector(n);
    touched.fill(m);
    for (int i = m; i < n; i++) {
      if (!touched.get(i)) {
        touched.set(i);
        sortStack[++sp] = i;
        loop: while(sp >= 0) {
          int j = sortStack[sp];
          BitVector row_j = getRow(j);
          if (row_j != null) {
            int succ = row_j.getLowestSetBitNotIn(touched);
            if (succ >= 0) {
              // do the successor first
              touched.set(succ);
              sortStack[++sp] = succ;
              continue loop;
            }
          }
          // no untouched successor found
          sp--;                 // pop j
          S[--s] = j;           // push it on the sort array
        }
      }
    }
  }


  /**
   * Tests if this bit matrix is included in M on the first n columns and
   * lines. Assume m <= this.size() and m <= M.size(). If there exists (i, j)
   * < (m, m) such that this.get(i, j) but !M.get(i, j), return an array a
   * such that a[0] = i and a[1] = j. Otherwise, return null.
   **/
  public int[] includedIn(int m, BitMatrix M) {
    if (this == M) {
      return null;
    } else {
      for (int i = 0; i < m; i++) {
        BitVector row1 = this.getRow(i);
        if (row1 != null) {
          int j;
          BitVector row2 = M.getRow(i);
          if (row2 != null) {
            j = row1.getLowestSetBitNotIn(row2);
          } else {
            j = row1.getLowestSetBit();
          }
          if (0 <= j && j < m) {
            return new int[] {i, j};
          }
        }
      }
      return null;
    }
  }
  

  /**
   * In-place copy row src to row dest and clear row src. The previous results
   * of getRow(src) must not be used after this call.
   **/
  public void rowMove(int src, int dest) {
    rows.setElementAt(getRow(src), dest);
    rows.setElementAt(null, src);
  }

  /**
   * clear column col
   **/
  public void colClear(int col) {
    int n = size();
    for (int i = 0; i < n; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        row.clear(col);
      }
    }
  }

  /**
   * copy column src to column dest and clear column src.
   * Assume src != dest
   **/
  public void colMove(int src, int dest) {
    int n = size();
    for (int i = 0; i < n; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        row.bitCopy(src, dest);
      }
    }
  }

  /**
   * Returns the number of bits set in the square [0, n[ x [0, n[
   **/
  public int bitCount(int n) {
    int cnt = 0;
    if (size() < n) {
      n = size();
    }
    for (int i = 0; i < n; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        cnt += row.bitCount(n);
      }
    }
    return cnt;
  }

  /**
   * Merge row src and row dest, put the result in row dest. 
   * Assume src != dest. Previous results of getRow(src) must not be used
   * after this call.
   **/
  public void rowMerge(int src, int dest) {
    BitVector srcRow = getRow(src);
    if (srcRow != null) {
      BitVector destRow = getRow(dest);
      if (destRow == null) {
        rows.setElementAt(srcRow, dest);
      } else {
        destRow.or(srcRow);
      }
    }
    rows.setElementAt(null, src);
  }

  /**
   * Merge column src and column dest, put the result in column dest. Assume
   * src != dest.
   **/
  public void colMerge(int src, int dest) {
    int n = rows.size();
    for (int i = 0; i < n; i++) {
      BitVector row = getRow(i);
      if (row != null) {
        row.bitMerge(src, dest);
      }
    }
  }

  /**
   * Compute the set of y such that x <* y.
   **/
  public BitVector ideal(int x) {
    BitVector ideal = new BitVector(size());
    addIdeal(x, ideal);
    return ideal;
  }

  // assume !ideal.get(x)
  private void addIdeal(int x, BitVector ideal) {
    ideal.set(x);
    BitVector xUp = getRow(x);
    if (xUp != null) {
      int y;
      while ((y = xUp.getLowestSetBitNotIn(ideal)) >= 0) {
        addIdeal(y, ideal);
      }
    }
  }
      
}
