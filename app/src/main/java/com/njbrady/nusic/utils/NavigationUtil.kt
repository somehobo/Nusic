package com.njbrady.nusic.utils

import androidx.navigation.NavController

fun NavController.destinationExistsInBackStack(destination: String): Boolean {
    return backQueue.any { it.destination.route == destination }
}