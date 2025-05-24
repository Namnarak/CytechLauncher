package com.movtery.zalithlauncher.utils.logging

import com.movtery.zalithlauncher.utils.logging.Logger.log

fun lError(msg: String, t: Throwable? = null) =
    log(Level.ERROR, msg, t)

fun lWarning(msg: String, t: Throwable? = null) =
    log(Level.WARNING, msg, t)

fun lInfo(msg: String, t: Throwable? = null) =
    log(Level.INFO, msg, t)

fun lDebug(msg: String, t: Throwable? = null) =
    log(Level.DEBUG, msg, t)

fun lTrace(msg: String, t: Throwable? = null) =
    log(Level.TRACE, msg, t)