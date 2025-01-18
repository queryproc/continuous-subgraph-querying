package ca.waterloo.dsg.graphflow.util.container;

import java.io.Serializable;

/**
 * A mutable Triple (A a, B b, C c).
 */
public class Quadruple<A, B, C, D> implements Serializable {

    public A a;
    public B b;
    public C c;
    public D d;

    public Quadruple(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
}
