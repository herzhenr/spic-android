package com.henrikherzig.playintegritychecker.ui

import com.henrikherzig.playintegritychecker.attestation.PlayIntegrityStatement
import com.henrikherzig.playintegritychecker.attestation.safetynet.SafetyNetStatement

sealed class ResponseType<out R> {
    class SuccessSafetyNet(val value: SafetyNetStatement) : ResponseType<SafetyNetStatement>()
    class SuccessPlayIntegrity(val value: PlayIntegrityStatement) : ResponseType<PlayIntegrityStatement>()
    //class SuccessGeneric<R>(val value: R) : ResponseType<R>()
    class SuccessSimple(val value: String) : ResponseType<Nothing>()
    class Failure(val error: Throwable) : ResponseType<Nothing>()
    class RateLimiting(val error: Throwable) : ResponseType<Nothing>()
    object Loading : ResponseType<Nothing>()
    object None : ResponseType<Nothing>()
}