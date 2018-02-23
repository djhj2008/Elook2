package com.elook.client.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.input.InputManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.elook.client.R;

import org.w3c.dom.Text;

/**
 * Created by xy on 16-2-2.
 */
public class ELookActionBar extends RelativeLayout implements View.OnClickListener{
    private static final String TAG = "ELookActionBar";
    private Context mContext;
    private LayoutInflater mInflater;
    private TextView mActionBarTitle, mActionBarSubTitle;

    private ArrayList<ActionBarListener> mListeners = new ArrayList<>();

    public ELookActionBar(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public ELookActionBar(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.action_bar, this, true);

        mActionBarTitle = (TextView)findViewById(R.id.action_bar_title);
        mActionBarSubTitle = (TextView)findViewById(R.id.action_bar_subtitle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.actionbar);
        String title = a.getString(R.styleable.actionbar_titleText);
        if(title == null || (title !=null && title.isEmpty())){
            mActionBarTitle.setVisibility(GONE);
        } else {
            mActionBarTitle.setText(title);
        }

        String subTitle = a.getString(R.styleable.actionbar_subTitleText);
        if(subTitle == null || (subTitle !=null && subTitle.isEmpty())){
            mActionBarSubTitle.setVisibility(GONE);
        } else {
            mActionBarSubTitle.setText(subTitle);
        }

        mActionBarSubTitle.setOnClickListener(this);
    }

    public void setActionBarTitle(String title){
        if(mActionBarTitle != null){
            mActionBarTitle.setText(title);
        }
    }

    public void setActionBarSubTitle(String title){
        if(mActionBarSubTitle != null){
            mActionBarSubTitle.setText(title);
        }
    }

    public void setActionBarTitle(int resId){
        setActionBarTitle(mContext.getString(resId));
    }

    public void setActionBarSubTitle(int resId){
        setActionBarSubTitle(mContext.getString(resId));
    }

    public TextView getActionBarTitle(boolean isSubtile){
        if(isSubtile){
            return mActionBarSubTitle;
        }
        return mActionBarTitle;
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if(mListeners != null){
            for (ActionBarListener listener : mListeners){
                listener.onMenuClicked(v);
            }
        }
    }

    public void setActionBarListener(ActionBarListener listener){
        if(listener != null){
            mListeners.add(listener);
        }
    }

    public interface ActionBarListener {
        void onMenuClicked(View v);
    }
}
