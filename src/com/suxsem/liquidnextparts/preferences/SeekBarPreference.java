package com.suxsem.liquidnextparts.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public View onCreateView(ViewGroup parent) {
		
		LinearLayout linear = new LinearLayout(getContext());
		
		 LinearLayout.LayoutParams params_text_left = new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.WRAP_CONTENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT);
		 params_text_left.gravity = Gravity.LEFT;
		 params_text_left.weight  = 1.0f;

		LinearLayout.LayoutParams params_sb = new LinearLayout.LayoutParams(180,
               LinearLayout.LayoutParams.WRAP_CONTENT);
				params_sb.gravity = Gravity.RIGHT;

		TextView desc = new TextView(getContext());
		desc.setText("Set sensitivity");
		desc.setTextSize(18);
		desc.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		desc.setLayoutParams(params_text_left);
		
		SeekBar sbar = new SeekBar(getContext());
		
		TextView status = new TextView(getContext());
		status.setText("   LOL");
		status.setTextSize(18);
		status.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
		status.setLayoutParams(params_text_left);
		
		sbar.setLayoutParams(params_sb);
		linear.addView(desc, 0);
		linear.addView(sbar, 1);
		linear.addView(status, 2);
		return linear;
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
}
