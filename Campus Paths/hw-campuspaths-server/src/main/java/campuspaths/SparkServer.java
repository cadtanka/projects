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

package campuspaths;

import campuspaths.utils.CORSFilter;
import pathfinder.CampusMap;
import pathfinder.datastructures.Path;
import pathfinder.datastructures.Point;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import java.util.ArrayList;
import java.util.Map;

public class SparkServer {

    public static void main(String[] args) {
        CORSFilter corsFilter = new CORSFilter();
        corsFilter.apply();
        // The above two lines help set up some settings that allow the
        // React application to make requests to the Spark server, even though it
        // comes from a different server.
        // You should leave these two lines at the very beginning of main().

        CampusMap mainMap = new CampusMap();

        //Returns a list of all the building names, with both their short and long names
        Spark.get("/list-buildings", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                Map<String, String> buildingMap = mainMap.buildingNames();
                return buildingMap.toString();
            }
        });

        //Returns the shortest path between the two given start and end buildings
        Spark.get("/shortest-path", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String start = request.queryParams("Start-Building");
                String end = request.queryParams("End-Building");
                if(start == null || end == null) {
                    Spark.halt(400, "Start and end points cannot be null");
                }
                try{
                    Path<Point> shortestPath = mainMap.findShortestPath(start, end);
                    ArrayList<Double> ret = new ArrayList<>();

                    //Breaks apart the path into only x and y coordinates (x1,y1,x2,y2)
                    for(Path<Point>.Segment part: shortestPath) {
                        ret.add(part.getStart().getX());
                        ret.add(part.getStart().getY());
                        ret.add(part.getEnd().getX());
                        ret.add(part.getEnd().getY());
                    }
                    return ret.toString();
                } catch(IllegalArgumentException e) {
                    Spark.halt(400, "Start and end points must exist in the CampusMap");
                }
                //Should never reach this
                return null;
            }
        });
    }

}
