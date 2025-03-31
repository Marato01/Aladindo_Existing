package com.reeman.delige.dispatch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.reeman.delige.constants.State
import com.reeman.delige.dispatch.DispatchState
import java.util.concurrent.ConcurrentLinkedQueue

@Keep
data class RobotInfo(
    @SerializedName("h")
    var hostname:String = "",
    @Volatile
    @SerializedName("cp")
    var currentPosition: DoubleArray =DoubleArray(3),
    @SerializedName("ls")
    var lineSpeed :Double = 0.0,
    @Volatile
    @SerializedName("csa")
    var currentSpecialArea:String  = "",
    @SerializedName("ut")
    var updateTime:Long = 0,
    @SerializedName("esas")
    var enterSpecialAreaSequence:Int = 0,
    @SerializedName("s")
    var state:State = State.IDLE,
    @SerializedName("pq")
    val pointQueue: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue<String>(),
    @SerializedName("ds")
    var dispatchState: DispatchState = DispatchState.INIT
) : java.io.Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RobotInfo) return false
        if (hostname != other.hostname) return false
        if (currentSpecialArea != other.currentSpecialArea) return false
        if (state != other.state) return false
        if (pointQueue != other.pointQueue) return false
        if (dispatchState != other.dispatchState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hostname.hashCode()
        result = 31 * result + currentSpecialArea.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + pointQueue.hashCode()
        result = 31 * result + dispatchState.hashCode()
        return result
    }
}