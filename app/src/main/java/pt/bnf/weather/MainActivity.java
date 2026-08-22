package pt.bnf.weather;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String URL_STRING = "https://www.bnf.pt/weather.json";
    private static final long REFRESH_MS = 60_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView updateTime;
    private final TextView[] names = new TextView[4];
    private final TextView[] temps = new TextView[4];
    private final TextView[] hums = new TextView[4];
    private final TextView[] batts = new TextView[4];

    private final int BG = Color.rgb(23, 32, 42);
    private final int CARD = Color.rgb(32, 43, 54);
    private final int TEXT = Color.rgb(244, 247, 250);
    private final int MUTED = Color.rgb(158, 171, 183);
    private final int HUM = Color.rgb(127, 214, 178);
    private final int GREEN = Color.rgb(110, 220, 145);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        refresh();
    }

    private TextView tv(String text, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 8, 10, 8);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        status = tv("●  Connecting", 13, MUTED, true);
        header.addView(status, new LinearLayout.LayoutParams(0, 36, 1));

        updateTime = tv("Last update: --", 10, MUTED, false);
        updateTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        header.addView(updateTime, new LinearLayout.LayoutParams(0, 36, 1));

        root.addView(header);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setRowCount(2);
        grid.setUseDefaultMargins(false);
        root.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1));

        for (int i = 0; i < 4; i++) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(6, 8, 6, 6);
            card.setBackgroundColor(CARD);

            names[i] = tv("Sensor " + (i + 1), 12, MUTED, true);
            temps[i] = tv("--°C", 26, TEXT, true);
            hums[i] = tv("Humidity  --%", 13, HUM, false);
            batts[i] = tv("Battery  --%", 10, MUTED, false);

            card.addView(names[i]);
            card.addView(temps[i]);
            card.addView(hums[i]);
            card.addView(batts[i]);

            GridLayout.LayoutParams p = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 2, 1f),
                    GridLayout.spec(i % 2, 1f));
            p.setMargins(3, 3, 3, 3);
            grid.addView(card, p);
        }

        setContentView(root);
    }

    private void refresh() {
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(URL_STRING).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                c.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                c.disconnect();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray sensors = root.optJSONArray("sensors");
                long updated = root.optLong("updated", 0);

                runOnUiThread(() -> updateUi(sensors, updated));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("●  Offline");
                    status.setTextColor(Color.rgb(180, 110, 110));
                });
            }
        }).start();

        handler.postDelayed(this::refresh, REFRESH_MS);
    }

    private void updateUi(JSONArray sensors, long updated) {
        int online = 0;
        for (int i = 0; i < 4; i++) {
            try {
                JSONObject s = sensors != null ? sensors.optJSONObject(i) : null;
                if (s == null) continue;

                String name = s.optString("name", "Sensor " + (i + 1));
                names[i].setText(name);

                if (!s.isNull("temperature"))
                    temps[i].setText(String.format(Locale.US, "%.1f°C", s.optDouble("temperature")));
                else
                    temps[i].setText("--°C");

                if (!s.isNull("humidity"))
                    hums[i].setText(String.format(Locale.US, "Humidity  %.0f%%", s.optDouble("humidity")));
                else
                    hums[i].setText("Humidity  --%");

                if (!s.isNull("battery"))
                    batts[i].setText(String.format(Locale.US, "Battery  %.0f%%", s.optDouble("battery")));
                else
                    batts[i].setText("Battery  --%");

                online++;
            } catch (Exception ignored) {}
        }

        status.setText("●  " + online + "/4");
        status.setTextColor(online > 0 ? GREEN : MUTED);

        if (updated > 0) {
            java.text.SimpleDateFormat fmt =
                    new java.text.SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault());
            updateTime.setText("Last update: " + fmt.format(new java.util.Date(updated * 1000L)));
        } else {
            updateTime.setText("Last update: --");
        }
    }
}
