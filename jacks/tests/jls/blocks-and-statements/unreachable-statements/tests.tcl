tcltest::test 14.20-valid-1 { Example of valid unreachable code, since value
        analysis is not made } {
    empty_main T1420valid1 {
        int n = 5, k;
        while (n > 7) k = 2;
    }
} PASS

# the reachability of empty blocks is tested with other statements
tcltest::test 14.20-block-1 { Empty blocks complete normally iff reachable } {
    empty_main T1420block1 {
        {}
        int i;
    }
} PASS

tcltest::test 14.20-block-2 { Non-empty blocks complete normally iff last statement does } {
    empty_main T1420block2 {
        { int i; }
        int j;
    }
} PASS

tcltest::test 14.20-block-3 { Non-empty blocks complete normally iff last statement does } {
    empty_main T1420block3 {
        { return; }
        int i;
    }
} FAIL

tcltest::test 14.20-block-4 { Non-empty blocks complete normally iff last statement does } {
    empty_main T1420block4 {
        { return; }
        ;
    }
} FAIL

tcltest::test 14.20-block-5 { Non-empty blocks complete normally iff last statement does } {
    empty_main T1420block5 {
        { return; }
        {}
    }
} FAIL

tcltest::test 14.20-block-6 { Non-empty blocks complete normally iff last statement does } {
    empty_main T1420block6 {
        { return; }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-block-7 { First block statement is reachable iff block is;
        all others iff the preceding statement completes normally } {
    empty_main T1420block7 {
        int i;
        int j;
    }
} PASS

tcltest::test 14.20-block-8 { First block statement is reachable iff block is;
        all others iff the preceding statement completes normally } {
    empty_main T1420block8 {
        return;
        int i;
    }
} FAIL

tcltest::test 14.20-block-9 { First block statement is reachable iff block is;
        all others iff the preceding statement completes normally } {
    empty_main T1420block9 {
        return;
        ;
    }
} FAIL

tcltest::test 14.20-block-10 { First block statement is reachable iff block is;
        all others iff the preceding statement completes normally } {
    empty_main T1420block10 {
        return;
        {}
    }
} FAIL

tcltest::test 14.20-block-11 { First block statement is reachable iff block is;
        all others iff the preceding statement completes normally } {
    empty_main T1420block11 {
        return;
        { int x=3; }
    }
} FAIL

# local declarations
tcltest::test 14.20-local-1 { Local class declarations can complete normally
        iff reachable } {
    empty_main T1420local1 {
        class bar {}
        int i;
    }
} PASS

tcltest::test 14.20-local-2 { Local class declarations can complete normally
        iff reachable } {
    empty_main T1420local2 {
        return;
        class bar {}
    }
} FAIL

tcltest::test 14.20-local-3 { Local variable declarations can complete normally
        iff reachable } {
    empty_main T1420local3 {
        int i;
        int j;
    }
} PASS

tcltest::test 14.20-local-4 { Local variable declarations can complete normally
        iff reachable } {
    empty_main T1420local4 {
        return;
        int j;
    }
} FAIL

# the reachability of empty statements is tested with other statements
tcltest::test 14.20-empty-1 { The empty statement completes normally iff reachable } {
    empty_main T1420empty1 {
        ;
        int i;
    }
} PASS

tcltest::test 14.20-label-1 { Labeled statements can complete normally if
        contained statement can as well } {
    empty_main T1420label1 {
        int i;
        a: i = 1;
        int j;
    }
} PASS

tcltest::test 14.20-label-2 { Labeled statements can complete normally if
        it contains a reachable break to exit the label } {
    empty_main T1420label2 {
        a: break a;
        int i;
    }
} PASS

tcltest::test 14.20-label-3 { Labeled statements can complete normally if
        it contains a reachable break to exit the label } {
    empty_main T1420label3 {
        a: while (true)
            break a;
        int i;
    }
} PASS

tcltest::test 14.20-label-4 { Labeled statements without exiting break or
        normal completion of contained statement do not complete normally } {
    empty_main T1420label4 {
        a: return;
        int i;
    }
} FAIL

tcltest::test 14.20-label-5 { Labeled statements without exiting break or
        normal completion of contained statement do not complete normally } {
    empty_main T1420label5 {
        a: return;
        ;
    }
} FAIL

tcltest::test 14.20-label-6 { Labeled statements without exiting break or
        normal completion of contained statement do not complete normally } {
    empty_main T1420label6 {
        a: return;
        {}
    }
} FAIL

tcltest::test 14.20-label-7 { Labeled statements without exiting break or
        normal completion of contained statement do not complete normally } {
    empty_main T1420label7 {
        a: return;
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-label-8 { The break is interrupted by the try-finally } {
    empty_main T1420label8 {
        a: try {
            break a;
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-label-9 { The break is interrupted by the try-finally } {
    empty_main T1420label9 {
        a: try {
            break a;
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-label-10 { The break is interrupted by the try-finally } {
    empty_main T1420label10 {
        a: try {
            break a;
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-label-11 { The break is interrupted by the try-finally } {
    empty_main T1420label11 {
        a: try {
            break a;
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-label-12 { The break is interrupted by the try-finally } {
    empty_main T1420label12 {
        a: try {
            throw new Exception();
        } catch (Exception e) {
            break a;
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-label-13 { The break is interrupted by the try-finally } {
    empty_main T1420label13 {
        a: try {
            throw new Exception();
        } catch (Exception e) {
            break a;
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-label-14 { The break is interrupted by the try-finally } {
    empty_main T1420label14 {
        a: try {
            throw new Exception();
        } catch (Exception e) {
            break a;
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-label-15 { The break is interrupted by the try-finally } {
    empty_main T1420label15 {
        a: try {
            throw new Exception();
        } catch (Exception e) {
            break a;
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-expression-1 { Expression statements can complete normally
        iff reachable } {
    empty_main T1420expression1 {
        System.out.println();
        int i;
        i = 1;
        i++;
        --i;
        new Object();
        new Object() {};
    }
} PASS

tcltest::test 14.20-expression-2 { Expression statements can complete normally
        iff reachable (even if stricter analysis shows otherwise) } {
    empty_class T1420expression2 {
        void die() { throw new RuntimeException(); }
        void foo() {
            System.exit(1); // never returns
            die(); // always abrupt exit with exception
            Object o = new Object[Integer.MAX_VALUE][Integer.MAX_VALUE][Integer.MAX_VALUE]; // almost a guaranteed OutOfMemoryError
            foo(); // recursion short-circuits rest of method
            int i; // will never get here, but it is reachable
        }
    }
} PASS

# see blocks-and-statements/switch-statement for a reachability ambiguity
# issue when all 256 cases of a byte switch are explicitly labeled.
tcltest::test 14.20-switch-1 { A switch statement can complete normally if:
        The last statement in the switch block can complete normally. } {
    empty_main T1420switch1 {
        switch (args.length) {
            case 0:
            int i;
        }
        int j;
    }
} PASS

tcltest::test 14.20-switch-2 { A switch statement can complete normally if:
        The switch block is empty or contains only switch labels. } {
    empty_main T1420switch2 {
        switch (args.length) {}
        int i;
    }
} PASS
        
tcltest::test 14.20-switch-3 { A switch statement can complete normally if:
        The switch block is empty or contains only switch labels. } {
    empty_main T1420switch3 {
        switch (args.length) {
            case 0:
        }
        int i;
    }
} PASS
        
tcltest::test 14.20-switch-4 { A switch statement can complete normally if:
        There is at least one switch label after the last switch block statement group. } {
    empty_main T1420switch4 {
        switch (args.length) {
            case 0: return;
            case 1:
        }
        int i;
    }
} PASS

tcltest::test 14.20-switch-5 { A switch statement can complete normally if:
        The switch block does not contain a default label. } {
    empty_main T1420switch5 {
        switch (args.length) {
            case 0: return;
        }
        int i;
    }
} PASS

tcltest::test 14.20-switch-6 { A switch statement can complete normally if:
        There is a reachable break statement that exits the switch statement. } {
    empty_main T1420switch6 {
        switch (args.length) {
            default: return;
            case 0: break;
        }
        int i;
    }
} PASS

tcltest::test 14.20-switch-7 { A switch statement cannot complete normally if
        all five preceding conditions are not met } {
    empty_main T1420switch7 {
        switch (args.length) {
            default: return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-switch-8 { A switch statement cannot complete normally if
        all five preceding conditions are not met } {
    empty_main T1420switch8 {
        switch (args.length) {
            default: return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-switch-9 { A switch statement cannot complete normally if
        all five preceding conditions are not met } {
    empty_main T1420switch9 {
        switch (args.length) {
            default: return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-switch-10 { A switch statement cannot complete normally if
        all five preceding conditions are not met } {
    empty_main T1420switch10 {
        switch (args.length) {
            default: return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-switch-11 { A switchblock statement is reachable if it
        bears a switch label } {
    switch_labels T1420switch11 int {
        case 0: return;
        case 1: boolean b;
    }
} PASS

tcltest::test 14.20-switch-12 { A switchblock statement is reachable if a prior
        statement can complete normally } {
    switch_labels T1420switch12 int {
        case 0:
            boolean b1;
            boolean b2;
    }
} PASS

tcltest::test 14.20-switch-13 { A switchblock statement is unreachable iff it
        bears no label and the prior statement cannot complete normally } {
    switch_labels T1420switch13 int {
        case 0: 
            return;
            int i;
    }
} FAIL

tcltest::test 14.20-switch-14 { A switchblock statement is unreachable iff it
        bears no label and the prior statement cannot complete normally } {
    switch_labels T1420switch14 int {
        case 0: 
            return;
            ;
    }
} FAIL

tcltest::test 14.20-switch-15 { A switchblock statement is unreachable iff it
        bears no label and the prior statement cannot complete normally } {
    switch_labels T1420switch15 int {
        case 0: 
            return;
            {}
    }
} FAIL

tcltest::test 14.20-switch-16 { A switchblock statement is unreachable iff it
        bears no label and the prior statement cannot complete normally } {
    switch_labels T1420switch16 int {
        case 0: 
            return;
            { int x=3; }
    }
} FAIL

tcltest::test 14.20-switch-17 { The break is interrupted by the try-finally } {
    empty_main T1420switch17 {
        switch (args.length) {
            default:
            try {
                break;
            } finally {
                return;
            }
        }
        int i;
    }
} FAIL

tcltest::test 14.20-switch-18 { The break is interrupted by the try-finally } {
    empty_main T1420switch18 {
        switch (args.length) {
            default:
            try {
                break;
            } finally {
                return;
            }
        }
        ;
    }
} FAIL

tcltest::test 14.20-switch-19 { The break is interrupted by the try-finally } {
    empty_main T1420switch19 {
        switch (args.length) {
            default:
            try {
                break;
            } finally {
                return;
            }
        }
        {}
    }
} FAIL

tcltest::test 14.20-switch-20 { The break is interrupted by the try-finally } {
    empty_main T1420switch20 {
        switch (args.length) {
            default:
            try {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-switch-21 { The break is interrupted by the try-finally } {
    empty_main T1420switch21 {
        switch (args.length) {
            default:
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        int i;
    }
} FAIL

tcltest::test 14.20-switch-22 { The break is interrupted by the try-finally } {
    empty_main T1420switch22 {
        switch (args.length) {
            default:
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        ;
    }
} FAIL

tcltest::test 14.20-switch-23 { The break is interrupted by the try-finally } {
    empty_main T1420switch23 {
        switch (args.length) {
            default:
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        {}
    }
} FAIL

tcltest::test 14.20-switch-24 { The break is interrupted by the try-finally } {
    empty_main T1420switch24 {
        switch (args.length) {
            default:
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    }
} FAIL

# while statements

tcltest::test 14.20-while-1 { A while statement can complete normally if:
        The condition expression is not a constant expression with value true } {
    empty_main T1420while1 {
        boolean b = true;
        while (b);
        int j;
    }
} PASS
        

tcltest::test 14.20-while-2 { A while statement can complete normally if:
        There is a reachable break statement that exits the while statement. } {
    empty_main T1420while2 {
        while (true) break;
        int i;
    }
} PASS

tcltest::test 14.20-while-3 { A while statement cannot complete normally iff
        there is no break and the condition is constant true } {
    empty_main T1420while3 {
        while (true);
        int i;
    }
} FAIL

tcltest::test 14.20-while-4 { A while statement cannot complete normally iff
        there is no break and the condition is constant true } {
    empty_main T1420while4 {
        while (true);
        ;
    }
} FAIL

tcltest::test 14.20-while-5 { A while statement cannot complete normally iff
        there is no break and the condition is constant true } {
    empty_main T1420while5 {
        while (true);
        {}
    }
} FAIL

tcltest::test 14.20-while-6 { A while statement cannot complete normally iff
        there is no break and the condition is constant true } {
    empty_main T1420while6 {
        while (true);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-while-7 { The contained statement of a while is unreachable
        iff the condition is constant false } {
    empty_main T1420while7 {
        boolean b = false;
        while (b) b = false;
        int i;
    }
} PASS

tcltest::test 14.20-while-8 { The contained statement of a while is unreachable
        iff the condition is constant false } {
    empty_main T1420while8 {
        int i;
        while (false) i = 1;
    }
} FAIL

tcltest::test 14.20-while-9 { The contained statement of a while is unreachable
        iff the condition is constant false } {
    empty_main T1420while9 {
        while (false);
    }
} FAIL

tcltest::test 14.20-while-10 { The contained statement of a while is unreachable
        iff the condition is constant false } {
    empty_main T1420while10 {
        while (false) {}
    }
} FAIL

tcltest::test 14.20-while-11 { The contained statement of a while is unreachable
        iff the condition is constant false } {
    empty_main T1420while11 {
        while (false) { int x=3; }
    }
} FAIL

tcltest::test 14.20-while-12 { The break is interrupted by the try-finally } {
    empty_main T1420while12 {
        while (true) {
            try {
                break;
            } finally {
                return;
            }
        }
        int i;
    }
} FAIL

tcltest::test 14.20-while-13 { The break is interrupted by the try-finally } {
    empty_main T1420while13 {
        while (true) {
            try {
                break;
            } finally {
                return;
            }
        }
        ;
    }
} FAIL

tcltest::test 14.20-while-14 { The break is interrupted by the try-finally } {
    empty_main T1420while14 {
        while (true) {
            try {
                break;
            } finally {
                return;
            }
        }
        {}
    }
} FAIL

tcltest::test 14.20-while-15 { The break is interrupted by the try-finally } {
    empty_main T1420while15 {
        while (true) {
            try {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-while-16 { The break is interrupted by the try-finally } {
    empty_main T1420while16 {
        while (true) {
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        int i;
    }
} FAIL

tcltest::test 14.20-while-17 { The break is interrupted by the try-finally } {
    empty_main T1420while17 {
        while (true) {
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        ;
    }
} FAIL

tcltest::test 14.20-while-18 { The break is interrupted by the try-finally } {
    empty_main T1420while18 {
        while (true) {
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        {}
    }
} FAIL

tcltest::test 14.20-while-19 { The break is interrupted by the try-finally } {
    empty_main T1420while19 {
        while (true) {
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        }
        { int x=3; }
    }
} FAIL

# do-while statements

tcltest::test 14.20-do-1 { A do statement can complete normally if:
        The contained statement can complete normally and the condition is not constant true } {
    empty_main T1420do1 {
        boolean b = true;
        do {} while (b);
        int i;
    }
} PASS

tcltest::test 14.20-do-2 { A do statement can complete normally if:
        The do statement contains a reachable unlabeled continue statement
        for the do, with condition not constant true } {
    empty_main T1420do2 {
        boolean b = true;
        do continue; while (b);
        int i;
    }
} PASS

tcltest::test 14.20-do-3 { A do statement can complete normally if:
        The do statement contains a reachable labeled continue statement
        for the do, with condition not constant true } {
    empty_main T1420do3 {
        boolean b = true;
        a: do continue a; while (b);
        int i;
    }
} PASS

tcltest::test 14.20-do-4 { A do statement can complete normally if:
        There is a reachable break statement that exits the do statement. } {
    empty_main T1420do4 {
        do break; while (true);
        int i;
    }
} PASS

tcltest::test 14.20-do-5 { A do statement cannot complete normally iff
        all four preceding conditions are not met } {
    empty_main T1420do5 {
        int i;
        do i = 1;
        while (true);
        int j;
    }
} FAIL

tcltest::test 14.20-do-6 { A do statement cannot complete normally iff
        all four preceding conditions are not met } {
    empty_main T1420do6 {
        int i;
        do i = 1;
        while (true);
        ;
    }
} FAIL

tcltest::test 14.20-do-7 { A do statement cannot complete normally iff
        all four preceding conditions are not met } {
    empty_main T1420do7 {
        int i;
        do i = 1;
        while (true);
        {}
    }
} FAIL

tcltest::test 14.20-do-8 { A do statement cannot complete normally iff
        all four preceding conditions are not met } {
    empty_main T1420do8 {
        int i;
        do i = 1;
        while (true);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-9 { The break is interrupted by the try-finally } {
    empty_main T1420do9 {
        do try {
            break;
        } finally {
            return;
        } while (true);
        int i;
    }
} FAIL

tcltest::test 14.20-do-10 { The break is interrupted by the try-finally } {
    empty_main T1420do10 {
        do try {
            break;
        } finally {
            return;
        } while (true);
        ;
    }
} FAIL

tcltest::test 14.20-do-11 { The break is interrupted by the try-finally } {
    empty_main T1420do11 {
        do try {
            break;
        } finally {
            return;
        } while (true);
        {}
    }
} FAIL

tcltest::test 14.20-do-12 { The break is interrupted by the try-finally } {
    empty_main T1420do12 {
        do try {
            break;
        } finally {
            return;
        } while (true);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-13 { The break is interrupted by the try-finally } {
    empty_main T1420do13 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            break;
        } finally {
            return;
        } while (true);
        int i;
    }
} FAIL

tcltest::test 14.20-do-14 { The break is interrupted by the try-finally } {
    empty_main T1420do14 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            break;
        } finally {
            return;
        } while (true);
        ;
    }
} FAIL

tcltest::test 14.20-do-15 { The break is interrupted by the try-finally } {
    empty_main T1420do15 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            break;
        } finally {
            return;
        } while (true);
        {}
    }
} FAIL

tcltest::test 14.20-do-16 { The break is interrupted by the try-finally } {
    empty_main T1420do16 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            break;
        } finally {
            return;
        } while (true);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-17 { The continue is interrupted by the try-finally } {
    empty_main T1420do17 {
        do try {
            continue;
        } finally {
            return;
        } while (false);
        int i;
    }
} FAIL

tcltest::test 14.20-do-18 { The continue is interrupted by the try-finally } {
    empty_main T1420do18 {
        do try {
            continue;
        } finally {
            return;
        } while (false);
        ;
    }
} FAIL

tcltest::test 14.20-do-19 { The continue is interrupted by the try-finally } {
    empty_main T1420do19 {
        do try {
            continue;
        } finally {
            return;
        } while (false);
        {}
    }
} FAIL

tcltest::test 14.20-do-20 { The continue is interrupted by the try-finally } {
    empty_main T1420do20 {
        do try {
            continue;
        } finally {
            return;
        } while (false);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-21 { The continue is interrupted by the try-finally } {
    empty_main T1420do21 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            continue;
        } finally {
            return;
        } while (false);
        int i;
    }
} FAIL

tcltest::test 14.20-do-22 { The continue is interrupted by the try-finally } {
    empty_main T1420do22 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            continue;
        } finally {
            return;
        } while (false);
        ;
    }
} FAIL

tcltest::test 14.20-do-23 { The continue is interrupted by the try-finally } {
    empty_main T1420do23 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            continue;
        } finally {
            return;
        } while (false);
        {}
    }
} FAIL

tcltest::test 14.20-do-24 { The continue is interrupted by the try-finally } {
    empty_main T1420do24 {
        do try {
            throw new Exception();
        } catch (Exception e) {
            continue;
        } finally {
            return;
        } while (false);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-25 { The continue is interrupted by the try-finally } {
    empty_main T1420do25 {
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        int i;
    }
} FAIL

tcltest::test 14.20-do-26 { The continue is interrupted by the try-finally } {
    empty_main T1420do26 {
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        ;
    }
} FAIL

tcltest::test 14.20-do-27 { The continue is interrupted by the try-finally } {
    empty_main T1420do27 {
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        {}
    }
} FAIL

tcltest::test 14.20-do-28 { The continue is interrupted by the try-finally } {
    empty_main T1420do28 {
        a: do try {
            continue a;
        } finally {
            return;
        } while (false);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-29 { The continue is interrupted by the try-finally } {
    empty_main T1420do29 {
        a: do try {
            throw new Exception();
        } catch (Exception e) {
            continue a;
        } finally {
            return;
        } while (false);
        int i;
    }
} FAIL

tcltest::test 14.20-do-30 { The continue is interrupted by the try-finally } {
    empty_main T1420do30 {
        a: do try {
            throw new Exception();
        } catch (Exception e) {
            continue a;
        } finally {
            return;
        } while (false);
        ;
    }
} FAIL

tcltest::test 14.20-do-31 { The continue is interrupted by the try-finally } {
    empty_main T1420do31 {
        a: do try {
            throw new Exception();
        } catch (Exception e) {
            continue a;
        } finally {
            return;
        } while (false);
        {}
    }
} FAIL

tcltest::test 14.20-do-32 { The continue is interrupted by the try-finally } {
    empty_main T1420do32 {
        a: do try {
            throw new Exception();
        } catch (Exception e) {
            continue a;
        } finally {
            return;
        } while (false);
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-do-33 { The continue is interrupted by the try-finally } {
    empty_main T1420do33 {
        a: do {
            try {
                throw new Exception();
            } catch (Exception e) {
                continue a;
            } finally {
                return;
            }
        } while (false);
        { int x=3; }
    }
} FAIL

# for statement
tcltest::test 14.20-for-1 { A for statement can complete normally if:
        The condition statement exists and is non-constant } {
    empty_main T1420for1 {
        boolean b = true;
        for ( ; b; );
        int i;
    }
} PASS

tcltest::test 14.20-for-2 { A for statement can complete normally if:
        There is a reachable break statement that exits the for statement. } {
    empty_main T1420for2 {
        for ( ; ; ) break;
        int i;
    }
} PASS

tcltest::test 14.20-for-3 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for3 {
        for ( ; ; );
        int i;
    }
} FAIL

tcltest::test 14.20-for-4 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for4 {
        for ( ; ; );
        ;
    }
} FAIL

tcltest::test 14.20-for-5 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for5 {
        for ( ; ; );
        {}
    }
} FAIL

tcltest::test 14.20-for-6 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for6 {
        for ( ; ; );
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-for-7 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for7 {
        for ( ; true; );
        int i;
    }
} FAIL

tcltest::test 14.20-for-8 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for8 {
        for ( ; true; );
        ;
    }
} FAIL

tcltest::test 14.20-for-9 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for9 {
        for ( ; true; );
        {}
    }
} FAIL

tcltest::test 14.20-for-10 { A for statement cannot complete normally if there
        is no condition or the condition is constant true, and there is no break } {
    empty_main T1420for10 {
        for ( ; true; );
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-for-11 { A for statement cannot have a constant false condition } {
    empty_main T1420for11 {
        int i;
        for( ; false; ) i = 1;
    }
} FAIL

tcltest::test 14.20-for-12 { A for statement cannot have a constant false condition } {
    empty_main T1420for12 {
        for ( ; false; );
    }
} FAIL

tcltest::test 14.20-for-13 { A for statement cannot have a constant false condition } {
    empty_main T1420for13 {
        for ( ; false; ) {}
    }
} FAIL

tcltest::test 14.20-for-14 { A for statement cannot have a constant false condition } {
    empty_main T1420for14 {
        for ( ; false; ) { int x=3; }
    }
} FAIL

tcltest::test 14.20-for-15 { The break is interrupted by the try-finally } {
    empty_main T1420for15 {
        for ( ; ; )
            try {
                break;
            } finally {
                return;
            }
        int i;
    }
} FAIL

tcltest::test 14.20-for-16 { The break is interrupted by the try-finally } {
    empty_main T1420for16 {
        for ( ; ; )
            try {
                break;
            } finally {
                return;
            }
        ;
    }
} FAIL

tcltest::test 14.20-for-17 { The break is interrupted by the try-finally } {
    empty_main T1420for17 {
        for ( ; ; )
            try {
                break;
            } finally {
                return;
            }
        {}
    }
} FAIL

tcltest::test 14.20-for-18 { The break is interrupted by the try-finally } {
    empty_main T1420for18 {
        for ( ; ; )
            try {
                break;
            } finally {
                return;
            }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-for-19 { The break is interrupted by the try-finally } {
    empty_main T1420for19 {
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        int i;
    }
} FAIL

tcltest::test 14.20-for-20 { The break is interrupted by the try-finally } {
    empty_main T1420for20 {
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        ;
    }
} FAIL

tcltest::test 14.20-for-21 { The break is interrupted by the try-finally } {
    empty_main T1420for21 {
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        {}
    }
} FAIL

tcltest::test 14.20-for-22 { The break is interrupted by the try-finally } {
    empty_main T1420for22 {
        for ( ; ; )
            try {
                throw new Exception();
            } catch (Exception e) {
                break;
            } finally {
                return;
            }
        { int x=3; }
    }
} FAIL

# statements that always complete abruptly
tcltest::test 14.20-abrupt-1 { break cannot complete normally } {
    empty_main T1420abrupt1 {
        while (true) {
            break;
            int i;
        }
    }
} FAIL

tcltest::test 14.20-abrupt-2 { break cannot complete normally } {
    empty_main T1420abrupt2 {
        while (true) {
            break;
            ;
        }
    }
} FAIL

tcltest::test 14.20-abrupt-3 { break cannot complete normally } {
    empty_main T1420abrupt3 {
        while (true) {
            break;
            {}
        }
    }
} FAIL

tcltest::test 14.20-abrupt-4 { break cannot complete normally } {
    empty_main T1420abrupt4 {
        while (true) {
            break;
            { int x=3; }
        }
    }
} FAIL

tcltest::test 14.20-abrupt-5 { continue cannot complete normally } {
    empty_main T1420abrupt5 {
        while (true) {
            continue;
            int i;
        }
    }
} FAIL

tcltest::test 14.20-abrupt-6 { continue cannot complete normally } {
    empty_main T1420abrupt6 {
        while (true) {
            continue;
            ;
        }
    }
} FAIL

tcltest::test 14.20-abrupt-7 { continue cannot complete normally } {
    empty_main T1420abrupt7 {
        while (true) {
            continue;
            {}
        }
    }
} FAIL

tcltest::test 14.20-abrupt-8 { continue cannot complete normally } {
    empty_main T1420abrupt8 {
        while (true) {
            continue;
            { int x=3; }
        }
    }
} FAIL

tcltest::test 14.20-abrupt-9 { return cannot complete normally } {
    empty_main T1420abrupt9 {
        return;
        int i;
    }
} FAIL

tcltest::test 14.20-abrupt-10 { return cannot complete normally } {
    empty_main T1420abrupt10 {
        return;
        ;
    }
} FAIL

tcltest::test 14.20-abrupt-11 { return cannot complete normally } {
    empty_main T1420abrupt11 {
        return;
        {}
    }
} FAIL

tcltest::test 14.20-abrupt-12 { return cannot complete normally } {
    empty_main T1420abrupt12 {
        return;
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-abrupt-13 { throw cannot complete normally } {
    empty_main T1420abrupt13 {
        throw new RuntimeException();
        int i;
    }
} FAIL

tcltest::test 14.20-abrupt-14 { throw cannot complete normally } {
    empty_main T1420abrupt14 {
        throw new RuntimeException();
        ;
    }
} FAIL

tcltest::test 14.20-abrupt-15 { throw cannot complete normally } {
    empty_main T1420abrupt15 {
        throw new RuntimeException();
        {}
    }
} FAIL


tcltest::test 14.20-abrupt-16 { throw cannot complete normally } {
    empty_main T1420abrupt16 {
        throw new RuntimeException();
        { int x=3; }
    }
} FAIL


tcltest::test 14.20-synchronized-1 { synchronized completes normally iff the
        contained block does as well } {
    empty_main T1420synch1 {
        synchronized (args) {}
        int i;
    }
} PASS

tcltest::test 14.20-synchronized-2 { synchronized completes normally iff the
        contained block does as well } {
    empty_main T1420synch2 {
        synchronized (args) {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-synchronized-3 { synchronized completes normally iff the
        contained block does as well } {
    empty_main T1420synch3 {
        synchronized (args) {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-synchronized-4 { synchronized completes normally iff the
        contained block does as well } {
    empty_main T1420synch4 {
        synchronized (args) {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-synchronized-5 { synchronized completes normally iff the
        contained block does as well } {
    empty_main T1420synch5 {
        synchronized (args) {
            return;
        }
        { int x=3; }
    }
} FAIL


# try-catch-finally
tcltest::test 14.20-try-1 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try1 {
        try {
            new Object();
        } catch (RuntimeException e) {
            return;
        }
        int i;
    }
} PASS

tcltest::test 14.20-try-2 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try2 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-3 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try3 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-4 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try4 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-5 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try5 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-6 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try6 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        }
        int i;
    }
} PASS

tcltest::test 14.20-try-7 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try7 {
        try {
            new Object();
        } catch (RuntimeException e) {
            return;
        } finally {
        }
        int i;
    }
} PASS

tcltest::test 14.20-try-8 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try8 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-9 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try9 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-10 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try10 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-11 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try11 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-12 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try12 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
        }
        int i;
    }
} PASS

tcltest::test 14.20-try-13 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try13 {
        try {
            new Object();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-14 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try14 {
        try {
            new Object();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-15 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try15 {
        try {
            new Object();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-16 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try16 {
        try {
            new Object();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-17 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try17 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-18 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try18 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-19 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try19 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-20 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try20 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-21 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try21 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-22 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try22 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-23 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try23 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-24 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try24 {
        try {
            throw new Exception();
        } catch (Exception e) {
            return;
        } catch (Throwable t) {
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-25 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try25 {
        try {
            new Object();
        } finally {
        }
        int i;
    }
} PASS

tcltest::test 14.20-try-26 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try26 {
        try {
            throw new RuntimeException();
        } finally {
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-27 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try27 {
        try {
            throw new RuntimeException();
        } finally {
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-28 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try28 {
        try {
            throw new RuntimeException();
        } finally {
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-29 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try29 {
        try {
            throw new RuntimeException();
        } finally {
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-30 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try30 {
        try {
            new Object();
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-31 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try31 {
        try {
            new Object();
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-32 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try32 {
        try {
            new Object();
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-33 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try33 {
        try {
            new Object();
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

tcltest::test 14.20-try-34 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try34 {
        try {
            throw new Exception();
        } finally {
            return;
        }
        int i;
    }
} FAIL

tcltest::test 14.20-try-35 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try35 {
        try {
            throw new Exception();
        } finally {
            return;
        }
        ;
    }
} FAIL

tcltest::test 14.20-try-36 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try36 {
        try {
            throw new Exception();
        } finally {
            return;
        }
        {}
    }
} FAIL

tcltest::test 14.20-try-37 { A try statement can complete normally iff both:
        The try block can complete normally or any catch block can complete normally.
        The finally block, if present, can complete normally. } {
    empty_main T1420try37 {
        try {
            throw new Exception();
        } finally {
            return;
        }
        { int x=3; }
    }
} FAIL

# catch blocks of try-catch
# Some of these tests are waiting on Sun's answer to bug 4046575:
# -1, -2, -10, -12, -13, -14, -16, -18, -20, -22, -23
tcltest::test 14.20-catch-1 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch1 {
        try { // empty block cannot throw exception
        } catch (Throwable t) {
            // but Throwable is superclass of asynchronous exceptions
        }
    }
} PASS

tcltest::test 14.20-catch-2 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch2 {
        try {
            int i = 0; // throws no exceptions
            i /= i; // can only throw ArithmeticException
        } catch (ClassCastException e) {
            // but ClassCastException is unchecked
        }
    }
} PASS

tcltest::test 14.20-catch-3 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch3 {
        try {
            throw new Exception();
        } catch (Exception e) {
        }
    }
} PASS

tcltest::test 14.20-catch-4 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch4 {
        try {
            throw new Exception();
        } catch (Throwable t) {
        } catch (Exception e) {
        }
    }
} FAIL

tcltest::test 14.20-catch-5 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch5 {
        try {
            throw new Exception();
        } catch (Exception e) {
        } catch (Throwable t) {
            // possible Error creating new Exception
        }
    }
} PASS

tcltest::test 14.20-catch-6 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch6 {
        try {
            new Object();
        } catch (RuntimeException e) {
        } catch (Error err) {
        }
    }
} PASS

tcltest::test 14.20-catch-7 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    ok_pass_or_warn [compile [saveas T1420catch7.java {
class T1420catch7 {
    void foo() {
        try {
            new Object();
        } catch (Exception e) {
        } catch (Throwable t) {
        }
    }
}
    }]]
} OK

tcltest::test 14.20-catch-8 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch8 {
        try {
            throw new Exception();
        } catch (Exception e) {
        } catch (Exception e1) {
        }
    }
} FAIL

tcltest::test 14.20-catch-9 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch9 {
        int i, j=0;
        try {
            i = 1/j;
        } catch (ArithmeticException ae) {
        }
    }
} PASS

tcltest::test 14.20-catch-10 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch10 {
        int i, j=0;
        try {
            i = 1/j;
        } catch (ArithmeticException ae) {
        } catch (RuntimeException re) {
            // the only possible exception, ae, has already been caught,
            // but this is unchecked
        }
    }
} PASS

tcltest::test 14.20-catch-11 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_class T1420catch11 {
        void choke() throws Exception {
            throw new ClassNotFoundException();
        }
        void foo() throws Exception {
            try {
                choke();
            } catch (ClassNotFoundException e) {
                // reachable, as choke() can throw any subclass
            }
        }
    }
} PASS

tcltest::test 14.20-catch-12 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_class T1420catch12 {
        void foo() throws Exception {
            try {
                throw new Exception();
            } catch (ClassNotFoundException e) {
                // reachable, although the class instance creation cannot
                // create a subclass of Exception
            }
        }
    }
} PASS

tcltest::test 14.20-catch-13 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch13 {
        try {
            return; // throws nothing
        } catch (RuntimeException e) {
            // but this is unchecked
        }
    }
} PASS

tcltest::test 14.20-catch-14 { The throw is interrupted by the inner finally } {
    empty_main T1420catch14 {
        Exception e = new Exception(); // create here to make test work
        try {
            try {
                throw e;
            } finally {
                return; // discards Exception e
            }
        } catch (Exception e1) {
            // but this is unchecked
        }
    }
} PASS

tcltest::test 14.20-catch-15 { The throw is interrupted by the inner finally } {
    empty_main T1420catch15 {
        try {
            try {
                throw new ClassNotFoundException();
            } finally {
                return; // discards ClassNotFoundException
            }
        } catch (ClassNotFoundException e) {
            // this is unreachable
        }
    }
} FAIL

tcltest::test 14.20-catch-16 { The throw is interrupted by the inner finally } {
    empty_main T1420catch16 {
        Exception e = new Exception(); // create here to make test work
        try {
            try {
                throw e;
            } catch (Exception e1) {
                throw e1;
            } finally {
                return; // discards Exception e1
            }
        } catch (Exception e2) {
            // but Exception is superclass of asynchronous exceptions
        }
    }
} PASS

tcltest::test 14.20-catch-17 { The throw is interrupted by the inner finally } {
    empty_main T1420catch17 {
        try {
            try {
                throw new ClassNotFoundException();
            } catch (ClassNotFoundException e) {
                throw e;
            } finally {
                return; // discards ClassNotFoundException
            }
        } catch (ClassNotFoundException e1) {
            // this is unreachable
        }
    }
} FAIL

tcltest::test 14.20-catch-18 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch18 {
        try {
            try {
                throw new Exception();
            } catch (Throwable t) {
                // traps all exceptions
            }
        } catch (Exception e) {
            // but this is unchecked
        }
    }
} PASS                    

tcltest::test 14.20-catch-19 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_class T1420catch19 {
        class MyException extends ClassNotFoundException {}
        void foo() throws Exception {
            ClassNotFoundException c = new MyException();
            try {
                throw c;
            } catch (MyException e) {
                // reachable, as variable reference can contain subclass
            }
        }
    }
} PASS

tcltest::test 14.20-catch-20 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch20 {
        class MyException extends ArithmeticException {}
        try {
            int i = 0;
            i /= i; // throws only ArithmeticException, not MyException
        } catch (MyException e) {
            // but this is unchecked
        }
    }
} PASS

tcltest::test 14.20-catch-21 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch21 {
        final Exception e = new ClassNotFoundException();
        try {
            throw e;
        } catch (ClassNotFoundException c) {
            // this one will be called
        } catch (Exception ex) {
            // this one is reachable, but will never be executed,
            // since analysis does not evaluate variable contents
        }
    }
} PASS

tcltest::test 14.20-catch-22 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch22 {
        class MyException extends ClassNotFoundException {}
        try {
            throw new MyException();
        } catch (MyException m) {
        } catch (ClassNotFoundException e) {
            // this one is reachable, but will never be executed,
            // as the only exception in the try block has already been caught
        }
    }
} PASS

tcltest::test 14.20-catch-23 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch23 {
        try {
            new Object();
        } catch (RuntimeException r) {
        } catch (Error er) {
        } catch (Exception e) {
            // this one is reachable, but will never be executed,
            // as the try block can only throw RuntimeException or
            // Error, which have already been caught
        }
    }
} PASS

tcltest::test 14.20-catch-24 { A catch block C is reachable iff both:
        Some reachable expression or throw statement throws assignable exception.
        No earlier catch block covers type C or superclass. } {
    empty_main T1420catch24 {
        try {
            new Object();
        } catch (RuntimeException e) {
        } catch (NullPointerException n) {
            // although this is unchecked, it is a subclass of
            // a prior clause
        }
    }
} FAIL

# if-then-else
tcltest::test 14.20-if-1 { if-then can complete normally iff it is reachable } {
    empty_main T1420if1 {
        if (true)
            return;
        int i;
    }
} PASS

tcltest::test 14.20-if-2 { then-statement of if-then is reachable } {
    empty_main T1420if2 {
        int i;
        if (false)
           i = 1;
    }
} PASS

tcltest::test 14.20-if-3 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if3 {
        if (true)
            return;
        else
            ;
        int i;
    }
} PASS

tcltest::test 14.20-if-4 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if4 {
        if (false)
            ;
        else
            return;
        int i;
    }
} PASS

tcltest::test 14.20-if-5 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if5 {
        if (true)
            return;
        else
            return;
        int i;
    }
} FAIL

tcltest::test 14.20-if-6 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if6 {
        if (true)
            return;
        else
            return;
        ;
    }
} FAIL

tcltest::test 14.20-if-7 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if7 {
        if (true)
            return;
        else
            return;
        {}
    }
} FAIL

tcltest::test 14.20-if-8 { if-then-else can complete normally iff
        then-statement or else-statement can complete normally. } {
    empty_main T1420if8 {
        if (true)
            return;
        else
            return;
        { int x=3; }
    }
} FAIL
