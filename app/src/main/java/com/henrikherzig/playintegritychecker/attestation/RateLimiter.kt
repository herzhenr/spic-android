package com.henrikherzig.playintegritychecker.attestation

import android.content.Context
import com.henrikherzig.playintegritychecker.ui.ResponseType

open class RateLimiter(context: Context, preferenceName: String, timeBetweenRequests: Int = 5000, maxRequestsPerMinute: Int = 3) {

    private val context: Context
    private val preferenceName: String
    private val timeBetweenRequests: Int
    private val maxRequestsPerMinute: Int

    // Initialization block, executed when an instance of the class is created
    init {
        this.context = context
        this.preferenceName = preferenceName
        this.timeBetweenRequests = timeBetweenRequests
        this.maxRequestsPerMinute = maxRequestsPerMinute
    }

    // Constructor to set context and preferenceName

    fun shouldRequestBeMade(): ResponseType.RateLimiting? {
        // Rate Limiting: Between two requests there must be at least 3 seconds and not more than 5 requests per minute.
        // get array of last 5 requests from shared preferences (first check, if preference exists and then deserialize into an array)
        val lastRequestTimes = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getStringSet(preferenceName, emptySet()) ?: emptySet()

        // convert set of string to array of longs sorted so the first element is the smallest time
        val lastRequestTimesArray = try {
            lastRequestTimes.toTypedArray().map { it.toLong() }.sorted()
        } catch (e: NumberFormatException) {
            emptySet()
        }
        val currTime = System.currentTimeMillis()
        // print(currTime)
        // if there were requests before, apply rate limiting
        if (lastRequestTimesArray.isNotEmpty()) {
            // check if there were more than MAX_REQUESTS_PER_MINUTE requests in the last minute
            if (lastRequestTimesArray.size >= maxRequestsPerMinute && (currTime - lastRequestTimesArray.first()) < 60000) {
                return ResponseType.RateLimiting(Throwable("Rate Limiting: Please wait another ${(60000 + 1000 - (currTime - lastRequestTimesArray.first())) / 1000} seconds for your next request"))
            }
            // check if last request was less than 3 seconds ago
            if (currTime - lastRequestTimesArray.last() < timeBetweenRequests) {
                return ResponseType.RateLimiting(Throwable("Rate Limiting: Please wait another ${(timeBetweenRequests + 1000 - (currTime - lastRequestTimesArray.last())) / 1000} seconds for your next request"))
            }
        }
        // add current time to array and remove first element if array is longer than 5
        val newLastRequestTimesArray = lastRequestTimesArray.toMutableList()
        newLastRequestTimesArray.add(maxRequestsPerMinute, currTime)
        while (newLastRequestTimesArray.size > maxRequestsPerMinute) newLastRequestTimesArray.removeFirst()
        // save new array to shared preferences
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(preferenceName, newLastRequestTimesArray.map { it.toString() }.toSet())
            .apply()
        return null
    }
}

class RateLimiterSafetyNet(context: Context) : RateLimiter(context, "safetyNetRequests")
class RateLimiterPlayIntegrity(context: Context) : RateLimiter(context, "playIntegrityRequests")