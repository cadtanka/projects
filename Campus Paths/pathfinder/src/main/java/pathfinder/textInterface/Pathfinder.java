/*
 * Copyright (C) 2023 Hal Perkins.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 331 for use solely during Winter Quarter 2023 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

package pathfinder.textInterface;

import graph.Edge;
import graph.Graph;
import pathfinder.CampusMap;
import pathfinder.datastructures.Path;

import java.util.*;

/**
 * Pathfinder represents a complete application capable of responding to user prompts to provide
 * a variety of information about campus buildings and paths between them.
 */
public class Pathfinder {

    // This class does not represent an ADT.

    /**
     * The main entry point for this application. Initializes and launches the application.
     *
     * @param args The command-line arguments provided to the system.
     */
    public static void main(String[] args) {
        CampusMap map = new CampusMap();
        TextInterfaceView view = new TextInterfaceView();
        TextInterfaceController controller = new TextInterfaceController(map, view);
        //
        view.setInputHandler(controller);
        controller.launchApplication();
    }

    /**
     * Finds the path of the least cost from a given start node to a given end node
     * @param map the map used to find the shortest paths
     * @param start the starting node of the path
     * @param end the ending node of the path
     * @return a List that contains the edges of the least costly path or null if there is no path found
     * @param <N> The value assigned to the node
     * @throws IllegalArgumentException is map, start, or end is null
     */
    public static <N> Path<N> leastCostPath(Graph<N, Double> map, N start, N end) {
        if (map == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start or end cannot be null");
        }
        if (!map.containsNode(start) || !map.containsNode(end)) {
            throw new IllegalArgumentException("start and end must both exist in map");
        }
        Queue<Path<N>> active = new PriorityQueue<>(new Comparator<Path<N>>() {
            @Override
            public int compare(Path<N> o1, Path<N> o2) {
                double diff = o1.getCost() - o2.getCost();
                if(diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                }
                return 0;
            }
        });
        Set<N> finished = new HashSet<>();
        Path<N> beginPath = new Path<N>(start);
        active.add(beginPath);

        while (!active.isEmpty()) {
            Path<N> minPath = active.remove();
            N minDest = minPath.getEnd();

            if (minDest.equals(end)) {
                return minPath;
            }

            if (finished.contains(minDest)) {
                continue;
            }
            for (Edge<Double, N, N> e : map.getEdgeList(minDest)) {
                if (!finished.contains(e.getEnd())) {
                    Path<N> newPath = minPath.extend(e.getEnd(), e.getValue());
                    active.add(newPath);
                }
            }
            finished.add(minDest);
        }
        //There is no path
        return null;
    }
}