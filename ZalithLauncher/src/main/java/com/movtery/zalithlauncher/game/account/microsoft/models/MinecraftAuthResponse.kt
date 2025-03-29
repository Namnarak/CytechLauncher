package com.movtery.zalithlauncher.game.account.microsoft.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class MinecraftAuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String = "Bearer",
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("username")
    val username: String? = null,
    @SerialName("roles")
    private val _rawRoles: JsonElement? = null,
    @SerialName("entitlements")
    val entitlements: List<Entitlement> = emptyList(),
    @SerialName("metadata")
    val rawMetadata: JsonObject? = null
)

@Serializable
data class Entitlement(
    @SerialName("name")
    val name: String,
    @SerialName("signature")
    val signature: String
)