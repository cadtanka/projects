package main.java.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <b>graph</b> represents an immutable, unsorted collection of nodes connected by edges
 * @Param <N> the value assigned to the node of a graph
 * @Param <E> the value assigned to the edge of a graph
 */
public class Graph <N, E> {

    //Holds all the edges and nodes within the graph
    private final Map<N, HashSet<Edge<E,N,N>>> graph;

    //Toggles the checkRep method (false meaning the checkRep method is disabled)
    private final static boolean ON_OFF = false;

    //Abstraction function:
    //A graph g is a collection of edges and nodes, where the key string represents a node, and each key is the key
    //to the set {e1,e2,e1} where each element is an outgoing edge of the node

    //Representation Invariant for every graph g
    //graph != null && every node and graph is not null. Every node that an edge begins at and ends at must also
    //be included in graph

    /**
     * Constructs a new Graph
     * @spec.effects constructs a new graph object
     */
    public Graph() {
        this.graph = new HashMap<>();
        checkRep();
    }

    /**
     * Throws an exception if the representation invariant is violated.
     */
    private void checkRep() {
        if(ON_OFF) {
            //check the graph isn't null
            assert(this.graph != null);
            for(N target: this.graph.keySet()) {
                //Check no nodes are null
                assert(target != null);

                for(Edge targetE: this.graph.get(target)) {
                    //Check no edges are null
                    assert(targetE != null);
                    //Checks the end of the edge is within the graph
                    assert(this.graph.containsKey(targetE.getEnd()));
                }
            }
        }
    }

    /**
     * Gets the size of the nodeList
     * @return the size of this nodeList
     */
    public int sizeNodes() {
        checkRep();
        return this.graph.size();
    }

    /**
     * Inserts a new node with the associated value
     * If node already exists, node is not added
     * @param value is the name of the associated term that will be inserted
     * @spec.effects inserts a node of value 'value'
     * @spec.modifies this.nodeList
     * @throws IllegalArgumentException if the value is null
     */
    public void insertNode(N value) {
        checkRep();
        if(value == null) {
            throw new IllegalArgumentException("Node of null values are not allowed!");
        }
        if(!this.graph.containsKey(value)) {
            this.graph.put(value, new HashSet<>());
        }
    }

    /**
     * Inserts the edge with the associated value 'value'
     * If edge already exists, edge is not added
     * @param value is the name of the associated term that will be inserted
     * @param A is the value of the node that the edge will start at
     * @param B is the value of the node that the edge will end at
     * @spec.effects inserts an edge of value 'value' beginning at 'A' and ending at 'B'
     * @spec.modifies this.edgeList
     * @throws IllegalArgumentException if either node (A or B) is null, or if the value given is null
     */
    public void insertEdge(E value, N A, N B) {
        if(value.equals(null) || A.equals(null) || B.equals(null)) {
            throw new IllegalArgumentException("Value and both nodes must not be null");
        }
        if(!this.containsNode(A) || !this.containsNode(B)) {
            throw new IllegalArgumentException(("Nodes must not be null"));
        }
        Edge<E,N, N> newEdge = new Edge<>(value, A, B);
        HashSet<Edge<E,N,N>> newSet = this.graph.get(A);
        newSet.add(newEdge);
        checkRep();
    }

    /**
     * Gets the node list of this
     * @return the nodeList of this
     */
    public HashSet<N> getNodeList() {
        return new HashSet<>(this.graph.keySet());
    }

    /**
     * Gets the edge list of this
     * @param node is the string representation to get the edges from
     * @return the edgeList of this
     */
    public Set<Edge<E,N,N>> getEdgeList(N node) {
        Set<Edge<E,N,N>> edgeSet = new HashSet<>(this.graph.get(node));
        return edgeSet;
    }

    public boolean containsNode(N node) {
        if(node.equals(null)) {
            throw new IllegalArgumentException("Node value cannot be null");
        }
        return this.graph.containsKey(node);
    }
}