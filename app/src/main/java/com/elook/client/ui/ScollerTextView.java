package com.elook.client.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by xy on 7/14/16.
 */
public class ScollerTextView extends TextView {


    public ScollerTextView(Context context) {
        super(context);
    }

    public ScollerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScollerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
