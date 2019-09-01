package org.effervescence.app19.ca.models

class EventDetails(var createdAt: Int, var note: String, var points: Int, var task: String) {
    constructor() : this(0, "", 0, "")
}

