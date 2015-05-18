package com.mrcornman.otp.adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mrcornman.otp.R;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan on 5/9/2015.
 */
public class MessageAdapter extends BaseAdapter {
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Pair<WritableMessage, Integer>> messages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<>();
    }

    public void addMessage(WritableMessage message, int direction) {
        messages.add(new Pair(message, direction));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);

        // show message left or right depending on direction
        if(convertView == null) {
            int res = 0;
            switch (direction) {
                case DIRECTION_INCOMING:
                    res = R.layout.message_right;
                    break;
                case DIRECTION_OUTGOING:
                    res = R.layout.message_left;
                    break;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        WritableMessage message = messages.get(i).first;

        TextView messageText = (TextView) convertView.findViewById(R.id.txtMessage);
        messageText.setText(message.getTextBody());

        return convertView;
    }
}