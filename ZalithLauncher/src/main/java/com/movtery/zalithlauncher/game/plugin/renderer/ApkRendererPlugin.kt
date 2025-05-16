package com.movtery.zalithlauncher.game.plugin.renderer

class ApkRendererPlugin(
    id: String,
    displayName: String,
    summary: String? = null,
    minMCVer: String? = null,
    maxMCVer: String? = null,
    uniqueIdentifier: String,
    glName: String,
    eglName: String,
    path: String,
    env: Map<String, String>,
    dlopen: List<String>,
    val packageName: String
) : RendererPlugin(
    id = id,
    displayName = displayName,
    summary = summary,
    minMCVer = minMCVer,
    maxMCVer = maxMCVer,
    uniqueIdentifier = uniqueIdentifier,
    glName = glName,
    eglName = eglName,
    path = path,
    env = env,
    dlopen = dlopen
)