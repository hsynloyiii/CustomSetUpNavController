package com.example.customnavcontrollerlibrary

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.customview.widget.Openable
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.android.material.navigation.NavigationView
import java.lang.ref.WeakReference


fun NavigationView.customSetUpWithNavController(
    navController: NavController,
    closeDrawerLayoutDelay: Long = 150,
    @AnimRes @AnimatorRes enterAnim: Int = R.anim.nav_default_enter_anim,
    @AnimRes @AnimatorRes exitAnim: Int = R.anim.nav_default_exit_anim,
    @AnimRes @AnimatorRes popEnterAnim: Int = R.anim.nav_default_pop_enter_anim,
    @AnimRes @AnimatorRes popExitAnim: Int = R.anim.nav_default_pop_exit_anim
) = setUp(
    navigationView = this,
    navController = navController,
    closeDrawerLayoutDelay = closeDrawerLayoutDelay,
    enterAnim = enterAnim,
    exitAnim = exitAnim,
    popEnterAnim = popEnterAnim,
    popExitAnim = popExitAnim
)

private fun setUp(
    navigationView: NavigationView,
    navController: NavController,
    closeDrawerLayoutDelay: Long,
    @AnimRes @AnimatorRes enterAnim: Int,
    @AnimRes @AnimatorRes exitAnim: Int,
    @AnimRes @AnimatorRes popEnterAnim: Int,
    @AnimRes @AnimatorRes popExitAnim: Int
) {
    navigationView.setNavigationItemSelectedListener {
        val destinationSelect = onNavDestinationSelected(
            item = it,
            navController = navController,
            enterAnim,
            exitAnim,
            popEnterAnim,
            popExitAnim
        )
        if (destinationSelect) {
            val parent = navigationView.parent
            if (parent is Openable) {
                Handler(Looper.getMainLooper()).postDelayed({
                    parent.close()
                }, closeDrawerLayoutDelay)
            }
        }
        destinationSelect
    }
    val navigationWeakReference = WeakReference(navigationView)
    navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                // As needed to use removeListener when view is null, avoid using lambda expression
                if (navigationWeakReference.get() == null) {
                    navController.removeOnDestinationChangedListener(this)
                }
                navigationView.setCheckedItem(destination.id)
            }
        }
    )
}


private fun onNavDestinationSelected(
    item: MenuItem,
    navController: NavController,
    @AnimRes @AnimatorRes enterAnim: Int,
    @AnimRes @AnimatorRes exitAnim: Int,
    @AnimRes @AnimatorRes popEnterAnim: Int,
    @AnimRes @AnimatorRes popExitAnim: Int,
): Boolean {
    val navBuilder = NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true)

    if (navController.currentDestination?.parent!!.findNode(item.itemId) is ActivityNavigator.Destination) {
        navBuilder.setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .setPopEnterAnim(popEnterAnim)
            .setPopExitAnim(popExitAnim)
    } else {
        navBuilder.setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .setPopEnterAnim(popEnterAnim)
            .setPopExitAnim(popExitAnim)
    }
    if (item.order and Menu.CATEGORY_SECONDARY == 0) {
        navBuilder.setPopUpTo(
            navController.graph.findStartDestination().id,
            inclusive = false,
            saveState = true
        )
    }
    return try {
        navController.navigate(item.itemId, null, navBuilder.build())
        navController.currentDestination?.matchNavigationDestination(item.itemId) == true
    } catch (e: IllegalArgumentException) {
        false
    }
}

private fun NavDestination.matchNavigationDestination(@IdRes destinationId: Int): Boolean =
    hierarchy.any { it.id == destinationId }

