package com.symemo;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.CircularProgressButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import top.jim9606.networktestapp.FileTransferClient;

public class SyncActivity extends AppCompatActivity {
    public static final String LogTag = "SyncActivity";
    private static final int duration = 4000;
    private CircularProgressButton regBtn,upBtn,dlBtn;
    private EditText userEdit,passwordEdit;
    private File dbFile;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;

    Handler regHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FileTransferClient.finishMsg_register:
                    regBtn.setProgress(100);
                    Log.i(LogTag, msg.toString());
                    break;
                case FileTransferClient.errorMsg:
                    if (msg.arg1 == HttpURLConnection.HTTP_FORBIDDEN) {
                        regBtn.setErrorText(getString(R.string.error_existed_user));
                    } else {
                        regBtn.setErrorText(getString(R.string.error_network));
                    }
                    regBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
                    break;
                default:
                    regBtn.setErrorText(getString(R.string.error_unknown));
                    regBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    regBtn.setProgress(0);
                }
            }, duration);
        }
    };

    Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FileTransferClient.finishMsg_update:
                    upBtn.setProgress(100);
                    Log.i(LogTag, msg.toString());
                    postReset();
                    break;
                case FileTransferClient.progressMsg_update:
                    upBtn.setProgress(msg.arg1 * 90 / msg.arg2 + 10);
                    break;
                case FileTransferClient.errorMsg:
                    if (msg.arg1 == HttpURLConnection.HTTP_FORBIDDEN) {
                        upBtn.setErrorText(getString(R.string.error_unauthorized));
                    } else {
                        upBtn.setErrorText(getString(R.string.error_network));
                    }
                    upBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
                    postReset();
                    break;
                default:
                    upBtn.setErrorText(getString(R.string.error_unknown));
                    upBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
                    postReset();
            }
        }

        void postReset() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    upBtn.setProgress(0);
                }
            },duration);
        }
    };

    Handler dlHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FileTransferClient.finishMsg_download:
                    dlBtn.setProgress(100);
                    Log.i(LogTag, msg.toString());
                    postReset();
                    break;
                case FileTransferClient.progressMsg_download:
                    dlBtn.setProgress(msg.arg1 * 90 / msg.arg2 + 10);
                    break;
                case FileTransferClient.errorMsg:
                    if (msg.arg1 == HttpURLConnection.HTTP_FORBIDDEN) {
                        dlBtn.setErrorText(getString(R.string.error_unauthorized));
                    } else {
                        dlBtn.setErrorText(getString(R.string.error_network));
                    }
                    dlBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
                    postReset();
                    break;
                default:
                    dlBtn.setErrorText(getString(R.string.error_unknown));
                    dlBtn.setProgress(-1);
                    Log.e(LogTag, msg.toString());
                    postReset();
            }
        }

        void postReset() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    dlBtn.setProgress(0);
                }
            },duration);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        setTitle("");
        regBtn = (CircularProgressButton)findViewById(R.id.sync_button_reg);
        upBtn = (CircularProgressButton)findViewById(R.id.sync_button_upload);
        dlBtn  = (CircularProgressButton)findViewById(R.id.sync_button_download);
        userEdit = (EditText)findViewById(R.id.sync_edit_user);
        passwordEdit = (EditText)findViewById(R.id.sync_edit_password);

        dbFile = getDatabasePath(MainActivity.dbName);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButton();
            }
        };
        userEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CharSequence res = checkUser();
                if (!s.toString().isEmpty())
                    userEdit.setError(res);
            }
        });
        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CharSequence res = checkPassword();
                if (!s.toString().isEmpty())
                    passwordEdit.setError(res);
            }
        });

        regBtn.setIndeterminateProgressMode(true);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValid()) return;
                regBtn.setProgress(10);
                final FileTransferClient client = new FileTransferClient(userEdit.getText().toString(), passwordEdit.getText().toString(), regHandler);
                new Thread() {
                    @Override
                    public void run() {
                        int code = client.register();
                    }
                }.start();
            }
        });
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValid()) return;
                upBtn.setProgress(10);
                final FileTransferClient client = new FileTransferClient(userEdit.getText().toString(),passwordEdit.getText().toString(),updateHandler);
                new Thread() {
                    @Override
                    public void run() {
                        int code = client.update(dbFile.toURI());
                    }
                }.start();
            }
        });
        dlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValid()) return;
                dlBtn.setProgress(1);
                try {
                    String cachePath = new String("file://" + getCacheDir().getAbsolutePath() + "/dl.db");
                    URI cacheURI = new URI(cachePath);
                    final File cacheFile = new File(cacheURI);
                    if (cacheFile.exists())
                        cacheFile.delete();
                    final FileTransferClient client = new FileTransferClient(userEdit.getText().toString(), passwordEdit.getText().toString(), dlHandler);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                int code = client.download(cacheFile.toURI());
                                if (code != HttpURLConnection.HTTP_OK) return;
                                FileInputStream fin = new FileInputStream(cacheFile);
                                FileOutputStream fout = new FileOutputStream(dbFile, false);
                                byte[] buffer = new byte[1024];
                                int ptr = 0;
                                while ((ptr = fin.read(buffer)) != -1) {
                                    fout.write(buffer, 0, ptr);
                                }
                                fin.close();
                                fout.close();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                catch (Exception e) {
                    dlHandler.sendEmptyMessage(FileTransferClient.errorMsg);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        preferences = getSharedPreferences("account",MODE_PRIVATE);
        preferenceEditor = preferences.edit();
        userEdit.setText(preferences.getString("name",null));
        passwordEdit.setText(preferences.getString("password",null));
    }

    @Override
    public void onStop() {
        super.onStop();
        String name = userEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (!checkValid()) {
            name = null;password = null;
        }
        preferenceEditor.putString("name",name);
        preferenceEditor.putString("password",password);
        preferenceEditor.commit();
    }

    private CharSequence checkUser() {
        String c = userEdit.getText().toString();
        if (!c.matches("[\\w@_]*"))
            return getString(R.string.error_invalid_characters);
        else if (!c.matches("[\\w@_]{3,}"))
            return getString(R.string.error_too_short);
        else if (!c.matches("[\\w@_]{3,20}"))
            return getString(R.string.error_too_long);
        else
            return null;
    }

    private CharSequence checkPassword() {
        String c = passwordEdit.getText().toString();
        if (!c.matches("[\\w]*"))
            return getString(R.string.error_invalid_characters);
        else if (!c.matches("[\\w]{6,}"))
            return getString(R.string.error_too_short);
        else if (!c.matches("[\\w]{6,25}"))
            return getString(R.string.error_too_long);
        else
            return null;
    }

    private boolean checkValid() {
        return checkUser() == null && checkPassword() == null;
    }

    private void resetButton() {
        regBtn.setProgress(0);
        upBtn.setProgress(0);
        dlBtn.setProgress(0);
    }
}
