package org.baiyu.fuckkiller;

import de.robv.android.xposed.XSharedPreferences;

public class Settings {

    private static final XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    private static final String PREF_MAX_CACHED_PROCESSES = "MAX_CACHED_PROCESSES";
    private static final String PREF_MAX_PHANTOM_PROCESSES = "MAX_PHANTOM_PROCESSES";
    private static final String PREF_RECENT_TASKS_HOOK = "RECENT_TASKS_HOOK";
    private static final int MIN_PROCESSES = 4;

    private Settings() {}

    private static class SingletonHelper {
        private static final Settings INSTANCE = new Settings();
    }

    static Settings getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public int getMinProcesses() {
        return MIN_PROCESSES;
    }

    public int getMaxCachedProcesses() {
        return Integer.parseInt(prefs.getString(PREF_MAX_CACHED_PROCESSES, "1024"));
    }

    public int getMaxPhantomProcesses() {
        return Integer.parseInt(prefs.getString(PREF_MAX_PHANTOM_PROCESSES, "1024"));
    }

    public boolean isRecentTasksHookEnabled() {
        return prefs.getBoolean(PREF_RECENT_TASKS_HOOK, true);
    }

}
