/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2023 The LineageOS Project
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

package org.lineageos.settings.doze;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.android.settingslib.widget.MainSwitchPreference;

import org.lineageos.settings.R;

public class DozeSettingsFragment extends PreferenceFragmentCompat
        implements OnPreferenceChangeListener {

    private SwitchPreference mAlwaysOnDisplayPreference;
    private SwitchPreference mWakeOnGesturePreference;
    private SwitchPreference mPickUpPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPocketPreference;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.doze_settings, rootKey);

        SharedPreferences prefs =
                getActivity().getSharedPreferences("doze_settings", Activity.MODE_PRIVATE);
        if (savedInstanceState == null && !prefs.getBoolean("first_help_shown", false)) {
            showHelp();
        }

        boolean dozeEnabled = DozeUtils.isDozeEnabled(getActivity());

        MainSwitchPreference switchBar = findPreference(DozeUtils.DOZE_ENABLE);
        switchBar.setOnPreferenceChangeListener(this);
        switchBar.setChecked(dozeEnabled);

        mAlwaysOnDisplayPreference = (SwitchPreference) findPreference(DozeUtils.ALWAYS_ON_DISPLAY);
        mAlwaysOnDisplayPreference.setEnabled(dozeEnabled);
        mAlwaysOnDisplayPreference.setChecked(DozeUtils.isAlwaysOnEnabled(getActivity()));
        mAlwaysOnDisplayPreference.setOnPreferenceChangeListener(this);

        mWakeOnGesturePreference = (SwitchPreference) findPreference(DozeUtils.WAKE_ON_GESTURE_KEY);
        mWakeOnGesturePreference.setEnabled(dozeEnabled);
        mWakeOnGesturePreference.setOnPreferenceChangeListener(this);

        PreferenceCategory pickupSensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(
                        DozeUtils.CATEG_PICKUP_SENSOR);
        PreferenceCategory proximitySensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(
                        DozeUtils.CATEG_PROX_SENSOR);

        mPickUpPreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setEnabled(dozeEnabled);
        mPickUpPreference.setOnPreferenceChangeListener(this);

        mHandwavePreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_HAND_WAVE_KEY);
        mHandwavePreference.setEnabled(dozeEnabled);
        mHandwavePreference.setOnPreferenceChangeListener(this);

        mPocketPreference = (SwitchPreference) findPreference(DozeUtils.GESTURE_POCKET_KEY);
        mPocketPreference.setEnabled(dozeEnabled);
        mPocketPreference.setOnPreferenceChangeListener(this);

        // Hide proximity sensor related features if the device doesn't support them
        if (!DozeUtils.getProxCheckBeforePulse(getActivity())) {
            getPreferenceScreen().removePreference(proximitySensorCategory);
        }

        // Hide AOD if not supported and set all its dependents otherwise
        if (!DozeUtils.alwaysOnDisplayAvailable(getActivity())) {
            getPreferenceScreen().removePreference(mAlwaysOnDisplayPreference);
        } else {
            mWakeOnGesturePreference.setDependency(DozeUtils.ALWAYS_ON_DISPLAY);
            pickupSensorCategory.setDependency(DozeUtils.ALWAYS_ON_DISPLAY);
            proximitySensorCategory.setDependency(DozeUtils.ALWAYS_ON_DISPLAY);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (DozeUtils.DOZE_ENABLE.equals(preference.getKey())) {
            boolean isChecked = (Boolean) newValue;
            DozeUtils.enableDoze(getActivity(), isChecked);

            if (!isChecked) {
                DozeUtils.enableAlwaysOn(getActivity(), false);
                mAlwaysOnDisplayPreference.setChecked(false);
            }
            mAlwaysOnDisplayPreference.setEnabled(isChecked);
            mWakeOnGesturePreference.setEnabled(isChecked);
            mPickUpPreference.setEnabled(isChecked);
            mHandwavePreference.setEnabled(isChecked);
            mPocketPreference.setEnabled(isChecked);
        } else if (DozeUtils.ALWAYS_ON_DISPLAY.equals(preference.getKey())) {
            DozeUtils.enableAlwaysOn(getActivity(), (Boolean) newValue);
        }

        mHandler.post(() -> DozeUtils.checkDozeService(getActivity()));

        return true;
    }
    private void showHelp() {
        AlertDialog helpDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.doze_settings_help_title)
                .setMessage(R.string.doze_settings_help_text)
                .setPositiveButton(R.string.dialog_ok,
                        (dialog, which) -> {
                            getActivity()
                                    .getSharedPreferences("doze_settings", Activity.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("first_help_shown", true)
                                    .commit();
                            dialog.cancel();
                        })
                .create();
        helpDialog.show();
    }
}
