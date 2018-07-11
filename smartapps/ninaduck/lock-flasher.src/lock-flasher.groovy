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
    name: "Lock Flasher",
    namespace: "NinaDuck",
    author: "NinjaDuck",
    description: "Locks the door and flashes lights if open.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Samsung/home.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Samsung/home@2x.png"
)

preferences {
	section("What lock to base it off of...") {
		input "thelock", "capability.lock", title: "Which?", required: true, multiple: false
	}
	section("Sensor to base open state off of...") {
		input "thesensor", "capability.sensor", title: "Which?", required: true, multiple: false
	}
	section("Then flash..."){
		input "switches", "capability.switch", title: "These lights", multiple: true
		input "numFlashes", "number", title: "This number of times (default 3)", required: false
	}
	section("Time settings in milliseconds (optional)..."){
		input "onFor", "number", title: "On for (default 1000)", required: false
		input "offFor", "number", title: "Off for (default 1000)", required: false
	}
    
    }

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
    subscribe(thelock, "lock.locked", valveHandler)
    
    log.debug thelock.currentValue("lock")
    log.debug thesensor.currentValue("contact")
}

def valveHandler(evt) {
	log.debug "Valve says ${evt.source}/${evt.name}/${evt.value}"
    //log.debug thesensor.currentValue("contact")
    if (evt.value == "locked" && thesensor.currentValue("contact") == "open") {
    	log.debug "No love"
    	flashLights()
	}
    
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}
