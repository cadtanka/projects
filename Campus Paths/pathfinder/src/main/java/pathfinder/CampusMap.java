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

package main.java.pathfinder;

import graph.Edge;
import graph.Graph;
import pathfinder.datastructures.Path;
import pathfinder.datastructures.Point;
import pathfinder.parser.CampusBuilding;
import pathfinder.parser.CampusPath;
import pathfinder.parser.CampusPathsParser;
import pathfinder.textInterface.Pathfinder;

import java.util.*;

/**
 * CampusMap is a collection of all buildings at UW, connected by paths
 */
public class CampusMap implements ModelAPI {

    //Maps the abbreviated name to the long name of a building
    private Map<String,String> shortToLong;

    //Maps the abbreviated name to it's coordinates
    private Map<String, Point> shortToCoords;

    //A graph representing the University of Washington's campus
    private Graph<Point, Double> campusGraph;

    //Toggles the checkRep method on and off
    private boolean onOFF = false;

    //Abstraction function:
    //A campusMap M is a collection of nodes connected by edges. Each node is a destination of an edge, and
    //each edge's cost is the distance traveled. Buildings are stored as nodes

    //Rep invariant
    //  shortToLong != null &&
    //  shortToCoords != null &&
    //  campusGraph != null &&
    //  shortToLong does not contain null elements &&
    //  shortToCoords does not contain null elements &&
    //  campusGraph does not contain null nodes or edges

    /**
     * Constructs a new CampusMap
     */
    public CampusMap() {
        shortToLong = new HashMap<>();
        shortToCoords = new HashMap<>();
        campusGraph = new Graph<>();
        checkRep();
        makeGraph();
    }

    /**
     * Makes a new graph using the data of the campus_buildings and campus_paths files
     */
    public void makeGraph() {
        List<CampusBuilding> campusBuildings = new ArrayList<>(CampusPathsParser.parseCampusBuildings("campus_buildings.csv"));
        List<CampusPath> campusPaths = new ArrayList<>(CampusPathsParser.parseCampusPaths("campus_paths.csv"));
        for(CampusBuilding target: campusBuildings) {
            this.shortToLong.put(target.getShortName(), target.getLongName());
            Point begin = new Point(target.getX(), target.getY());
            this.shortToCoords.put(target.getShortName(), begin);
        }
        checkRep();
        for(CampusPath targetPath: campusPaths) {
            Point begin = new Point(targetPath.getX1(), targetPath.getY1());
            Point end = new Point(targetPath.getX2(), targetPath.getY2());
            this.campusGraph.insertNode(begin);
            this.campusGraph.insertNode(end);
            this.campusGraph.insertEdge(targetPath.getDistance(), begin, end);
        }
        checkRep();
    }

    @Override
    public boolean shortNameExists(String shortName) {
        checkRep();
        return shortToLong.containsKey(shortName);
    }

    @Override
    public String longNameForShort(String shortName) {
        checkRep();
        if(!shortToLong.containsKey(shortName)) {
            throw new IllegalArgumentException();
        }
        return shortToLong.get(shortName);
    }

    @Override
    public Map<String, String> buildingNames() {
        checkRep();
        return new HashMap<String,String>(shortToLong);
    }

    @Override
    public Path<Point> findShortestPath(String startShortName, String endShortName) {
        checkRep();
        Point start = shortToCoords.get(startShortName);
        Point end = shortToCoords.get(endShortName);
        if(startShortName.equals(null) || endShortName.equals(null) || !(campusGraph.containsNode(start))||
                !(campusGraph.containsNode(end))) {
            throw new IllegalArgumentException();
        }
        checkRep();
        return Pathfinder.leastCostPath(campusGraph,start,end);
    }

    /**
     * Ensures that the representation invariant has not been violated. Returns normally if
     * there is no violation.
     */
    private void checkRep() {
        if(onOFF) {
            assert shortToLong != null;
            assert shortToCoords != null;
            assert campusGraph != null;
            for (String node : shortToLong.keySet()) {
                assert node != null;
            }
            for (String node : shortToCoords.keySet()) {
                assert node != null;
            }
            for (Point node : campusGraph.getNodeList()) {
                assert node != null;
                for(Edge<Double, Point, Point> e: campusGraph.getEdgeList(node)) {
                    assert e != null;
                }
            }
        }
    }

}