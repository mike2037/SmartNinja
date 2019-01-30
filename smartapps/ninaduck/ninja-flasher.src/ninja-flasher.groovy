/**
 *  Copyright 2015 SmartThings
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
 *  The Flasher
 *
 *  Author: bob
 *  Date: 2013-02-06
 */
definition(
    name: "Ninja Flasher",
    namespace: "NinaDuck",
    author: "NinjaDuck",
    description: "Flashes a set of lights in response to motion (sets color), an open/close event, or a switch.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck1.jpg",
    iconX2Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck2.jpg",
   	iconX3Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck3.jpg"  
)

preferences {
	section("When any of the following devices trigger..."){
		input "motion", "capability.motionSensor", title: "Motion Sensor?", required: false
		input "contact", "capability.contactSensor", title: "Contact Sensor?", required: false
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Sensor?", required: false
		input "mySwitch", "capability.switch", title: "Switch?", required: false
		input "myPresence", "capability.presenceSensor", title: "Presence Sensor?", required: false
		input "thelock", "capability.lock", title: "Which?", required: true, multiple: false        
	}
	section("Then flash..."){
    	input "switches", "capability.colorControl", title: "Colored Light", required: true, multiple: true
		input "numFlashes", "number", title: "This number of times (default 3)", required: false
	}
	section("Time settings in milliseconds (optional)..."){
		input "onFor", "number", title: "On for (default 1000)", required: false
		input "offFor", "number", title: "Off for (default 1000)", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	subscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe()
}

def subscribe() {
	if (contact) {
		subscribe(contact, "contact.open", contactOpenHandler)
	}
	if (acceleration) {
		subscribe(acceleration, "acceleration.active", accelerationActiveHandler)
	}
	if (motion) {
		subscribe(motion, "motion.active", motionActiveHandler)
	}
	if (mySwitch) {
		subscribe(mySwitch, "switch.on", switchOnHandler)
	}
	if (myPresence) {
		subscribe(myPresence, "presence", presenceHandler)
	}
	if (thelock) {
		subscribe(thelock, "lock.unlocked", lockHandler)
	}    
}

def motionActiveHandler(evt) {
	log.debug "motion $evt.value"
	flashLights()
}

def contactOpenHandler(evt) {
	log.debug "contact $evt.value"
	flashLights()
}

def accelerationActiveHandler(evt) {
	log.debug "acceleration $evt.value"
	flashLights()
}

def switchOnHandler(evt) {
	log.debug "switch $evt.value"
	flashLights()
}

def presenceHandler(evt) {
	log.debug "presence $evt.value"
	if (evt.value == "present") {
		flashLights()
	} else if (evt.value == "not present") {
		flashLights()
	}
}

def lockHandler(evt) {
	log.debug "Valve says ${evt.source}/${evt.name}/${evt.value}"
    flashLights()
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