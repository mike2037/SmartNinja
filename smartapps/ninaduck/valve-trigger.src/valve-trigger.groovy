/**
 *  Set other valves based on a single valve
 *
 *  Copyright 2018 SmartThings
 *
 *	Author: Mike Matloub
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Valve Trigger",
    namespace: "NinaDuck",
    author: "NinjaDuck",
    description: "Opend or closes other valves based on the state of one.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck1.jpg",
    iconX2Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck2.jpg"
)

preferences {
	section("What valve triggers the action...") {
		input "theswitch", "capability.switch", title: "Which?", required: true, multiple: false
	}
	section("Valves to make the same state...") {
		input "valves1", "capability.valve", title: "Which?", required: false, multiple: true
	}
	section("Valves to make the opposite state...") {
		input "valves2", "capability.valve", title: "Which?", required: false, multiple: true
	}}

def installed() {
	log.debug "SET HANDLER"
 	initialize()
}

def updated() {
	log.debug "RESET HANDLER"
	unsubscribe()
 	initialize()
}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
    subscribe(theswitch, "switch", valveHandler)
}

def valveHandler(evt) {
	log.debug "Valve says ${evt.source}/${evt.name}/${evt.value}"
    
    if (evt.value == "on"){
    	log.debug "ON!!!"
        valves1?.open()
        valves2?.close()
        } 
    else {
        valves1?.close()
        valves2?.open()  
    }
    
}

