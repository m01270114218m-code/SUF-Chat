package com.pharaohparty.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TextView view = new TextView(this);
    view.setText("Pharaoh Party");
    view.setTextSize(30);
    view.setTextColor(Color.WHITE);
    view.setGravity(Gravity.CENTER);
    view.setBackgroundColor(Color.rgb(18,18,18));
    setContentView(view);
  }
}
