/*
* Copyright (C) 2013 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.lineageos.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.PreferenceManager;
import android.util.Log;

import org.lineageos.settings.dirac.DiracUtils;
import org.lineageos.settings.doze.DozeUtils;
import org.lineageos.settings.soundcontrol.SoundControlSettings;
import org.lineageos.settings.soundcontrol.SoundControlFileUtils;

public class Startup extends BroadcastReceiver {

    private static final boolean DEBUG = false;
    private static final String TAG = "BootReceiver";
    private static final String ONE_TIME_TUNABLE_RESTORE = "hardware_tunable_restored";

    private void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        if (enabled) {
            Utils.writeValue(file, "1");
        }
    }

    private void restore(String file, String value) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, value);
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        boolean enabled = false;
        if (DEBUG) Log.d(TAG, "Received boot completed intent");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_OTG_SWITCH, false);
        restore(OTGModeSwitch.getFile(), enabled);
        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_GAME_SWITCH, false);
        restore(GameModeSwitch.getFile(), enabled);
        new DiracUtils(context).onBootCompleted();
        DozeUtils.checkDozeService(context);

    int gain = Settings.Secure.getInt(context.getContentResolver(),
                SoundControlSettings.PREF_HEADPHONE_GAIN, 0);
        SoundControlFileUtils.setValue(SoundControlSettings.HEADPHONE_GAIN_PATH, gain + " " + gain);
        SoundControlFileUtils.setValue(SoundControlSettings.MICROPHONE_GAIN_PATH, Settings.Secure.getInt(context.getContentResolver(),
                SoundControlSettings.PREF_MICROPHONE_GAIN, 0));

        //FPS
        enabled = sharedPrefs.getBoolean(DeviceSettings.PREF_KEY_FPS_INFO, false);
        if (enabled) {
            Utils.startService(context, FPSInfoService.class);
    }
    }

    private boolean hasRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ONE_TIME_TUNABLE_RESTORE, false);
    }

    private void setRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(ONE_TIME_TUNABLE_RESTORE, true).apply();
    }
}
