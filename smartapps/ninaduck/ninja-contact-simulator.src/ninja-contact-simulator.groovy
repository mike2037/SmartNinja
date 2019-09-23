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
    name: "Ninja Contact Simulator",
    namespace: "NinaDuck",
    author: "NinjaDuck",
    description: "Causes a contact open to trigger on a virtual device momentarily.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck1.jpg",
    iconX2Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck2.jpg",
   	iconX3Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck3.jpg"  
)

preferences {
	section("When the folowing switch is turned on, simulate motion..."){
		input "mySwitch", "capability.switch", title: "Which Switch?", required: true
	}
	section("Then simulate opening on..."){
        input "contact", "capability.contactSensor", title: "Contact Sensor?", required: true
	}
	section("Time settings in milliseconds (optional)..."){
		input "onFor", "number", title: "On for (default 1000)", required: true
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
	subscribe(mySwitch, "switch.on", switchOnHandler)
    subscribe(contact, "contact.open", contactOpenHandler)

}
 
def switchOnHandler(evt) {
	log.debug "switch $evt.value"
    log.debug mySwitch.name
    log.debug mySwitch.id
    log.debug "switch state is..."
    log.debug mySwitch.currentSwitch 
}

def contactOpenHandler(evt) {
	log.debug "contact $evt.value"

}



private Doit() {
    log.debug "switch state is..."
    log.debug mySwitch.currentSwitch
	If (mySwitch.currentSwitch == "on")
    	{
			contact.open =true
            log.debug contact.currentContact
            mySwitch.off([delay:1000])
           
		}
       Else
       {
       		contact.open =false
             log.debug contact.currentContact
       }
    

}