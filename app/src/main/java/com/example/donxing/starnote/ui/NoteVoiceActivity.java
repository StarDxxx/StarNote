package com.example.donxing.starnote.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.donxing.starnote.R;

public class NoteVoiceActivity extends AppCompatActivity {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_show);

        setContentView(R.layout.note_voice_new);
    }
}
