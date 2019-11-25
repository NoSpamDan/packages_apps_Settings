/*
 * Copyright (C) 2016 Paranoid Android
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

package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;

import com.android.internal.util.candy.ActionConstants;
import com.android.internal.util.candy.AppHelper;
import com.android.internal.util.candy.DeviceUtils;
import com.android.internal.util.candy.DeviceUtils.FilteredDeviceFeaturesArray;
import com.android.internal.util.candy.KernelControl;
import com.android.internal.util.candy.ShortcutPickerHelper;
import com.android.internal.util.candy.ShortcutPickerHelper.OnPickListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.SwitchBar;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GesturesSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener, Indexable {

    private static final String TAG = "GesturesSettings";

    private static final String SETTINGS_METADATA_NAME = "com.android.settings";
    //private static final String GESTURE_SETTINGS = "gestures_settings";

    private static final String KEY_DOUBLE_TAP = "double_tap";
    private static final String KEY_DRAW_V = "draw_v";
    private static final String KEY_DRAW_INVERSE_V = "draw_inverse_v";
    private static final String KEY_DRAW_O = "draw_o";
    private static final String KEY_DRAW_M = "draw_m";
    private static final String KEY_DRAW_W = "draw_w";
    private static final String KEY_DRAW_S = "draw_s";
    private static final String KEY_DRAW_ARROW_LEFT = "draw_arrow_left";
    private static final String KEY_DRAW_ARROW_RIGHT = "draw_arrow_right";
    private static final String KEY_ONE_FINGER_SWIPE_UP = "one_finger_swipe_up";
    private static final String KEY_ONE_FINGER_SWIPE_RIGHT = "one_finger_swipe_right";
    private static final String KEY_ONE_FINGER_SWIPE_DOWN = "one_finger_swipe_down";
    private static final String KEY_ONE_FINGER_SWIPE_LEFT = "one_finger_swipe_left";
    private static final String KEY_TWO_FINGER_SWIPE = "two_finger_swipe";

    private static final HashMap<String, Integer> mGesturesKeyCodes = new HashMap<>();
    private static final HashMap<String, Integer> mGesturesDefaults = new HashMap();
    private static final HashMap<String, String> mGesturesSettings = new HashMap();

    private static final int DLG_SHOW_ACTION_DIALOG  = 0;
    private static final int DLG_RESET_TO_DEFAULT    = 1;

    private static final int MENU_RESET = Menu.FIRST;

    static {
        mGesturesKeyCodes.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_doubleTapKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_V, com.android.internal.R.integer.config_drawVKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_INVERSE_V, com.android.internal.R.integer.config_drawInverseVKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_O, com.android.internal.R.integer.config_drawOKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_M, com.android.internal.R.integer.config_drawMKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_W, com.android.internal.R.integer.config_drawWKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_S, com.android.internal.R.integer.config_drawSKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_ARROW_LEFT, com.android.internal.R.integer.config_drawArrowLeftKeyCode);
        mGesturesKeyCodes.put(KEY_DRAW_ARROW_RIGHT, com.android.internal.R.integer.config_drawArrowRightKeyCode);
        mGesturesKeyCodes.put(KEY_ONE_FINGER_SWIPE_UP, com.android.internal.R.integer.config_oneFingerSwipeUpKeyCode);
        mGesturesKeyCodes.put(KEY_ONE_FINGER_SWIPE_RIGHT, com.android.internal.R.integer.config_oneFingerSwipeRightKeyCode);
        mGesturesKeyCodes.put(KEY_ONE_FINGER_SWIPE_DOWN, com.android.internal.R.integer.config_oneFingerSwipeDownKeyCode);
        mGesturesKeyCodes.put(KEY_ONE_FINGER_SWIPE_LEFT, com.android.internal.R.integer.config_oneFingerSwipeLeftKeyCode);
        mGesturesKeyCodes.put(KEY_TWO_FINGER_SWIPE, com.android.internal.R.integer.config_twoFingerSwipeKeyCode);
    }

    static {
        mGesturesDefaults.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_doubleTapDefault);
        mGesturesDefaults.put(KEY_DRAW_V, com.android.internal.R.integer.config_drawVDefault);
        mGesturesDefaults.put(KEY_DRAW_INVERSE_V, com.android.internal.R.integer.config_drawInverseVDefault);
        mGesturesDefaults.put(KEY_DRAW_O, com.android.internal.R.integer.config_drawODefault);
        mGesturesDefaults.put(KEY_DRAW_M, com.android.internal.R.integer.config_drawMDefault);
        mGesturesDefaults.put(KEY_DRAW_W, com.android.internal.R.integer.config_drawWDefault);
        mGesturesDefaults.put(KEY_DRAW_S, com.android.internal.R.integer.config_drawSDefault);
        mGesturesDefaults.put(KEY_DRAW_ARROW_LEFT, com.android.internal.R.integer.config_drawArrowLeftDefault);
        mGesturesDefaults.put(KEY_DRAW_ARROW_RIGHT, com.android.internal.R.integer.config_drawArrowRightDefault);
        mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_UP, com.android.internal.R.integer.config_oneFingerSwipeUpDefault);
        mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_RIGHT, com.android.internal.R.integer.config_oneFingerSwipeRightDefault);
        mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_DOWN, com.android.internal.R.integer.config_oneFingerSwipeDownDefault);
        mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_LEFT, com.android.internal.R.integer.config_oneFingerSwipeLeftDefault);
        mGesturesDefaults.put(KEY_TWO_FINGER_SWIPE, com.android.internal.R.integer.config_twoFingerSwipeDefault);
    }

    static {
        mGesturesSettings.put(KEY_DOUBLE_TAP, Settings.System.GESTURE_DOUBLE_TAP);
        mGesturesSettings.put(KEY_DRAW_V, Settings.System.GESTURE_DRAW_V);
        mGesturesSettings.put(KEY_DRAW_INVERSE_V, Settings.System.GESTURE_DRAW_INVERSE_V);
        mGesturesSettings.put(KEY_DRAW_O, Settings.System.GESTURE_DRAW_O);
        mGesturesSettings.put(KEY_DRAW_M, Settings.System.GESTURE_DRAW_M);
        mGesturesSettings.put(KEY_DRAW_W, Settings.System.GESTURE_DRAW_W);
        mGesturesSettings.put(KEY_DRAW_S, Settings.System.GESTURE_DRAW_S);
        mGesturesSettings.put(KEY_DRAW_ARROW_LEFT, Settings.System.GESTURE_DRAW_ARROW_LEFT);
        mGesturesSettings.put(KEY_DRAW_ARROW_RIGHT, Settings.System.GESTURE_DRAW_ARROW_RIGHT);
        mGesturesSettings.put(KEY_ONE_FINGER_SWIPE_UP, Settings.System.GESTURE_ONE_FINGER_SWIPE_UP);
        mGesturesSettings.put(KEY_ONE_FINGER_SWIPE_RIGHT, Settings.System.GESTURE_ONE_FINGER_SWIPE_RIGHT);
        mGesturesSettings.put(KEY_ONE_FINGER_SWIPE_DOWN, Settings.System.GESTURE_ONE_FINGER_SWIPE_DOWN);
        mGesturesSettings.put(KEY_ONE_FINGER_SWIPE_LEFT, Settings.System.GESTURE_ONE_FINGER_SWIPE_LEFT);
        mGesturesSettings.put(KEY_TWO_FINGER_SWIPE, Settings.System.GESTURE_TWO_FINGER_SWIPE);
    }

    private GesturesEnabler mGesturesEnabler;

    private SharedPreferences mScreenOffGestureSharedPreferences;

    private ShortcutPickerHelper mPicker;
    private String mPendingSettingsKey;
    private static FilteredDeviceFeaturesArray sFinalActionDialogArray;

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gestures_settings);

        for (String gestureKey : mGesturesKeyCodes.keySet()) {
            if (getResources().getInteger(mGesturesKeyCodes.get(gestureKey)) > 0) {
                findPreference(gestureKey).setOnPreferenceChangeListener(this);
            } else {
                removePreference(gestureKey);
            }
        }

        mPicker = new ShortcutPickerHelper(getActivity(), this);

        //mScreenOffGestureSharedPreferences = getActivity().getSharedPreferences(
        //        GESTURE_SETTINGS, Activity.MODE_PRIVATE);

        PackageManager pm = getActivity().getPackageManager();
        Resources settingsResources = null;
        try {
            settingsResources = pm.getResourcesForApplication(SETTINGS_METADATA_NAME);
        } catch (Exception e) {
            return;
        }

        sFinalActionDialogArray = new FilteredDeviceFeaturesArray();
        sFinalActionDialogArray = DeviceUtils.filterUnsupportedDeviceFeatures(getActivity(),
                settingsResources.getStringArray(
                        settingsResources.getIdentifier(SETTINGS_METADATA_NAME
                        + ":array/gesture_values", null, null)),
                settingsResources.getStringArray(
                        settingsResources.getIdentifier(SETTINGS_METADATA_NAME
                        + ":array/gesture_entries", null, null)));

        setHasOptionsMenu(true);
    }

    private void setupOrUpdatePreference(Preference preference, String action) {
        if (preference == null || action == null) {
            return;
        }

        if (action.startsWith("**")) {
            preference.setSummary(getDescription(action));
        } else {
            preference.setSummary(AppHelper.getFriendlyNameForUri(
                    getActivity(), getActivity().getPackageManager(), action));
        }
        //preference.setOnPreferenceClickListener(this);
    }

    private String getDescription(String action) {
        if (sFinalActionDialogArray == null || action == null) {
            return null;
        }
        int i = 0;
        for (String actionValue : sFinalActionDialogArray.values) {
            if (action.equals(actionValue)) {
                return sFinalActionDialogArray.entries[i];
            }
            i++;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGesturesEnabler != null) {
            mGesturesEnabler.teardownSwitchBar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SettingsActivity activity = (SettingsActivity) getActivity();
        mGesturesEnabler = new GesturesEnabler(activity.getSwitchBar());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGesturesEnabler != null) {
            mGesturesEnabler.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGesturesEnabler != null) {
            mGesturesEnabler.pause();
        }
    }

    private void enableGestures(boolean enable, boolean start) {
        for (String gestureKey : mGesturesKeyCodes.keySet()) {
            if (getResources().getInteger(mGesturesKeyCodes.get(gestureKey)) == 0) {
                continue;
            }
            ListPreference gesturePref = (ListPreference) findPreference(gestureKey);
            gesturePref.setEnabled(enable);
            if (start) {
                int gestureDefault = getResources().getInteger(
                        mGesturesDefaults.get(gestureKey));
                int gestureBehaviour = Settings.System.getInt(getContentResolver(),
                        mGesturesSettings.get(gestureKey), gestureDefault);
                gesturePref.setValue(String.valueOf(gestureBehaviour));
            }
        }
    }

    @Override
    public void shortcutPicked(String action,
                String description, Bitmap bmp, boolean isApplication) {
        if (mPendingSettingsKey == null || action == null) {
            return;
        }
        mScreenOffGestureSharedPreferences.edit().putString(mPendingSettingsKey, action).commit();
        //setupOrUpdatePreference(m);
        mPendingSettingsKey = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            }
        } else {
            mPendingSettingsKey = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                    showDialogInner(DLG_RESET_TO_DEFAULT, null, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void showDialogInner(int id, String settingsKey, int dialogTitle) {
        DialogFragment newFragment =
                MyAlertDialogFragment.newInstance(id, settingsKey, dialogTitle);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(
                int id, String settingsKey, int dialogTitle) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putString("settingsKey", settingsKey);
            args.putInt("dialogTitle", dialogTitle);
            frag.setArguments(args);
            return frag;
        }

        GesturesSettings getOwner() {
            return (GesturesSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            final String settingsKey = getArguments().getString("settingsKey");
            int dialogTitle = getArguments().getInt("dialogTitle");
            switch (id) {
                case DLG_SHOW_ACTION_DIALOG:
                    if (sFinalActionDialogArray == null) {
                        return null;
                    }
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(dialogTitle)
                    .setNegativeButton(R.string.cancel, null)
                    .setItems(getOwner().sFinalActionDialogArray.entries,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (getOwner().sFinalActionDialogArray.values[item]
                                    .equals(ActionConstants.ACTION_APP)) {
                                if (getOwner().mPicker != null) {
                                    getOwner().mPendingSettingsKey = settingsKey;
                                    getOwner().mPicker.pickShortcut(getOwner().getId());
                                }
                            } else {
                                getOwner().mScreenOffGestureSharedPreferences.edit()
                                        .putString(settingsKey,
                                        getOwner().sFinalActionDialogArray.values[item]).commit();
                                //getOwner().setupOrUpdatePreference();
                            }
                        }
                    })
                    .create();
                case DLG_RESET_TO_DEFAULT:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().resetToDefault();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Settings.System.putInt(getContentResolver(),
                mGesturesSettings.get(preference.getKey()),
                Integer.parseInt((String) newValue));
        return true;
    }

    public static boolean supportsGestures(Context context) {
        for (String gestureKey : mGesturesKeyCodes.keySet()) {
            if (context.getResources().getInteger(mGesturesKeyCodes
                    .get(gestureKey)) > 0) {
                return true;
            }
        }
        return false;
    }

    private class GesturesEnabler implements SwitchBar.OnSwitchChangeListener {

        private final Context mContext;
        private final SwitchBar mSwitchBar;
        private boolean mListeningToOnSwitchChange;

        public GesturesEnabler(SwitchBar switchBar) {
            mContext = switchBar.getContext();
            mSwitchBar = switchBar;

            mSwitchBar.show();

            boolean gesturesEnabled = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.GESTURES_ENABLED, 0) != 0;
            mSwitchBar.setChecked(gesturesEnabled);
            GesturesSettings.this.enableGestures(gesturesEnabled, true);
        }

        public void teardownSwitchBar() {
            pause();
            mSwitchBar.hide();
        }

        // Reset all entries to default.
        private void resetToDefault() {
            SharedPreferences.Editor editor = mScreenOffGestureSharedPreferences.edit();

            //mScreenOffGestureSharedPreferences.edit()
            //      .putBoolean(PREF_GESTURE_ENABLE, true).commit();

            mGesturesDefaults.put(KEY_DOUBLE_TAP, com.android.internal.R.integer.config_doubleTapDefault);
            mGesturesDefaults.put(KEY_DRAW_V, com.android.internal.R.integer.config_drawVDefault);
            mGesturesDefaults.put(KEY_DRAW_INVERSE_V, com.android.internal.R.integer.config_drawInverseVDefault);
            mGesturesDefaults.put(KEY_DRAW_O, com.android.internal.R.integer.config_drawODefault);
            mGesturesDefaults.put(KEY_DRAW_M, com.android.internal.R.integer.config_drawMDefault);
            mGesturesDefaults.put(KEY_DRAW_W, com.android.internal.R.integer.config_drawWDefault);
            mGesturesDefaults.put(KEY_DRAW_S, com.android.internal.R.integer.config_drawSDefault);
            mGesturesDefaults.put(KEY_DRAW_ARROW_LEFT, com.android.internal.R.integer.config_drawArrowLeftDefault);
            mGesturesDefaults.put(KEY_DRAW_ARROW_RIGHT, com.android.internal.R.integer.config_drawArrowRightDefault);
            mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_UP, com.android.internal.R.integer.config_oneFingerSwipeUpDefault);
            mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_RIGHT, com.android.internal.R.integer.config_oneFingerSwipeRightDefault);
            mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_DOWN, com.android.internal.R.integer.config_oneFingerSwipeDownDefault);
            mGesturesDefaults.put(KEY_ONE_FINGER_SWIPE_LEFT, com.android.internal.R.integer.config_oneFingerSwipeLeftDefault);
            mGesturesDefaults.put(KEY_TWO_FINGER_SWIPE, com.android.internal.R.integer.config_twoFingerSwipeDefault);

            KernelControl.enableGestures(true);
            //setupOrUpdatePreference();
        }

        public void resume() {
            if (!mListeningToOnSwitchChange) {
                mSwitchBar.addOnSwitchChangeListener(this);
                mListeningToOnSwitchChange = true;
            }
        }

        public void pause() {
            if (mListeningToOnSwitchChange) {
                mSwitchBar.removeOnSwitchChangeListener(this);
                mListeningToOnSwitchChange = false;
            }
        }

        @Override
        public void onSwitchChanged(Switch switchView, boolean isChecked) {
            Settings.System.putInt(
                    mContext.getContentResolver(),
                    Settings.System.GESTURES_ENABLED, isChecked ? 1 : 0);
            GesturesSettings.this.enableGestures(isChecked, false);
        }

    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER
            = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            final SearchIndexableResource sir =
                    new SearchIndexableResource(context);
            sir.xmlResId = R.xml.gestures_settings;
            return Arrays.asList(sir);
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = new ArrayList<String>();
            for (String gestureKey : mGesturesKeyCodes.keySet()) {
                if (context.getResources().getInteger(mGesturesKeyCodes
                        .get(gestureKey)) == 0) {
                    keys.add(gestureKey);
                }
            }
            return keys;
        }

    };
}
