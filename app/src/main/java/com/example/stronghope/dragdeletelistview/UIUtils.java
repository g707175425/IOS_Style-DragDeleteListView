package com.example.stronghope.dragdeletelistview;

import android.content.Context;

public class UIUtils {

	/** dip转换px */
	public static int dip2px(Context context,int dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}
	
}


