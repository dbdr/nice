package bossa.engine;

//import fr.ensmp.cma.jazz.typing.Unsatisfiable;
//import fr.ensmp.cma.jazz.util.*;
//import fr.ensmp.cma.jazz.S;

/**
 * A repository for static methods used during satisfiability test
 *
 * @author Alexandre Frey
 * @version $Revision$, $Date$
 **/
final class Satisfier {
  // not instantiable
  private Satisfier() {}

  static int[] compileStrategy(BitMatrix C, BitMatrix Ct, int m, int n) {
    int[] strategy = new int[n - m];
    C.topologicalSort(m, strategy);
    return strategy;
  }

  private static boolean satisfiable = false;
  private static class Satisfiable extends Exception {}

  private static void enumerate(int[] strategy, DomainVector domains,
                                BitMatrix C, BitMatrix Ct,
                                BitMatrix R, BitMatrix Rt,
                                int m, int n,
                                BitVector observers,
                                LowlevelSolutionHandler handler)
  throws LowlevelUnsatisfiable, Satisfiable {
    domains.gfp(R, Rt, C, Ct, strategy);
    boolean isObserver = true;
    // try first the observers
    int x = domains.chooseDomain(observers);
    if (x < 0) {
      isObserver = false;
      // no more uninstantiated observers
      x = domains.chooseDomain();
      if (x < 0) {
        // no more domains at all, the constraint has been satisfied
        handler.handle(domains);

        // backtrack
        throw new Satisfiable();
      }
    }
    Domain dx = (Domain)domains.getDomain(x).clone();
    for (int a = dx.getLowestSetBit();
         a >= 0;
         a = dx.getNextBit(a)) {
      DomainVector domainsCopy = (DomainVector)domains.clone();
      try {
        domainsCopy.getDomain(x).instantiate(a);
        enumerate(strategy, domainsCopy, C, Ct, R, Rt, m, n,
                  observers, handler);
      }
      catch (LowlevelUnsatisfiable e) {
        // try another value
      }
      catch (Satisfiable e) {
        if (!isObserver) {
          // ok, we found a solution and reported it
          // no need to try another value for the non-observer x
          throw e;
        } else {
          // try another value for the observer
        }
      }
    }
    throw new LowlevelUnsatisfiable();
  }
  
  private static void enumerate(int[] strategy, DomainVector domains,
                                BitMatrix C, BitMatrix Ct,
                                BitMatrix R, BitMatrix Rt,
                                int m, int n,
                                LowlevelSolutionHandler handler)
  throws LowlevelUnsatisfiable, Satisfiable {
    domains.gfp(R, Rt, C, Ct, strategy);
    
    int x = domains.chooseDomain();
    if (x < 0) {
      // no more domains to be instantiated: a solution has been found
      satisfiable = true;
      if (handler == null) {
        throw new Satisfiable();
      }
      handler.handle(domains);
      throw new LowlevelUnsatisfiable();
    }
    Domain dx = (Domain)domains.getDomain(x).clone();
    // iterate through the elements of dx
    for (int a = dx.getLowestSetBit();
         a >= 0;
         a = dx.getNextBit(a)) {
      DomainVector domainsCopy = (DomainVector)domains.clone();
      try {
        domainsCopy.getDomain(x).instantiate(a);
        enumerate(strategy, domainsCopy, C, Ct, R, Rt, m, n, handler);

        // XXX: reachable ?
        throw new Satisfiable();
      }
      catch (LowlevelUnsatisfiable _) {
        // try another value
      }
    }
    throw new LowlevelUnsatisfiable();
  }
    
  static void enumerateSolutions(int[] strategy, DomainVector domains,
                                 BitMatrix C, BitMatrix Ct,
                                 BitMatrix R, BitMatrix Rt,
                                 int m, int n,
                                 LowlevelSolutionHandler handler)
  throws LowlevelUnsatisfiable {
    try {
      satisfiable = false;
      domains.initGfpCardinals();
      enumerate(strategy, domains, C, Ct, R, Rt, m, n, handler);
    }
    catch (Satisfiable e) {}
    catch (LowlevelUnsatisfiable e) {
      if (!satisfiable) {
        throw e;
      } else {
        /* the exception was thrown to request for more solutions
         * but there aren't any more
         */
      }
    }
  }

  static void enumerateSolutions(int[] strategy, DomainVector domains,
                                 BitMatrix C, BitMatrix Ct,
                                 BitMatrix R, BitMatrix Rt,
                                 int m, int n,
                                 BitVector observers,
                                 LowlevelSolutionHandler handler) {
    try {
      domains.initGfpCardinals();
      enumerate(strategy, domains, C, Ct, R, Rt, m, n, observers, handler);
    }
    catch (Satisfiable e) {}
    catch (LowlevelUnsatisfiable e) {}
  }
  
  static void satisfy(int[] strategy, DomainVector domains,
                      BitMatrix C, BitMatrix Ct,
                      BitMatrix R, BitMatrix Rt,
                      int m, int n)
  throws LowlevelUnsatisfiable {
    try {
      domains.initGfpCardinals();
      enumerate(strategy, domains, C, Ct, R, Rt, m, n, null);
    }
    catch (Satisfiable e) {}
  }
}
