package bossa.util;

import java.math.BigInteger;

/**
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 */
public class Misc {
  // not instantiable
  private Misc() {}

  public static String arrayToString(Object[] array, int n,
                                     String lpar, String sep, String rpar) {
    StringBuffer sb = new StringBuffer();
    sb.append(lpar);
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        sb.append(sep);
      }
      sb.append(array[i]);
    }
    sb.append(rpar);
    return sb.toString();
  }

  public static String arrayToString(int[] array, int n,
                                     String lpar, String sep, String rpar) {
    StringBuffer sb = new StringBuffer();
    sb.append(lpar);
    for (int i = 0; i < n; i++) {
      if (i > 0) {
        sb.append(sep);
      }
      sb.append(array[i]);
    }
    sb.append(rpar);
    return sb.toString();
  }

  public static String arrayToString(Object[] array,
                                     String lpar, String sep, String rpar) {
    return arrayToString(array, array.length, lpar, sep, rpar);
  }

  public static String arrayToString(int[] array,
                                     String lpar, String sep, String rpar) {
    return arrayToString(array, array.length, lpar, sep, rpar);
  }

  public static void printStackTrace() {
    try {
      throw new Throwable();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  
  /**
   * floor(n / d)
   **/
  public static BigInteger div(BigInteger n, BigInteger d)
  throws ArithmeticException {
    if (n.signum() == d.signum()) {
      return n.divide(d);
    } else {
      BigInteger[] divrem = n.divideAndRemainder(d);
      BigInteger div = divrem[0];
      BigInteger rem = divrem[1];
      if (rem.signum() == 0) {
        return div;
      } else {
        return div.add(BigInteger.valueOf(-1));
      }
    }
  }

  /**
   * if d == -1, n must be != Integer.MIN_VALUE (otherwise, overflow would
   * occur and give the incorrect result Integer.MIN_VALUE)
   **/
  public static int div(int n, int d) throws ArithmeticException {
    if ((n >= 0 && d > 0)
        ||
        (n <= 0 && d < 0)) {
      return n / d;
    } else {
      int q = n / d;
      int r = n % d;
      if (r == 0) {
        return q;
      } else {
        return q - 1;
      }
    }
  }

  /**
   * n - div(n, d) * d
   **/
  public static BigInteger mod(BigInteger n, BigInteger d)
  throws ArithmeticException {
    BigInteger rem = n.remainder(d);
    if (rem.signum() == 0) {
      return rem;
    }
    if (n.signum() == d.signum()) {
      return rem;
    } else {
      return rem.add(d);
    }
  }

  public static int mod(int n, int d) throws ArithmeticException {
    int r = n % d;
    if (r == 0) {
      return r;
    } else if ((n >= 0 && d > 0)
               ||
               (n <= 0 && d < 0)) {
      return r;
    } else {
      return r + d;
    }
  }
}


