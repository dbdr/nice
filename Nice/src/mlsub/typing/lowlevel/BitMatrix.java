package mlsub.typing.lowlevel;

import java.util.ArrayList;

/**
 * A square matrix of bits, used to represent a relation between integers.
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
final public class BitMatrix implements Cloneable {
  /**
   * a vector of BitVectors. rows.get(i) is the ith line of
   * the matrix. 
   **/
  private BitVector[] rows;
  private int size;
  
  /**
   * Constructs an empty matrix
   **/
  public BitMatrix() {
    this(10);
  }
  private BitMatrix(int rowCapacity) {
    if(rowCapacity==0)
      rowCapacity=5;
    rows = new BitVector[rowCapacity];
  }

  /**
   * Returns the number of rows and columns of this matrix
   **/
  public int size() {
    return size;
  }

  /**
   * Set the size of the matrix. If the new size is greater than the current
   * size, new empty rows and columns are added. If the new size is less than
   * the current size, all rows and columns at index greater than newSize are
   * discarded.
   **/
  public void setSize(int newSize) {
    if (newSize < size) {
      // clear the columns beyond newSize, so that subsequent calls to extend()
      // or setSize will find them empty.
      for (int i = 0; i < newSize; i++) {
        BitVector row = rows[i];
        if (row != null) {
          row.truncate(newSize);
        }
      }
      for (int i = size; i>newSize; )
	rows[--i] = null;
    }
      else {
        if(rows.length<newSize)
	  {
	    BitVector[] newRows = new BitVector[rows.length*2];
	    System.arraycopy(rows, 0, newRows, 0, rows.length);
	    rows = newRows;
	  }
      }
    size = newSize;
  }

  /**
   * Grow the matrix by one column and one row, initially empty. Returns the
   * index of the new row.
   **/
  public int extend() {
    setSize(size+1);
    return size-1;
  }
  
  /**
   * Get ith row. May return null if it is empty or if row is beyond
   * size()
   **/
  BitVector getRow(int i) {
    if (i < size) {
      return rows[i];
    } else {
      return null;
    }
  }

  /**
   * get element at position (i, j), i.e., returns the value of i < j.
   * Assume i and j are valid indexes
   **/
  public boolean get(int i, int j) {
    BitVector row = rows[i];
    return row != null && row.get(j);
  }

  /**
   * Set element at position (i, j) to true. Assume i and j are valid indexes.
   **/
  public void set(int i, int j) {
    BitVector row = rows[i];
    if (row == null) {
      row = new BitVector(size);
      rows[i] = row;
    }
    row.set(j);
  }

  /**
   * Set element at position (i, j) to false. Assume i and j are valid indexes.
   **/
  public void clear(int i, int j) {
    BitVector row = rows[i];
    if (row != null) {
      row.clear(j);
    }
  }

  /**
   * Performs in-place reflexive transitive closure of the relation.
   **/
  public void closure() {
    // Warshall algorithm
    for (int k = 0; k < size; k++) {
      BitVector row_k = rows[k];
      if (row_k != null) {
        for (int i = 0; i < size; i++) {
          BitVector row_i = rows[i];
          if (row_i != null && row_i.get(k)) { // i < k
            /* for all j such that k < j, add i < j */
            row_i.or(row_k);
          }
        }
      }
    }
    for (int i = 0; i < size; i++) {
      set(i, i);
    }
  }

  /**
   * Returns a newly-allocated BitMatrix initialized with the transpose
   * of this BitMatrix
   **/
  public BitMatrix transpose() {
    BitMatrix m = new BitMatrix(size);
    m.setSize(size);
    for (int i = 0; i < size; i++) {
      BitVector row = rows[i];
      if (row != null) {
        int ncols = row.size();
	for (int j = row.getLowestSetBit();
	     j>=0;
	     j = row.getLowestSetBit(j+1))
	  m.set(j, i);
        /*
	for (int j = 0; j < ncols; j++) {
	  if (row.get(j)) {
	    m.set(j, i);
	  }
        }
	*/
      }
    }
    return m;
  }

  public Object clone() {
    try {
      BitMatrix m = (BitMatrix)super.clone();
      BitVector[] v = (BitVector[])rows.clone();
      
      for (int i = 0; i < size; i++) {
        BitVector row = v[i];
        if (row != null) {
          v[i] = (BitVector) row.clone();
        }
      }
      m.rows = v;
      m.size = size;
      return m;
    } catch (CloneNotSupportedException e) {
      throw new InternalError
	("Should never happen, since BitMatrix implements Cloneable");
    }
  }

  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    boolean needSeparator = false;
    for (int row = 0; row < size; row++) {
      BitVector rowBitVector = rows[row];
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
      BitMatrix that = (BitMatrix) obj;
      BitVector[] rows1 = this.rows;
      BitVector[] rows2 = that.rows;
      int nrows1 = size;
      int nrows2 = that.size;
      int n = Math.min(nrows1, nrows2);
      for (int i = 0; i < n; i++) {
        BitVector row1 = rows1[i];
        BitVector row2 = rows2[i];
        if (row1 == null && row2 == null) continue;
        if (row1 == null && row2.isEmpty()) continue;
        if (row2 == null && row1.isEmpty()) continue;
        if (row1.equals(row2)) continue;
        return false;
      }
      if (nrows1 > n) {
        for (int i = n; i < nrows1; i++) {
          BitVector row1 = rows1[i];
          if (row1 != null && !row1.isEmpty()) {
            return false;
          }
        }
      } else if (nrows2 > n) {
        for (int i = n; i < nrows2; i++) {
          BitVector row2 = rows2[i];
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
    for (int k = 0; k < size; k++) {
      BitVector row_k = getRow(k);
      if (row_k != null) {
        for (int i = 0; i < size; i++) {
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
    int n = size;
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
    rows[dest] = getRow(src);
    rows[src] = null;
  }

  /**
   * clear column col
   **/
  public void colClear(int col) {
    for (int i = 0; i < size; i++) {
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
    for (int i = 0; i < size; i++) {
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
    if (size < n) {
      n = size;
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
        rows[dest] = srcRow;
      } else {
        destRow.or(srcRow);
      }
    }
    rows[src] = null;
  }

  /**
   * Merge column src and column dest, put the result in column dest. Assume
   * src != dest.
   **/
  public void colMerge(int src, int dest) {
    for (int i = 0; i < size; i++) {
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
    BitVector ideal = new BitVector(size);
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
