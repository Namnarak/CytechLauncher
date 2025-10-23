package com.movtery.zalithlauncher.ui.control.gamepad

import androidx.compose.ui.geometry.Offset
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel.Event
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

private const val MOUSE_MAX_ACCELERATION = 2.0

//W
const val MOVEMENT_FORWARD = "key_key.forward"
//A
const val MOVEMENT_LEFT = "key_key.left"
//S
const val MOVEMENT_BACK = "key_key.back"
//D
const val MOVEMENT_RIGHT = "key_key.right"

/**
 * 摇杆横轴、纵轴偏移量状态
 */
class Joystick(
    val type: JoystickType,
    var horizontalValue: Float = 0f,
    var verticalValue: Float = 0f
) {
    /**
     * 摇杆当前方向
     */
    var direction: Direction = Direction.None
        private set

    private var angleRadian: Double? = null
    private var acceleration: Double? = null

    fun onTick(
        inGame: Boolean,
        sendEvent: (Event) -> Unit
    ) {
        val mouseAngle = angleRadian ?: getAngleRadian()
        val acceleration = acceleration ?: calculateAcceleration()
        val sensitivity = if (inGame) 18 else 19 * (AllSettings.cursorSensitivity.state / 100f)

        val deltaX = (cos(mouseAngle) * acceleration * sensitivity.toDouble()).toFloat()
        val deltaY = (sin(mouseAngle) * acceleration * sensitivity.toDouble()).toFloat()

        val offset = Offset(deltaX, -deltaY)
        //偏移量为0的情况下，无论发不发送事件都是无意义的
        if (offset != Offset.Zero) {
            sendEvent(
                Event.StickOffset(type, offset)
            )
        }

        calculateDirection(mouseAngle).takeIf { it != direction }?.let { d ->
            direction = d
            sendEvent(
                Event.StickDirection(type, d)
            )
        }
    }

    fun updateState(
        horizontal: Float = horizontalValue,
        vertical: Float = verticalValue
    ) {
        this.horizontalValue = horizontal
        this.verticalValue = vertical

        angleRadian = getAngleRadian()
        acceleration = calculateAcceleration()
    }

    fun getAngleRadian(): Double {
        return -atan2(verticalValue.toDouble(), horizontalValue.toDouble())
    }

    fun calculateAcceleration(): Double {
        return getMagnitude().pow(MOUSE_MAX_ACCELERATION).coerceAtMost(1.0)
    }

    fun calculateDirection(
        angleRadian: Double = getAngleRadian()
    ): Direction {
        val magnitude = getMagnitude()
        if (magnitude == 0.0) return Direction.None

        val angleDeg = Math.toDegrees(angleRadian).let { if (it < 0) it + 360.0 else it }
        val index = (((angleDeg + 22.5) / 45).toInt() % 8 + 8) % 8

        return Direction.entries.getOrNull(index) ?: Direction.None
    }

    fun getMagnitude(): Double {
        val x = abs(horizontalValue)
        val y = abs(verticalValue)
        return hypot(x.toDouble(), y.toDouble())
    }

    /**
     * 摇杆当前方向
     */
    enum class Direction(val movement: List<String>) {
        East(listOf(MOVEMENT_RIGHT)),
        NorthEast(listOf(MOVEMENT_FORWARD, MOVEMENT_RIGHT)),
        North(listOf(MOVEMENT_FORWARD)),
        NorthWest(listOf(MOVEMENT_FORWARD, MOVEMENT_LEFT)),
        West(listOf(MOVEMENT_LEFT)),
        SouthWest(listOf(MOVEMENT_BACK, MOVEMENT_LEFT)),
        South(listOf(MOVEMENT_BACK)),
        SouthEast(listOf(MOVEMENT_BACK, MOVEMENT_RIGHT)),
        /**
         * 无方向
         */
        None(emptyList())
    }
}