# Random crashers, know to be a problem in jikes 1.11.
# or perhaps jikes 1.12 before it was released.

tcltest::test non-jls-random-crashers-1 { crash } {
    saveas I.java {
public interface I {
    int hashCode();
}
}

    saveas I2.java {
interface I2 extends I { }
}

    compile I.java I2.java
} PASS

