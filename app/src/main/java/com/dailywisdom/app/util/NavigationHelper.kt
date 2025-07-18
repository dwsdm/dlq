package com.dailywisdom.app.util

import android.app.Activity
import android.content.Intent

/**
 * Helper class to manage navigation between activities
 * to prevent the shaking/refreshing effect when moving between sections
 */
object NavigationHelper {
    
    /**
     * Navigate to a destination activity without animations
     * @param currentActivity The current activity
     * @param destinationClass The destination activity class
     * @param finishCurrent Whether to finish the current activity
     * @param clearTop Whether to clear the activity stack
     */
    fun navigateTo(
        currentActivity: Activity,
        destinationClass: Class<*>,
        finishCurrent: Boolean = false,
        clearTop: Boolean = false
    ) {
        // Check if we're already on the destination activity
        if (currentActivity.javaClass == destinationClass) {
            return
        }
        
        // Create intent with appropriate flags
        val intent = Intent(currentActivity, destinationClass)
        
        if (clearTop) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        // Start the activity without animations
        currentActivity.startActivity(intent)
        
        // Finish current activity if needed
        if (finishCurrent) {
            currentActivity.finish()
        }
    }
    
    /**
     * Handle back navigation without animations
     * @param activity The activity to finish
     */
    fun navigateBack(activity: Activity) {
        activity.finish()
    }
}
