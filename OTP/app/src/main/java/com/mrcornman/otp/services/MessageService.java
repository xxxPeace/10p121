package com.mrcornman.otp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

/**
 * Created by Jonathan on 5/8/2015.
 */
public class MessageService extends Service implements SinchClientListener {
    private static final String SINCH_APP_KEY = "65331179-02a7-40aa-81a7-8742817e7bcb";
    private static final String SINCH_APP_SECRET = "Q8bCsKv1JUuinsZvvvMRNA==";
    private static final String SINCH_ENVIRONMENT = "sandbox.sinch.com";

    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();

    private Intent broadcastIntent = new Intent("com.mrcornman.otp.activities.MainActivity");
    private LocalBroadcastManager broadcaster;

    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;

    public void startSinchClient(String username) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(SINCH_APP_KEY)
                .applicationSecret(SINCH_APP_SECRET)
                .environmentHost(SINCH_ENVIRONMENT)
                .build();

        sinchClient.addSinchClientListener(this);

        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);

        sinchClient.checkManifest();
        sinchClient.start();
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    // required sinch client listener overrides

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        broadcaster = LocalBroadcastManager.getInstance(this);

        // get the current user id
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(currentUserId != null && !isSinchClientStarted()) {
            startSinchClient(currentUserId);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onClientStarted(SinchClient client) {
        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();

        Log.i("MessageService", "Sinch client successfully started.");

        broadcastIntent.putExtra("success", true);
        broadcaster.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        sinchClient = null;

        Log.i("MessageService", "Sinch client failed to start: " + error.getMessage());

        broadcastIntent.putExtra("success", false);
        broadcaster.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onClientStopped(SinchClient client) {
        sinchClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration) {}

    @Override
    public void onLogMessage(int level, String area, String message) {}

    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    // message service interaction functions
    public void sendMessage(String recipientUserId, String textBody) {
        if(messageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if(messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if(messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }

    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody) {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }
}