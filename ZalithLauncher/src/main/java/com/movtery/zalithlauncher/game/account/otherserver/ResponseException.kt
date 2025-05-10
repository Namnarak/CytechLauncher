package com.movtery.zalithlauncher.game.account.otherserver

class ResponseException(val responseMessage: String) : RuntimeException(
    responseMessage
)
