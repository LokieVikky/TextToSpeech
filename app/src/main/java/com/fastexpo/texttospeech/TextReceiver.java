package com.fastexpo.texttospeech;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TextReceiver extends FirebaseMessagingService {
    TextToSpeech tts;
    private static final String TAG = "TextReceiver";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String textToSpeak = data.get("TextToSpeak");
        Log.d(TAG, "onMessageReceived: " + textToSpeak);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                    tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(new Random().nextInt()));
                }
            }
        });
    }


}
