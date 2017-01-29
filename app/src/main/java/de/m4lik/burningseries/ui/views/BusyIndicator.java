package de.m4lik.burningseries.ui.views;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.pnikosis.materialishprogress.ProgressWheel;

import static de.m4lik.burningseries.services.ThemeHelperService.primaryColor;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public class BusyIndicator extends ProgressWheel {
    public BusyIndicator(Context context) {
        super(context);
        init();
    }

    public BusyIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBarColor(ContextCompat.getColor(getContext(), primaryColor()));
        spin();
    }
}
