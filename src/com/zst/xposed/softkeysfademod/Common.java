package com.zst.xposed.softkeysfademod;

public class Common {
	
	public static final String MY_PACKAGE_NAME = Common.class.getPackage().getName();
	
	/* BROADCAST INTENTS */
	public static final String BROADCAST_RESTART_SYSTEMUI = MY_PACKAGE_NAME + "_RESTART_SYSTEMUI";
	public static final String BROADCAST_FADE = MY_PACKAGE_NAME + "_FADE";
	public static final String BROADCAST_SHOW = MY_PACKAGE_NAME + "_SHOW";
	
	/* PREFERENCE KEYS */
	public static final String KEY_ENABLED = "config_enabled";
	public static final String KEY_FADE_DELAY = "config_delay";
	public static final String KEY_FADE_SPEED = "config_speed";
	public static final String KEY_FADE_ALPHA = "config_min_alpha";
	
	/* DEFAULT VALUES */
	public static final int DEFAULT_FADE_DELAY = 2000;
	public static final int DEFAULT_FADE_SPEED = 1000;
	public static final float DEFAULT_FADE_ALPHA = 0f;
}
