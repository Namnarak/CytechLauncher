package com.movtery.zalithlauncher.ui.screens

import androidx.navigation.NavController

fun NavController.navigateOnce(screenTag: String) {
    if (screenTag == currentDestination?.route) return //防止反复加载
    navigate(screenTag) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateTo(screenTag: String) {
    if (screenTag == currentDestination?.route) return //防止反复加载
    navigate(screenTag)
}