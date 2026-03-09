package com.fakeroot.manager.home

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

/**
 * Sealed class representing items in the home screen
 */
sealed class HomeItem {

    /**
     * Status display item
     */
    data class Status(
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int,
        @ColorRes val statusColor: Int
    ) : HomeItem()

    /**
     * Action button item
     */
    data class Action(
        @StringRes val titleRes: Int,
        @StringRes val summaryRes: Int,
        val action: ActionType
    ) : HomeItem()

    /**
     * Action types
     */
    enum class ActionType {
        START_SERVICE,
        STOP_SERVICE,
        MANAGE_APPS,
        TERMINAL,
        SETTINGS
    }
}
