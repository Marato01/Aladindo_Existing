package com.reeman.delige.dispatch.util

import com.reeman.delige.request.model.PathPoint
import com.reeman.delige.utils.DestHelper
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class PointUtil {

    companion object {

        /**
         * 计算弧度
         */
        fun calculateRadian(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            // 计算两点之间的直线距离
            val deltaX = x2 - x1
            val deltaY = y2 - y1
            // 使用反正切函数计算弧度

            return atan2(deltaY, deltaX)
        }

        fun areSegmentsOpposite(radian1: Double,radian2: Double):Boolean{
            val angleDifference = abs(Math.toDegrees(radian1)-Math.toDegrees(radian2))
            return angleDifference <= 150.0 || angleDifference >= 210.0
        }


        /**
         * 计算距离
         */
        fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            val deltaX = x2 - x1
            val deltaY = y2 - y1
            return sqrt((deltaX * deltaX + deltaY * deltaY))
        }

        /**
         * 计算最近的路线点
         */
        fun calculateNearestPoint(position: DoubleArray): PathPoint? {
            if (DestHelper.getInstance().pathPoints.isNullOrEmpty()) return null
            val pathPointList = DestHelper.getInstance().pathPoints as List<PathPoint>
            if (pathPointList.isNotEmpty()) {
                return pathPointList
                    .filter {
                        calculateDistance(
                            position[0],
                            position[1],
                            it.xPosition,
                            it.yPosition
                        ) < 1.0
                    }
                    .minByOrNull {
                        calculateDistance(
                            position[0],
                            position[1],
                            it.xPosition,
                            it.yPosition
                        )
                    }
            }
            return null
        }

        /**
         * 判断点C是否在线段AB中间或前方
         */
        fun determinePosition(x1: Double, y1: Double, x2: Double, y2: Double, xC: Double, yC: Double): Boolean {
            val dx = x2 - x1
            val dy = y2 - y1

            val t = ((xC - x1) * dx + (yC - y1) * dy) / (dx * dx + dy * dy)

            val xPerpendicular = x1 + t * dx
            val yPerpendicular = y1 + t * dy

            return when {
                (xPerpendicular == xC && yPerpendicular == yC)|| t < 0 -> true // 点C在线段AB上/点C在线段AB的前方
                else -> false  // 点C在线段AB的后方
            }
        }
    }
}