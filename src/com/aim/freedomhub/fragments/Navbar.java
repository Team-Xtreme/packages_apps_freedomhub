/*
 * Copyright (C) 2020 AIMROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aim.freedomhub.fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.aim.Utils;

import java.util.List;
import java.util.ArrayList;
import lineageos.providers.LineageSettings;
import com.aim.freedomhub.R;
import com.aim.freedomhub.utils.DeviceUtils;
import com.aim.freedomhub.utils.TelephonyUtils;
import org.lineageos.internal.util.ScreenType;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import lineageos.hardware.LineageHardwareManager;

public class Navbar extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String NAVBAR_ENABLE = "navbar_enable";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";

    private SwitchPreference mNavbarEnable;
    private boolean mIsNavSwitchingMode = false;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.freedomhub_navbar);
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        mHandler = new Handler();

        mNavbarEnable = (SwitchPreference) findPreference(NAVBAR_ENABLE);

        boolean showing = LineageSettings.System.getIntForUser(resolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR,
                Utils.hasNavbarByDefault(getActivity()) ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        mNavbarEnable.setChecked(showing);
        mNavbarEnable.setOnPreferenceChangeListener(this);

        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action homeLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);

        // Navigation bar home long press
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction);

        // Navigation bar home double tap
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction);

        // Navigation bar app switch long press
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNavbarEnable) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean showing = ((Boolean)newValue);
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.FORCE_SHOW_NAVBAR,
                    showing ? 1 : 0, UserHandle.USER_CURRENT);
            mNavbarEnable.setChecked(showing);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        } else if (preference == mHomeLongPressAction ||
                preference == mNavigationHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction ||
                preference == mNavigationHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction ||
                preference == mNavigationAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        LineageSettings.System.putIntForUser(resolver, LineageSettings.System.FORCE_SHOW_NAVBAR,
             Utils.hasNavbarByDefault(mContext) ? 1 : 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.FREEDOMHUB;
    }
}

