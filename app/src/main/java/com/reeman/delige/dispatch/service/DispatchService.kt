package com.reeman.delige.dispatch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.reeman.delige.base.BaseApplication.*
import com.reeman.delige.constants.State
import com.reeman.delige.dispatch.DispatchManager
import com.reeman.delige.dispatch.DispatchState
import com.reeman.delige.dispatch.model.RobotInfo
import com.reeman.delige.dispatch.util.DispatchUtil
import com.reeman.delige.dispatch.util.PointUtil
import com.reeman.delige.event.Event
import com.reeman.delige.navigation.Mode
import com.reeman.delige.request.model.PathPoint
import com.reeman.delige.utils.DestHelper
import com.reeman.ros.util.Log
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DispatchService : Service() {

    private var espPublishCount = 0

    private var lastRobotInfo: RobotInfo? = null

    private val gson: Gson = Gson()


    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        val notificationId = 1001
        startForeground(notificationId, notification)
        val newScheduledThreadPool = Executors.newScheduledThreadPool(2)
        newScheduledThreadPool.scheduleWithFixedDelay(
            {
                try {
                    if (
                        !DispatchManager.isStarted()
                        || navigationMode != Mode.FIX_ROUTE
                        || mRobotInfo.hostname.isEmpty()
                    ) return@scheduleWithFixedDelay
                    val take = pointInfoQueue.take(3).map { it.name }
                    mRobotInfo.pointQueue.clear()
                    mRobotInfo.pointQueue.addAll(take)
                    mRobotInfo.state = ros.state
                    mRobotInfo.dispatchState = dispatchState
                    if (++espPublishCount >= 10 || mRobotInfo != lastRobotInfo) {
                        espPublishCount = 0
                        if (ros.state == State.PAUSE || ros.state == State.IDLE || ros.state == State.CHARGING || dispatchState != DispatchState.INIT) {
                            mRobotInfo.lineSpeed = 0.0
                        } else {
                            if (take.size >= 2) {
                                val pathPoints = DestHelper.getInstance().points as List<PathPoint>
                                val firstPoint =
                                    pathPoints.firstOrNull { pathPoint -> pathPoint.name == take[0] }
                                val secondPoint =
                                    pathPoints.firstOrNull { pathPoint -> pathPoint.name == take[1] }
                                if (firstPoint != null && secondPoint != null) {
                                    mRobotInfo.lineSpeed = PointUtil.calculateRadian(
                                        firstPoint.xPosition,
                                        firstPoint.yPosition,
                                        secondPoint.xPosition,
                                        secondPoint.yPosition
                                    )
                                } else {
                                    mRobotInfo.lineSpeed = 0.0
                                }
                            } else {
                                mRobotInfo.lineSpeed = 0.0
                            }
                        }
                        lastRobotInfo = mRobotInfo.copy()
                        DispatchManager.publishMessage(mRobotInfo)
                        Log.w("本机推送 : ${gson.toJson(mRobotInfo)}")
                    }
                } catch (e: Exception) {
                }
            },
            100, 100, TimeUnit.MILLISECONDS
        )
        newScheduledThreadPool
            .scheduleWithFixedDelay({
                try {
                    if (navigationMode != Mode.FIX_ROUTE
                        || dispatchState == DispatchState.IGNORE
                        || ros.state == State.IDLE
                        || ros.state == State.CHARGING
                        || ros.state == State.PAUSE
                    ) return@scheduleWithFixedDelay
                    val robotList = DispatchUtil.getRobotList()
                    Log.w("机器信息 : ${gson.toJson(robotList)}")
                    if (robotList.isEmpty()) {
                        if (dispatchState == DispatchState.WAITING) {
                            Log.w("机器全部离线,本机暂停中,恢复任务")
                            EventBus.getDefault().post(Event.getOnDispatchResumeEvent())
                        }
                        return@scheduleWithFixedDelay
                    }
                    if (dispatchState == DispatchState.INIT
                        && ros.state != State.IDLE
                        && ros.state != State.PAUSE
                        && ros.state != State.CHARGING
                    ) {
                        if (DispatchUtil.isFirstEnterSpecialArea(robotList)) {
                            val pathList = pointInfoQueue.take(3).map { it.name }
                            var otherOnFront = false
                            robotList.forEach { robotInfo ->
                                if (robotInfo.dispatchState != DispatchState.IGNORE) {
                                    val robotPathPoints = robotInfo.pointQueue.take(3)
                                    val intersect = pathList.intersect(robotPathPoints.toSet())
                                    if (intersect.isNotEmpty()
                                        && pathList[0] !in intersect
                                    ) {
                                        if (robotPathPoints[0] in intersect) {
                                            otherOnFront = true
                                            Log.w("机器${robotInfo.hostname}在本机前方")
                                        }
                                    }
                                }
                            }
                            if (otherOnFront) {
                                EventBus.getDefault().post(Event.getOnDispatchPauseEvent())
                            }
                            return@scheduleWithFixedDelay
                        }
                        if (DispatchUtil.checkPause(
                                pointInfoQueue.take(3).map { it.name },
                                robotList,
                                mRobotInfo.enterSpecialAreaSequence != 0
                            )
                        ) {
                            EventBus.getDefault().post(Event.getOnDispatchPauseEvent())
                        }

                        return@scheduleWithFixedDelay
                    }
                    if (dispatchState == DispatchState.WAITING) {
                        val currentPathPoints = pointInfoQueue.take(4).map { it.name }
                        if (mRobotInfo.enterSpecialAreaSequence != 0 && !DispatchUtil.isFirstEnterSpecialArea(
                                robotList
                            )
                        ) {
                            return@scheduleWithFixedDelay
                        }
                        if (DispatchUtil.checkRoute(
                                currentPathPoints,
                                robotList
                            )
                        ) return@scheduleWithFixedDelay
                        if (
                            DispatchUtil.isCloseToTargetPoint()
                            && DispatchUtil.isTargetPointOccupied()) return@scheduleWithFixedDelay
                        Log.w("机器与其他机器无路线交叉,恢复任务")
                        EventBus.getDefault().post(Event.getOnDispatchResumeEvent())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("调度服务报错 : ${e.message}")
                }
            }, 0L, 500L, TimeUnit.MILLISECONDS)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "your_channel_id"
            val channelName = "Your Channel Name"
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, "your_channel_id")
            .setContentTitle("Dispatch")
            .setContentText("Dispatch service is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return builder.build()
    }
}