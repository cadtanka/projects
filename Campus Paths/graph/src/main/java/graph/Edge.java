package main.java.graph;

/**
 * <b>Edge</b> represents an immutable link between two nodes. Each edge has a start and end node,
 * and can only ever have two connections
 * @Param <V> is the value given to the node
 * @Param <B> is the beginning node of the edge's value
 * @Param <E> is the ending node of the edge's value
 */
public final class Edge <V,B,E>{

    //The string value associated with the edge
    private final V label;

    //The node that the edge begins at
    private final B begin;

    //The node that the edge ends at
    private final E end;

    //Abstraction function:
    //A edge object is an edge that contains a string value within it, as well as as two other
    //string representing its starting and ending nodes

    //Representation Invariant
    //The string value within edge != null && edge != null

    /**
     * Constructs a new Edge
     * @param value is the value associated with the value
     * @param A is the starting node of the Edge
     * @param B is the ending node of the Edge
     * @spec.effects constructs a new Edge with Node A as a beginning, and Node B as an end
     */
    public Edge(V value, B A, E B) {
        this.label = value;
        this.begin = A;
        this.end = B;
        checkRep();
    }

    /**
     * Throws an exception if the representation invariant is violated.
     */
    private void checkRep() {
        assert(this.label != null);
        assert(this.begin != null);
        assert(this.end != null);
    }

    /**
     * Gets the string value within the node
     */
    public V getValue() {
        checkRep();
        return this.label;
    }

    /**
     * Gets the beginning string of the node of the given edge
     * @return the starting node label of the edge
     */
    public B getBegin() {
        checkRep();
        B returnString = this.begin;
        return returnString;
    }

    /**
     * Gets the end node of the given edge
     * @return the end node label of the edge
     */
    public E getEnd() {
        checkRep();
        E returnString = this.end;
        return returnString;
    }

    /**
     * Standard equality operation.
     *
     * @param compare the object to be compared for equality
     * @return true if and only if 'compare' is an instance of an edge and 'this' and 'compare' represent the
     * same edge.
     */
    @Override
    public boolean equals(Object compare) {
        checkRep();
        if(compare instanceof Edge<?,?,?>) {
            Edge<?,?,?> o = (Edge<?,?,?>)compare;
            checkRep();
            return this.label.equals(o.label) && this.getBegin().equals(o.getBegin()) && this.getEnd().equals(o.getEnd());
        } else {
            checkRep();
            return false;
        }
    }

    /**
     * Standard hashCode function.
     *
     * @return an int that all objects equal to this will also return.
     */
    @Override
    public int hashCode() {
        checkRep();
        return this.label.hashCode() + this.getBegin().hashCode() + this.getEnd().hashCode();
    }
}