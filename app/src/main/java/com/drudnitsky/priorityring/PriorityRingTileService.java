package com.drudnitsky.priorityring;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.media.RingtoneManager;
import android.net.Uri;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;

/**
 * TileService class for the Priority Ring app.
 * The service is managed by the Android OS.
 * See https://developer.android.com/develop/ui/views/quicksettings-tiles.
 */
public class PriorityRingTileService extends TileService {

	private AudioManager audioManager;

	// App preferences name constants.
	private static final String APP_PREFS = "priority-ring-prefs";
	private static final String RINGTONE = "ringtone";
	private static final String NOTIFICATION_SOUND = "notification-sound";
	private static final String IS_PR_ON = "is-priority-ring-on";	// Flag representing the state of priority ring. Needed for phone restarts.

	// Method called by the OS when the user adds the tile
	@Override
	public void onTileAdded() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		setTileUI(Tile.STATE_INACTIVE, "OFF");
		preferences.edit()
			.putBoolean(IS_PR_ON, false)
			.apply();
	}

	// Method called by the OS when the tile becomes visible (e.g., when the user expands the quick settings panel).
	// This is the primary method for controlling the tile UI.
	@Override
	public void onStartListening() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Tile tile = getQsTile();

		if(!checkAllowedPermissions()) {
			turnPriorityRingOff();
			setTileUI(Tile.STATE_UNAVAILABLE, "Check app permissions");
			return;
		}

		if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			turnPriorityRingOff();
			setTileUI(Tile.STATE_UNAVAILABLE, "Unavailable in silent mode");
			return;
		}

		if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
			turnPriorityRingOff();
			setTileUI(Tile.STATE_INACTIVE, "OFF");
			return;
		}

		if(preferences.getBoolean(IS_PR_ON, false))
			setTileUI(Tile.STATE_ACTIVE, "ON");
		else
			setTileUI(Tile.STATE_INACTIVE, "OFF");
	}

	// Method called by the OS when the user clicks on the tile.
	@Override
	public void onClick() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		Tile tile = getQsTile();

		// On click, if priority ring is on, turn it off.
		if(preferences.getBoolean(IS_PR_ON, false)) {
			turnPriorityRingOff();
			setTileUI(Tile.STATE_INACTIVE, "OFF");
		}
		// On click, if priority ring is off, turn it on.
		else {
			turnPriorityRingOn();
			setTileUI(Tile.STATE_ACTIVE, "ON");
		}
	}
	private void turnPriorityRingOn() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		Tile tile = getQsTile();

		// 1. Capture the current settings.
		String currentRingtone = Settings.System.getString(getContentResolver(), Settings.System.RINGTONE);
		String currentNotificationSound = Settings.System.getString(getContentResolver(), Settings.System.NOTIFICATION_SOUND);

		// 2. Set priority ring settings
		RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, null);
		RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, null);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		// 3. Store previous settings and state as preferences.
		preferences.edit()
			.putString(RINGTONE, currentRingtone)
			.putString(NOTIFICATION_SOUND, currentNotificationSound)
			.putBoolean(IS_PR_ON, true)
			.apply();
	}

	private void turnPriorityRingOff() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		Tile tile = getQsTile();

		// 1. Get previous settings.
		String prevRingtone = preferences.getString(RINGTONE, null);
		String prevNotificationSound = preferences.getString(NOTIFICATION_SOUND, null);

		// 2. Restore them.
		if (prevRingtone != null)
			RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, Uri.parse(prevRingtone));

		if (prevNotificationSound != null)
			RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(prevNotificationSound));

		// 3. Clear stored settings and capture state.
		preferences.edit()			
			.remove(RINGTONE)
			.remove(NOTIFICATION_SOUND)
			.putBoolean(IS_PR_ON, false)
			.apply();
	}

	private void setTileUI(int tileState, String subtitle) {
		Tile tile = getQsTile();

		tile.setState(tileState);
		tile.setSubtitle(subtitle);

		if(tileState == Tile.STATE_UNAVAILABLE)
			tile.setIcon(Icon.createWithResource(this, R.drawable.ic_priority_ring_tile_warning));
		else
			tile.setIcon(Icon.createWithResource(this, R.drawable.ic_priority_ring_tile_normal));

		tile.updateTile();
	}

	private boolean checkAllowedPermissions() {
		boolean canWriteSystemSettings = Settings.System.canWrite(this);
		return canWriteSystemSettings;
	}
}
