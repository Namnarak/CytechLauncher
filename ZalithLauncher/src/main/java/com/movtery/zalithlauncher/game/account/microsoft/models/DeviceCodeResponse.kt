package com.movtery.zalithlauncher.game.account.microsoft.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceCodeResponse(
    @SerialName("user_code")
    val userCode: String,
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("verification_uri")
    val verificationUrl: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("interval")
    val interval: Int,
    @SerialName("message")
    val message: String
)