package com.utilkit.lib.events

class Event(var channel: String, var payload: String) {

  override fun toString(): String {
    return "Event(channel='$channel', payload='$payload')"
  }
}
