package com.movtery.zalithlauncher.game.account.microsoft.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XBLRequest(
    @SerialName("Properties")
    val properties: XBLProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: String
)

@Serializable
data class XBLProperties(
    @SerialName("AuthMethod")
    val authMethod: String,
    @SerialName("SiteName")
    val siteName: String,
    @SerialName("RpsTicket")
    val rpsTicket: String
)

@Serializable
data class XSTSRequest(
    @SerialName("Properties")
    val properties: XSTSProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: String
)

@Serializable
data class XSTSProperties(
    @SerialName("SandboxId")
    val sandboxId: String,
    @SerialName("UserTokens")
    val userTokens: List<String>
)

data class XSTSAuthResult(
    val token: String,
    val uhs: String
)