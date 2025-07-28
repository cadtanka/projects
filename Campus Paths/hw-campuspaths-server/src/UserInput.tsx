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

import React, {Component} from 'react';

interface EdgeListProps {
    onChange(edges: Edge[]): void;
}

type Edge = {
    x1: number
    y1: number
    x2: number
    y2: number
    color: string
}

interface EdgeListState {
    start:string
    end:string
    buildingList: string[]
}

/**
 * Two dropdowns that allow the user to select a start and end building
 * Also contains the buttons that the user will use to interact with the app.
 */
class UserInput extends Component<EdgeListProps,EdgeListState> {

    constructor(props: EdgeListProps) {
        super(props);
        this.state = {
            start: "",
            end: "",
            buildingList: []
        }
        this.listBuilding();
    }

    //Resets the path
    clickClear() {
        this.setState({
            start: '',
            end:''
        })
        this.props.onChange([]);
    }

    //Checks to make sure props are valid
    validateEdge(): boolean {
        return(this.state.start !== null || this.state.end !== null ||
            this.state.start !== "Choose start building" || this.state.end !== "Choose end building")
    }

    //Makes a request and stores the information as a string in onChange (to later pass through to app)
    makeRequest = async(startBuild: string, endBuild: string) => {
        //Checks to make sure both start and end buildings aren't the same
        if(startBuild === endBuild) {
            alert("Please enter two different buildings for a path to appear on the map!")
            return;
        }

        //Checks to make sure a building is selected for both options
        if(startBuild === "" || endBuild === "") {
            alert("Please choose an option for both a start and end building!")
            return;
        }
        try {
            let response = await fetch
            ("http://localhost:4567/shortest-path?Start-Building="+startBuild+"&End-Building="+endBuild);
            if (!response.ok) {
                alert("The status is wrong! Expected 200: Was " + response.status);
                return;
            }
            let textPromise = response.text();
            let text = await textPromise;
            let arr = this.pathParser(text)
            this.props.onChange(arr);
        } catch(e) {
            alert("There was an error contacting the server.");
            console.log(e);
        }
    }

    //Parses spark input into an array of edges
    pathParser(input:string): Edge[] {
        let target = input;
        let arr: Edge[] = [];
        let pathArr: string[];
        let counter = 0;
        //Gets rid of edge brackets
        target = target.replace("]",'');
        target = target.replace("[", '');

        //Splits the input text into an array with order: x1, y1, x2, y2
        pathArr = target.split(",");

        //Iterates through the data points, and makes a new edge every 4 values read
        for(let i = counter; i < pathArr.length-4; i+=4) {
            let piece: Edge = {
                x1: parseFloat(pathArr[i]),
                y1: parseFloat(pathArr[i+1]),
                x2: parseFloat(pathArr[i+2]),
                y2: parseFloat(pathArr[i+3]),
                color: 'red'
            }
            arr.push(piece);
        }
        return arr;
    }

    //Draws the line on click
    clickDraw() {
        if(this.validateEdge()) {
            let splitStart = this.state.start.split("=");
            let splitEnd = this.state.end.split("=");
            this.makeRequest(splitStart[0], splitEnd[0]);
            console.log("Draw passed")
        } else {
            alert("Select two buildings!")
            console.log("Draw failed")
        }
    }

    //Makes a request and stores the list of buildings as an array
    listBuilding = async() => {
        try {
            let response = await fetch("http://localhost:4567/list-buildings");
            if(!response.ok) {
                alert("The status is wrong! Expected 200: Was " + response.status);
                return;
            }
            let textPromise = response.text();
            let text = await textPromise;
            //Gets rid of edge brackets
            text = text.replace('{','');
            text = text.replace('}','');

            let buildings: string[] = [];
            let elem = text.split(", ");
            elem.sort();

            //Pushes each building into an array
            for(let i = 0; i < elem.length; i++) {
                buildings.push(elem[i])
            }
            this.setState({
                buildingList: buildings
            });
        } catch(e) {
            alert("There was an error contacting the server.");
            console.log(e);
        }
    }

    //The dropdown method to select buildings
    //Label is the label of the dropdown (for user convenience)
    //Input is the starting state of the dropdown
    //The function takes in the data depending on the option chosen
    dropDown = (label: string, input: string, change: Function): JSX.Element => {
        return(
            <div>
                <label>
                    <select onChange={(variable) => change(variable.target.value)} value = {input}>
                        <option value = "" key ="">{label}</option>
                        {this.state.buildingList.map((building) => (
                            <option value = {building} key = {building}>{building}</option>
                        ))}
                    </select>
                </label>
            </div>
        )
    }
    //Sets the start state to the input
    firstChoice = (input: string) => {
        this.setState({
            start: input
        })
    }

    //Sets the end state to the input
    secondChoice = (input: string) => {
        this.setState({
            end: input
        })
    }

    //Renders the buttons and dropdowns
    render() {
        return (
            <div id="Path-Choices">
                <center>
                    Building Choices <br/>
                </center>
                <div>
                    <center>
                        {this.dropDown("Choose start building", this.state.start, this.firstChoice)}
                        {this.dropDown("Choose end building", this.state.end, this.secondChoice)}
                    </center>
                </div>
                <br/>
                    <center>
                        <button onClick={() => {this.clickDraw()}}>Enter</button>
                        <button onClick={() => {this.clickClear()}}>Clear</button>
                    </center>
            </div>
        );
    }
}
export default UserInput;