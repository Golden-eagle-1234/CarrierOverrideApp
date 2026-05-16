package com.custom.carrieroverride;

import android.telephony.SubscriptionInfo;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XSharedPreferences;

public class MainHook implements IXposedHookLoadPackage {

    private static XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.phone")) return;

        prefs = new XSharedPreferences("com.custom.carrieroverride", "carrier_prefs");
        prefs.makeWorldReadable();

        XposedBridge.log("[CarrierOverride] Hooking com.android.phone with XSharedPreferences");

        try {
            Class<?> subInfoClass = XposedHelpers.findClass(
                "android.telephony.SubscriptionInfo", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(subInfoClass, "getCarrierName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        prefs.reload();
                        SubscriptionInfo info = (SubscriptionInfo) param.thisObject;
                        int subId = info.getSubscriptionId();
                        String custom = prefs.getString("sim_" + subId, "");
                        if (!custom.isEmpty()) {
                            XposedBridge.log("[CarrierOverride] phone getCarrierName subId=" + subId + " -> " + custom);
                            param.setResult(custom);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[CarrierOverride] phone error: " + t.getMessage());
                    }
                }
            });
            XposedBridge.log("[CarrierOverride] Hooked phone SubscriptionInfo.getCarrierName");
        } catch (Throwable t) {
            XposedBridge.log("[CarrierOverride] phone hook failed: " + t.getMessage());
        }
    }
}
