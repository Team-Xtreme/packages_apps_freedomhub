/*
 * Copyright (C) 2020 AIMROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aim.freedomhub.fragments;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import com.aim.freedomhub.preferences.SystemSettingSeekBarPreference;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_COLUMNS_PORTRAIT = "qs_columns_portrait";
    private static final String PREF_COLUMNS_LANDSCAPE = "qs_columns_landscape";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";

    private SystemSettingSeekBarPreference mQsColumnsPortrait;
    private SystemSettingSeekBarPreference mQsColumnsLandscape;
    private SystemSettingSeekBarPreference mQsRowsPortrait;
    private SystemSettingSeekBarPreference mQsRowsLandscape;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.freedomhub_qs);
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQsColumnsPortrait = (SystemSettingSeekBarPreference) findPreference(PREF_COLUMNS_PORTRAIT);
        int columnsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_PORTRAIT, 4, UserHandle.USER_CURRENT);
        mQsColumnsPortrait.setValue(columnsPortrait);
        mQsColumnsPortrait.setOnPreferenceChangeListener(this);

        mQsColumnsLandscape = (SystemSettingSeekBarPreference) findPreference(PREF_COLUMNS_LANDSCAPE);
        int columnsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.QS_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        mQsColumnsLandscape.setValue(columnsLandscape);
        mQsColumnsLandscape.setOnPreferenceChangeListener(this);

        mQsRowsPortrait = (SystemSettingSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_PORTRAIT, 2, UserHandle.USER_CURRENT);
        mQsRowsPortrait.setValue(rowsPortrait);
        mQsRowsPortrait.setOnPreferenceChangeListener(this);

        mQsRowsLandscape = (SystemSettingSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.QS_ROWS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
        mQsRowsLandscape.setValue(rowsLandscape);
        mQsRowsLandscape.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQsColumnsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_COLUMNS_PORTRAIT, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_COLUMNS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_ROWS_PORTRAIT, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_ROWS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.FREEDOMHUB;
    }
}
