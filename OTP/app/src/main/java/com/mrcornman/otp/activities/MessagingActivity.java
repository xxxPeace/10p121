package com.mrcornman.otp.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mrcornman.otp.R;
import com.mrcornman.otp.adapters.MessageAdapter;
import com.mrcornman.otp.services.MessageService;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jonathan on 5/8/2015.
 */
public class MessagingActivity extends Activity {

    private MessageService.MessageServiceInterface messageService;
    private ClientMessageClientListener clientMessageClientListener = new ClientMessageClientListener();
    private ServiceConnection serviceConnection = new MessageServiceConnection();

    private String recipientId;
    private String currentUserId;

    private EditText messageBodyField;
    private String messageBody;

    private ListView messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        // get recipient from intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("recipient_id");
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        View sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageBody = messageBodyField.getText().toString();
                if(messageBody.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You need to say something!", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.i("MessagingActivity", "Sent message = " + messageBody + " (to " + recipientId + ")");
                messageService.sendMessage(recipientId, messageBody);
                messageBodyField.setText("");
            }
        });

        messageList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messageList.setAdapter(messageAdapter);

        // initialize message list with messages from history
        String[] userIds = { currentUserId, recipientId };
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipientId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    for(int i = 0; i < list.size(); i++) {
                        WritableMessage message = new WritableMessage(list.get(i).get("recipientId").toString(),
                                list.get(i).get("messageText").toString());

                        if(list.get(i).get("senderId").toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        messageService.removeMessageClientListener(clientMessageClientListener);
        super.onDestroy();
    }

    private class MessageServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            messageService = (MessageService.MessageServiceInterface) binder;
            messageService.addMessageClientListener(clientMessageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class ClientMessageClientListener implements MessageClientListener {

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            if(message.getSenderId().equals(recipientId)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            // check for duplicates in message history and display only if not duplicate
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if(e == null) {
                        if(list.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId);
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                        }
                    }
                }
            });
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo messageDeliveryInfo) {

        }

        @Override
        public void onMessageFailed(MessageClient client, Message message, MessageFailureInfo failureInfo) {
            Toast.makeText(MessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {

        }
    }
}
