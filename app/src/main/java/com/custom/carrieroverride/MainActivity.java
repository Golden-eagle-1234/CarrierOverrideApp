package com.custom.carrieroverride;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout simContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge to edge
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        prefs = getSharedPreferences("carrier_prefs", 0x1);
        setContentView(buildUI());
        loadSims();
    }

    private View buildUI() {
        // Root scroll
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0F"));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(56), dp(20), dp(32));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(4), dp(16), dp(4), dp(24));

        TextView emoji = new TextView(this);
        emoji.setText("📡");
        emoji.setTextSize(40);
        header.addView(emoji);

        TextView title = new TextView(this);
        title.setText("Carrier Vanity");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(8), 0, 0);
        header.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Customize your carrier name per SIM");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#888899"));
        subtitle.setPadding(0, dp(4), 0, 0);
        header.addView(subtitle);

        root.addView(header);

        // SIM cards container
        simContainer = new LinearLayout(this);
        simContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(simContainer);

        // Info card
        View infoCard = buildInfoCard();
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        infoParams.topMargin = dp(16);
        root.addView(infoCard, infoParams);

        scroll.addView(root);
        return scroll;
    }

    private void loadSims() {
        simContainer.removeAllViews();

        try {
            SubscriptionManager sm = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> sims = sm.getActiveSubscriptionInfoList();

            if (sims == null || sims.isEmpty()) {
                TextView noSim = new TextView(this);
                noSim.setText("No SIM cards detected");
                noSim.setTextColor(Color.parseColor("#888899"));
                noSim.setTextSize(16);
                noSim.setPadding(dp(8), dp(16), dp(8), dp(16));
                simContainer.addView(noSim);
                return;
            }

            for (SubscriptionInfo info : sims) {
                View card = buildSimCard(info);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                p.bottomMargin = dp(16);
                simContainer.addView(card, p);
            }

        } catch (Exception e) {
            TextView err = new TextView(this);
            err.setText("⚠️ Could not read SIM info: " + e.getMessage());
            err.setTextColor(Color.parseColor("#FF6B6B"));
            err.setTextSize(14);
            simContainer.addView(err);
        }
    }

    private View buildSimCard(SubscriptionInfo info) {
        int subId = info.getSubscriptionId();
        int slot = info.getSimSlotIndex() + 1;
        String originalName = info.getCarrierName() != null ? info.getCarrierName().toString() : "Unknown";
        String displayName = info.getDisplayName() != null ? info.getDisplayName().toString() : "SIM " + slot;
        String savedName = prefs.getString("sim_" + subId, "");

        // Card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackground(buildGlassBackground(info.getIconTint()));

        // SIM header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // SIM slot badge
        TextView slotBadge = new TextView(this);
        slotBadge.setText("SIM " + slot);
        slotBadge.setTextSize(11);
        slotBadge.setTextColor(Color.WHITE);
        slotBadge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        slotBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        slotBadge.setBackground(buildBadgeBackground(info.getIconTint()));
        headerRow.addView(slotBadge);

        // Display name
        TextView simDisplayName = new TextView(this);
        simDisplayName.setText(displayName);
        simDisplayName.setTextSize(16);
        simDisplayName.setTextColor(Color.WHITE);
        simDisplayName.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        simDisplayName.setPadding(dp(12), 0, 0, 0);
        LinearLayout.LayoutParams dnParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        headerRow.addView(simDisplayName, dnParams);

        card.addView(headerRow);

        // Original carrier label
        TextView originalLabel = new TextView(this);
        originalLabel.setText("Current: " + originalName);
        originalLabel.setTextSize(12);
        originalLabel.setTextColor(Color.parseColor("#888899"));
        originalLabel.setPadding(0, dp(12), 0, dp(4));
        card.addView(originalLabel);

        // Input label
        TextView inputLabel = new TextView(this);
        inputLabel.setText("Custom name");
        inputLabel.setTextSize(12);
        inputLabel.setTextColor(Color.parseColor("#AAAACC"));
        inputLabel.setPadding(0, dp(8), 0, dp(4));
        card.addView(inputLabel);

        // Input field
        EditText input = new EditText(this);
        input.setHint("e.g. True 5G ⚡");
        input.setHintTextColor(Color.parseColor("#555566"));
        input.setText(savedName);
        input.setTextColor(Color.WHITE);
        input.setTextSize(16);
        input.setPadding(dp(16), dp(14), dp(16), dp(14));
        input.setBackground(buildInputBackground());
        input.setSingleLine(true);
        card.addView(input);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnRowParams.topMargin = dp(14);
        btnRow.setLayoutParams(btnRowParams);

        // Apply button
        TextView applyBtn = new TextView(this);
        applyBtn.setText("Apply");
        applyBtn.setTextSize(14);
        applyBtn.setTextColor(Color.WHITE);
        applyBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        applyBtn.setGravity(android.view.Gravity.CENTER);
        applyBtn.setPadding(dp(20), dp(12), dp(20), dp(12));
        applyBtn.setBackground(buildButtonBackground(Color.parseColor("#4C6EF5")));
        LinearLayout.LayoutParams applyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        applyParams.rightMargin = dp(8);
        applyBtn.setLayoutParams(applyParams);
        applyBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                toast("Please enter a carrier name");
                return;
            }
            prefs.edit().putString("sim_" + subId, text).apply();
            toast("✅ Applied! Restart SystemUI or reboot to see changes");
            originalLabel.setText("Custom: " + text);
        });
        btnRow.addView(applyBtn);

        // Reset button
        TextView resetBtn = new TextView(this);
        resetBtn.setText("Reset");
        resetBtn.setTextSize(14);
        resetBtn.setTextColor(Color.parseColor("#FF6B6B"));
        resetBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        resetBtn.setGravity(android.view.Gravity.CENTER);
        resetBtn.setPadding(dp(20), dp(12), dp(20), dp(12));
        resetBtn.setBackground(buildButtonBackground(Color.parseColor("#2A1A1A")));
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        resetParams.leftMargin = dp(8);
        resetBtn.setLayoutParams(resetParams);
        resetBtn.setOnClickListener(v -> {
            prefs.edit().remove("sim_" + subId).apply();
            input.setText("");
            originalLabel.setText("Current: " + originalName);
            toast("🔄 Reset to default. Reboot to apply.");
        });
        btnRow.addView(resetBtn);

        card.addView(btnRow);
        return card;
    }

    private View buildInfoCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.setBackground(buildGlassBackground(Color.parseColor("#1A1A2E")));

        TextView title = new TextView(this);
        title.setText("ℹ️  How it works");
        title.setTextSize(13);
        title.setTextColor(Color.parseColor("#AAAACC"));
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        card.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Names are saved and applied via LSPosed hook on every boot. Changes take effect after restarting SystemUI or rebooting.");
        desc.setTextSize(12);
        desc.setTextColor(Color.parseColor("#666677"));
        desc.setPadding(0, dp(6), 0, 0);
        card.addView(desc);

        return card;
    }

    private android.graphics.drawable.GradientDrawable buildGlassBackground(int tint) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(20));
        // Glass effect: semi-transparent with tinted border
        int alpha = Color.argb(30, Color.red(tint), Color.green(tint), Color.blue(tint));
        d.setColor(Color.argb(60, 20, 20, 35));
        d.setStroke(dp(1), Color.argb(60, Color.red(tint), Color.green(tint), Color.blue(tint)));
        return d;
    }

    private android.graphics.drawable.GradientDrawable buildBadgeBackground(int color) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(8));
        d.setColor(Color.argb(80, Color.red(color), Color.green(color), Color.blue(color)));
        d.setStroke(1, Color.argb(120, Color.red(color), Color.green(color), Color.blue(color)));
        return d;
    }

    private android.graphics.drawable.GradientDrawable buildInputBackground() {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(14));
        d.setColor(Color.argb(80, 15, 15, 25));
        d.setStroke(dp(1), Color.parseColor("#2A2A3E"));
        return d;
    }

    private android.graphics.drawable.GradientDrawable buildButtonBackground(int color) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(12));
        d.setColor(color);
        return d;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
