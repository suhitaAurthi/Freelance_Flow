package com.example.freelanceflowandroid;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MyApp extends Application {
    private static final String TAG = "MyApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.w(TAG, "FirebaseApp.initializeApp failed", e);
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                Log.e(TAG, "Uncaught exception", throwable);

                // write stacktrace to a file in cache so user can retrieve it
                File cacheDir = getCacheDir();
                File out = new File(cacheDir, "crash_log.txt");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                pw.flush();
                String stack = sw.toString();

                try (FileOutputStream fos = new FileOutputStream(out, true)) {
                    fos.write(("--- CRASH at " + System.currentTimeMillis() + " ---\n").getBytes());
                    fos.write(stack.getBytes());
                    fos.write("\n\n".getBytes());
                    fos.flush();
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to write crash file", ex);
                }

                // Try to show a crash screen if possible
                try {
                    android.content.Intent i = new android.content.Intent(getApplicationContext(), CrashActivity.class);
                    i.putExtra("stack", stack);
                    i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    // allow the activity to show for a short moment
                    Thread.sleep(1000);
                } catch (Throwable ex) {
                    Log.e(TAG, "Failed to launch CrashActivity", ex);
                }

            } catch (Throwable ignored) {
                // ignore
            } finally {
                // rethrow to let OS handle it after we've logged
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(2);
            }
        });
    }
}