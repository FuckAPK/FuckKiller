package org.lyaaz.fuckkiller

import de.robv.android.xposed.XSharedPreferences
import kotlin.concurrent.Volatile

class Settings private constructor(private val prefs: XSharedPreferences) {
    val maxCachedProcesses: Int
        get() = prefs.getString(
            PREF_MAX_CACHED_PROCESSES,
            "1024"
        )!!.toInt()

    val maxPhantomProcesses: Int
        get() = prefs.getString(
            PREF_MAX_PHANTOM_PROCESSES,
            "1024"
        )!!.toInt()

    val isRecentTasksHookEnabled: Boolean
        get() = prefs.getBoolean(PREF_RECENT_TASKS_HOOK, true)

    companion object {
        private const val PREF_MAX_CACHED_PROCESSES = "MAX_CACHED_PROCESSES"
        private const val PREF_MAX_PHANTOM_PROCESSES = "MAX_PHANTOM_PROCESSES"
        private const val PREF_RECENT_TASKS_HOOK = "RECENT_TASKS_HOOK"

        @Volatile
        private var INSTANCE: Settings? = null

        fun getInstance(prefs: XSharedPreferences): Settings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Settings(prefs).also { INSTANCE = it }
            }
        }
    }
}
