package com.movtery.zalithlauncher.game.account.otherserver.models

import com.google.gson.annotations.SerializedName

class Servers(
    @SerializedName("server")
    var server: MutableList<Server>
) {
    fun copy(): Servers {
        return Servers(server)
    }
    class Server(
        @SerializedName("baseUrl")
        var baseUrl: String,
        @SerializedName("serverName")
        var serverName: String,
        @SerializedName("register")
        var register: String
    )
}