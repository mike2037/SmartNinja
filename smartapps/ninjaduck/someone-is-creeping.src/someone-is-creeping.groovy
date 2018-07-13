/**
 *  Copyright 2018 SmartThings
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
 *
 *  Author: NinjaDuck
 *  Date: 2018-07-13
 *
 */
definition(
		name: "Someone is Creeping",
		namespace: "NinjaDuck",
		author: "NinjaDuck",
		description: "Receive notifications when something happens and you are not there.",
		category: "Safety & Security",
		iconUrl: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck1.jpg",
		iconX2Url: "https://raw.githubusercontent.com/mike2037/SmartNinja/master/ninjaduck2.jpg",
		pausable: true
)

preferences {
	section("Events to notify me about..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Changes", required: false, multiple: true
		input "Presence", "capability.presenceSensor", title: "When these presence sensor are out", required: true, multiple: true
	}
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Enter a phone number to get SMS", required: false
			paragraph "If outside the US please make sure to enter the proper country code"
			input "pushAndPhone", "enum", title: "Notify me via Push Notification", required: false, options: ["Yes", "No"]
		}
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contact, "contact.closed", eventHandler)
	subscribe(motion, "motion.active", eventHandler)

	//subscribe(arrivalPresence, "presence.present", eventHandler)
	//subscribe(departurePresence, "presence.not present", eventHandler)
}

def eventHandler(evt) {
	//log.debug "Notify got evt ${evt}"
    def nobodyHome = Presence.find{it.currentPresence == "present"} == null
    
    //log.debug nobodyHome
    if (nobodyHome) {
        if (frequency) {
            def lastTime = state[evt.deviceId]
            if (lastTime == null || now() - lastTime >= frequency * 60000) {
                sendMessage(evt)
            }
        }
        else {
            sendMessage(evt)
        }
    }
}

private sendMessage(evt) {
	String msg = messageText
	Map options = [:]

	if (!messageText) {
		msg = defaultText(evt)
		options = [translatable: true, triggerEvent: evt]
	}
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone != 'No') {
				log.debug 'Sending push and SMS'
				options.method = 'both'
			} else {
				log.debug 'Sending SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone != 'No') {
			log.debug 'Sending push'
			options.method = 'push'
		} else {
			log.debug 'Sending nothing'
			options.method = 'none'
		}
		sendNotification(msg, options)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}

private defaultText(evt) {
	if (evt.name == 'presence') {
		if (evt.value == 'present') {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has arrived at the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has arrived at {{ location.name }}'
			}
		} else {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has left the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has left {{ location.name }}'
			}
		}
	} else {
		'{{ triggerEvent.descriptionText }}'
	}
}

private getIncludeArticle() {
	def name = location.name.toLowerCase()
	def segs = name.split(" ")
	!(["work","home"].contains(name) || (segs.size() > 1 && (["the","my","a","an"].contains(segs[0]) || segs[0].endsWith("'s"))))
}