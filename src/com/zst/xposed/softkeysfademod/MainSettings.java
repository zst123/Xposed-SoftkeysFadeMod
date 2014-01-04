package com.zst.xposed.softkeysfademod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class MainSettings extends PreferenceActivity implements OnPreferenceChangeListener {
	
	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		addPreferencesFromResource(R.xml.pref_main);
		findPreference(Common.KEY_ENABLED).setOnPreferenceChangeListener(this);
		findPreference(Common.KEY_FADE_ALPHA).setOnPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, final Object newValue) {
		final Context ctx = this;
		
		if (Common.KEY_ENABLED.equals(preference.getKey())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setMessage(R.string.config_enabled_dialog_message);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					restartSystemUI();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final String msg = getResources().getString((Boolean) newValue ?
								R.string.config_enabled_dialog_cancel_enabled :
								R.string.config_enabled_dialog_cancel_disabled);
					Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
				}
			});
			builder.create().show();
			return true;
		} else if (Common.KEY_FADE_ALPHA.equals(preference.getKey())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setMessage(R.string.config_others_dialog_message);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					restartSystemUI();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final String msg = getResources().getString(R.string.config_others_dialog_cancel);
					Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
				}
			});
			builder.create().show();
			return true;
				
		}
		return false;
	}
	
	private void restartSystemUI() {
		sendBroadcast(new Intent(Common.BROADCAST_RESTART_SYSTEMUI));
	}
}
