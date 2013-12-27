package com.zst.xposed.softkeysfademod;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.DataOutputStream;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainXposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {
	
	XSharedPreferences mPref;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPref = new XSharedPreferences(Common.MY_PACKAGE_NAME);
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.android.systemui")) return;
		
		final Class<?> classSystemUIService = findClass("com.android.systemui.SystemUIService",
				lpparam.classLoader);
		hookSystemUIRestartBroadcast(classSystemUIService);
		
		if (isEnabled()) {
			final Class<?> classKeyButtonView = findClass(
					"com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader);
			hookKeyButtonView(classKeyButtonView);
		}
	}
	
	private void hookSystemUIRestartBroadcast(final Class<?> classSystemUIService) {
		XposedBridge.hookAllMethods(classSystemUIService, "onCreate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Service thiz = (Service) param.thisObject;
				thiz.getApplicationContext().registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						new Thread() {
							@Override
							public void run() {
								try {
									Process sh = Runtime.getRuntime().exec("sh");
									if (sh == null) return;
									DataOutputStream os = new DataOutputStream(sh.getOutputStream());
									os.writeBytes("pkill com.android.systemui\n");
									os.writeBytes("exit\n");
									sh.waitFor();
									os.close();
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}.start();
					}
				}, new IntentFilter(Common.BROADCAST_RESTART_SYSTEMUI));
			}
		});
		
	}
	
	private void hookKeyButtonView(final Class<?> classKeyButtonView) {
		XposedBridge.hookAllConstructors(classKeyButtonView, new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final ImageView thizz = (ImageView) param.thisObject;
				final Animation fade_out = new AlphaAnimation(1, 0);
				final Handler handler = new Handler(thizz.getContext().getMainLooper());
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						if (thizz.getVisibility() != View.VISIBLE) {
							// keys that are hidden should not animate
							return;
						}
						
						fade_out.setDuration(getSpeed());
						fade_out.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationRepeat(Animation a) {
							}
							
							@Override
							public void onAnimationStart(Animation a) {
							}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								thizz.setAlpha(0f);
							}
						});
						fade_out.reset();
						thizz.startAnimation(fade_out);
					}
				};
				
				thizz.getContext().registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						handler.removeCallbacks(runnable);
						handler.postDelayed(runnable, getDelay());
						// Restart the delay.
					}
				}, new IntentFilter(Common.BROADCAST_FADE));
				
				thizz.getContext().registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						fade_out.cancel();
						// Set to opaque immediately
						thizz.setAlpha(1f);
					}
				}, new IntentFilter(Common.BROADCAST_SHOW));
				
			}
		});
		XposedBridge.hookAllMethods(classKeyButtonView, "onTouchEvent", new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final ImageView thizz = (ImageView) param.thisObject;
				final MotionEvent event = (MotionEvent) param.args[0];
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					thizz.getContext().sendBroadcast(new Intent(Common.BROADCAST_FADE));
					break;
				case MotionEvent.ACTION_DOWN:
					thizz.getContext().sendBroadcast(new Intent(Common.BROADCAST_SHOW));
					break;
				}
			}
		});
	}
	
	/* Helper Methods */
	private boolean isEnabled() {
		mPref.reload();
		return mPref.getBoolean(Common.KEY_ENABLED, false);
	}
	
	private int getSpeed() {
		mPref.reload();
		return mPref.getInt(Common.KEY_FADE_SPEED, Common.DEFAULT_FADE_SPEED);
	}
	
	private int getDelay() {
		mPref.reload();
		return mPref.getInt(Common.KEY_FADE_DELAY, Common.DEFAULT_FADE_DELAY);
	}
	
}
