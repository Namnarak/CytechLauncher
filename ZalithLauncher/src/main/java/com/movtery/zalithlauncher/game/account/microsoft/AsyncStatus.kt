package com.movtery.zalithlauncher.game.account.microsoft

enum class AsyncStatus {
    GETTING_ACCESS_TOKEN,
    GETTING_XBL_TOKEN,
    GETTING_XSTS_TOKEN,
    AUTHENTICATE_MINECRAFT,
    VERIFY_GAME_OWNERSHIP,
    GETTING_PLAYER_PROFILE
}