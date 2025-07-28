
/*
 * Copyright (C) 2022 Kevin Zatloukal and James Wilcox.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 331 for use solely during Autumn Quarter 2022 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

import { LatLngExpression } from "leaflet";
import React, { Component } from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import MapLine from "./MapLine";
import { UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER } from "./Constants";

// This defines the location of the map. These are the coordinates of the UW Seattle campus
const position: LatLngExpression = [UW_LATITUDE_CENTER, UW_LONGITUDE_CENTER];

// NOTE: This component is a suggestion for you to use, if you would like to. If
// you don't want to use this component, you're free to delete it or replace it
// with your hw-lines Map

interface MapProps {
  input: Edge[]
}

type Edge = {
    x1: number
    y1: number
    x2: number
    y2: number
    color: string
}
interface MapState {}

//Parses and renders given data
class Map extends Component<MapProps, MapState> {
    constructor(props: MapProps) {
        super(props);
    }

    //Parses and returns an array used to render the line
    parseRender() : JSX.Element[] {
        let retArr: JSX.Element[] = [];
        //If the arr is empty, returns an empty JSX array
        if(this.props.input === null) {
            return retArr
        }
        //Using the MapLine object to create JSX elements
        for(let i = 0; i < this.props.input.length; i++) {
            let element = this.props.input[i];
            retArr.push(<MapLine key={i} x1={element.x1} y1={element.y1} x2={element.x2} y2={element.y2} color={element.color}/>)
        }
        return retArr
    }

    //Renders the path and map
  render() {
    return (
      <div id="map">
        <MapContainer
          center={position}
          zoom={15}
          scrollWheelZoom={false}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {
              this.parseRender()
          }
        </MapContainer>
      </div>
    );
  }
}

export default Map;