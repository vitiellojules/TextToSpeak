package com.example.texttospeak;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TTSExampleActivity extends AppCompatActivity implements OnInitListener {

    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText editText;
    private ListView listView;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSpeak = findViewById(R.id.btnSpeak);
        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);

        tts = new TextToSpeech(this, this);

        database = openOrCreateDatabase("SpeechDB", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS Speeches (_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT);");

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });

        if (tts == null) {
            Toast.makeText(this, "Mecanismo TTS não configurado no dispositivo.", Toast.LENGTH_LONG).show();
            finish();
        }

        displaySpeeches();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int langResult = tts.setLanguage(Locale.US);
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Língua não suportada ou dados faltando");
            } else {
                btnSpeak.setEnabled(true);
            }
        } else {
            Log.e("TTS", "Falha na inicialização do TextToSpeech");
        }
    }

    private void speakOut() {
        String text = editText.getText().toString();

        if (!text.isEmpty()) {
            addSpeechToDatabase(text);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            editText.setText("");
            displaySpeeches();
        } else {
            Toast.makeText(this, "Insira texto para realizar a conversão.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSpeechToDatabase(String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        database.insert("Speeches", null, values);
    }

    private void displaySpeeches() {
        Cursor cursor = database.rawQuery("SELECT * FROM Speeches", null);

        String[] fromColumns = {"content"};
        int[] toViews = {android.R.id.text1};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, fromColumns, toViews, 0);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
}
