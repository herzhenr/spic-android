package com.henrikherzig.playintegritychecker.attestation

import android.content.Context
import com.henrikherzig.playintegritychecker.ui.ResponseType

open class RateLimiter(context: Context, preferenceName: String) {

    private val MIN_TIME_BETWEEN_REQUESTS = 3000
    private val MAX_REQUESTS_PER_MINUTE = 5

    // Local variables within the class
    private val context: Context
    private val preferenceName: String

    // Initialization block, executed when an instance of the class is created
    init {
        this.context = context
        this.preferenceName = preferenceName
    }

    // Constructor to set context and preferenceName

    fun shouldRequestBeMade(): ResponseType.Failure? {
        // Rate Limiting: Between two requests there must be at least 3 seconds and not more than 5 requests per minute.
        // get array of last 5 requests from shared preferences (first check, if preference exists and then deserialize into an array)
        val lastRequestTimes = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getStringSet("lastRequestTime", emptySet())
        val lastRequestTimesArray = lastRequestTimes?.toTypedArray() ?: emptyArray()
        // check if last request was less than 3 seconds ago
        val currTime = System.currentTimeMillis()
        if (lastRequestTimesArray.isNotEmpty() && currTime - lastRequestTimesArray.first().toLong() < MIN_TIME_BETWEEN_REQUESTS) {
            // show received error message to the UI
            return ResponseType.Failure(Throwable("Rate Limiting: Between two requests there must be at least 3 seconds"))
        }
        // check if there were more than 5 requests in the last minute
        if (lastRequestTimesArray.isNotEmpty() && lastRequestTimesArray.size >= MAX_REQUESTS_PER_MINUTE && currTime - lastRequestTimesArray.last().toLong() < 10000) {
            // show received error message to the UI
            return ResponseType.Failure(Throwable("Rate Limiting: Not more than 5 requests per minute"))
        }
        // add current time to array and remove first element if array is longer than 5
        val newLastRequestTimesArray = lastRequestTimesArray.toMutableList()
        newLastRequestTimesArray.add(0, currTime.toString())
        if (newLastRequestTimesArray.size > MAX_REQUESTS_PER_MINUTE) newLastRequestTimesArray.removeLast()
        // save new array to shared preferences
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putStringSet("lastRequestTime", newLastRequestTimesArray.toSet())
            .apply()
        return null
    }
}

class RateLimiterSafetyNet(context: Context) : RateLimiter(context, "safetyNetRequests")
class RateLimiterPlayIntegrity(context: Context) : RateLimiter(context, "playIntegrityRequests")