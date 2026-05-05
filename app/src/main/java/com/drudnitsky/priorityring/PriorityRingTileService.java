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
	private static final String APP_PREFS = "priority-ring-prefs";
	private static final String RINGTONE = "ringtone";
	private static final String NOTIFICATION_SOUND = "notification-sound";

	// Method called by the OS when the user adds the tile
	@Override
	public void onTileAdded() {
		setTileUI(Tile.STATE_INACTIVE, "OFF");
	}

	// Method called by the OS when the tile becomes visible (e.g., when the user expands the quick settings panel).
	// This is the primary method for controlling the tile UI.
	@Override
	public void onStartListening() {
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

		if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || tile.getState() == Tile.STATE_UNAVAILABLE) {
			turnPriorityRingOff();
			setTileUI(Tile.STATE_INACTIVE, "OFF");
			return;
		}
	}

	// Method called by the OS when the user clicks on the tile.
	@Override
	public void onClick() {
		Tile tile = getQsTile();

		switch(tile.getState()) {
			// Tile: OFF > ON (turning on)
			case Tile.STATE_INACTIVE:
				turnPriorityRingOn();
				setTileUI(Tile.STATE_ACTIVE, "ON");
				break;

			// Tile: ON > OFF (turning off)
			case Tile.STATE_ACTIVE:
				turnPriorityRingOff();
				setTileUI(Tile.STATE_INACTIVE, "OFF");
				break;
		}
	}
	private void turnPriorityRingOn() {
		SharedPreferences preferences = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
		Tile tile = getQsTile();

		// 1. Capture the current settings.
		String currentRingtone = Settings.System.getString(getContentResolver(), Settings.System.RINGTONE);
		String currentNotificationSound = Settings.System.getString(getContentResolver(), Settings.System.NOTIFICATION_SOUND);

		// 2. Store as preferences.
		preferences.edit()
			.putString(RINGTONE, currentRingtone)
			.putString(NOTIFICATION_SOUND, currentNotificationSound)
			.apply();

		// 3. Set priority ring settings
		RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, null);
		RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, null);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
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

		// 3. Clear stored settings.
		preferences.edit().clear().apply();
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
