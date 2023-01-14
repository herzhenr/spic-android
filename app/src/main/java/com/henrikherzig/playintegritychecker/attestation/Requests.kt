package com.henrikherzig.playintegritychecker.attestation

import android.content.ContentValues
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 * sends generic api call and checks for errors in the response
 */
fun getApiCall(apiUrl: String, entrypoint: String, query: String = ""): String {
    val client = OkHttpClient()
    val request: Request = Request.Builder()
        .get()
        .url("$apiUrl$entrypoint?$query")
        .build()

    val response: Response = client.newCall(request).execute()

    if (!response.isSuccessful) {
        val `object` = JSONObject(response.body!!.string())
        val messageString = `object`.getString("Error")
        Log.d(ContentValues.TAG, "Error response from API Server. Message:\n'$messageString'")
        throw AttestationException("Error response from API Server. Message:\n'$messageString'")
        // return "Api request error. Code: " + response.code
    }
    val responseBody: ResponseBody? = response.body

    if (responseBody == null) {
        Log.d(ContentValues.TAG, "Error response from API Server (empty response) \n ${response.code}")
        throw AttestationException("Error response from API Server (empty response) \n ${response.code}")
    }

    return responseBody.string()
}