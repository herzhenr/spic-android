package com.henrikherzig.playintegritychecker.ui.navigationbar


import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.henrikherzig.playintegritychecker.R

sealed class BottomNavItem(@StringRes var title:Int, var icon: ImageVector, var screen_route:String){

    object PlayIntegrity : BottomNavItem(R.string.bottomBar_playIntegrity, Icons.Filled.VerifiedUser ,"play_integrity")
    object SafetyNet: BottomNavItem(R.string.bottomBar_safetyNet,Icons.Filled.VerifiedUser,"safety_net")
    object Settings: BottomNavItem(R.string.bottomBar_settings, Icons.Filled.Settings,"settings")
    object About: BottomNavItem(R.string.bottomBar_about,Icons.Filled.Info,"about")
}