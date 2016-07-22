package github.luv.mockgeofix;

import android.os.FileObserver;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Write a specified value (default -13) to /proc/PID/oom_adj every time it is changed
 */
public class OomAdjOverrider {
    static String TAG = "OomAdjOverrider";

    private List<FileObserver> observers = new ArrayList<>();
    public int pid = android.os.Process.myPid();
    public String oomAdjPath = String.format("/proc/%d/oom_adj", pid);
    public int oomValue;

    // when we detect it's not possible to write to oom_adj we set this to true
    // and stop bothering trying to write to oom_adj again. it's ok for the user of this
    // class to reset to true and call .start() again when the problem might have been recovered
    public boolean phoneNotRooted = false;

    // it's ok to skip some errors (eg EPIPE) if they happen on a Xth run, but 'su' has already
    // executed successfully.
    // EPIPE on Xth run could happen when someone kills our 'su' process in that very short window
    // ... we just log it, ignore it and set oom_adj on the next run,
    // EPIPE fail on first run means the 'su' did not open a shell (but probably exited with an error
    // message)
    public boolean runSuccessfully = false;

    public OomAdjOverrider() {
        this(-13);
    }

    /**
     * Note that overriding does not start on creation but you must call start()
     *
     * @param oomValue Override /proc/PID/oom_adj to this value (default -13)
     */
    public OomAdjOverrider(int oomValue) {
        this.oomValue = oomValue;
        observers.add(new FileObserver(String.format("/proc/%d/oom_adj", pid)) {
            @Override
            public void onEvent(int i, String s) {
                OomAdjOverrider.this.onEvent(i, s);
            }
        });
        observers.add(new FileObserver(String.format("/proc/%d/oom_score", pid)) {
            @Override
            public void onEvent(int i, String s) {
                OomAdjOverrider.this.onEvent(i, s);
            }
        });
        observers.add(new FileObserver(String.format("/proc/%d/oom_score_adj", pid)) {
            @Override
            public void onEvent(int i, String s) {
                OomAdjOverrider.this.onEvent(i, s);
            }
        });
    }

    private void onEvent(int i, String s) {
        if (i != FileObserver.MODIFY) {
            return;
        }
        write();
    }

    /* read /proc/PID/oom_adj */
    private int read() throws IOException {
        try {
            FileReader in = new FileReader(oomAdjPath);
            try {
                char[] c = new char[256];
                in.read(c);
                return Integer.parseInt(new String(c).trim());
            } finally {
                in.close();
            }
        } catch (NumberFormatException ex) {
            throw new IOException("oom_adj value is not numerical", ex);
        }
    }

    /* write oomValue to /proc/PID/oom_adj */
    synchronized private void write() {
        // private so noone calls this in the main thread
        if (phoneNotRooted) {
            stop();
            return;
        }
        int currentOomValue;
        try {
            currentOomValue = read();
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
            return;
        }
        if (currentOomValue == oomValue) {
            return;
        }
        Log.i(TAG, String.format("writing %d to %s", oomValue, oomAdjPath));

        for (FileObserver o : observers) {
            o.stopWatching();
        }
        try {
            // this command works on android versions from Gingerbread up to N
            // (kernel version 3.10.0+) even though it says in the android kernel docs
            // oom_adj is obsolete
            run(String.format("echo %d > /proc/%d/oom_adj", oomValue, pid));
        } catch (RunException e) {
            errorHandler(e);
        }
        for (FileObserver o : observers) {
            o.startWatching();
        }
    }

    /* write oomValue to oom_adj and
     * start observing oom_adj and write our oomValue back to the file every time it is changed */
    public void start() {
        for (FileObserver o : observers) {
            o.startWatching();
        }
        Thread t = new Thread() {
            @Override
            public void run() {
                write();
                int currentOomValue;
                try {
                    currentOomValue = read();
                } catch (IOException ex) {
                    Log.e(TAG, ex.toString());
                    return;
                }
                if (currentOomValue == oomValue) {
                    Log.i(TAG, String.format("First write check: " +
                                    "oom_adj value written successfully: %d",
                            currentOomValue));
                } else {
                    Log.e(TAG, String.format("First write check: " +
                                    "oom_adj value not overridden - oom_adj is %d not %d",
                            currentOomValue, oomValue));
                }
            }
        };
        t.start();
    }

    /* stop observing */
    public void stop() {
        for (FileObserver o : observers) {
            o.stopWatching();
        }
    }

    /**
     * This method is triggered when an error occurs when executing su
     * (eg permission denied, su not found, "EPIPE" (su did not open shell))
     *
     * WARNING: this method is executed in the thread that executed su.
     * So be careful, for example, if you want to do something with UI from this method
     * (eg Toast) use activity.runOnUiThread
     */
    synchronized public void errorHandler(RunException ex) {
        Log.e(TAG, ex.toString());
        phoneNotRooted = true;
    }

    /**
     * Executes single command in "su" shell in the current thread
     *
     * @param command the command to execute
     * @throws RunException If anything goes wrong, this exception is thrown, the RunException
     *      message is ready to be displayed to the end user
     */
    private void run(String command) throws RunException {
        // private so noone calls this in the main thread
        try {
            Process process;
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                // su probably not found or permission denied
                Log.w(OomAdjOverrider.TAG, String.format("IOException (%s) when executing su",
                        e.toString()));
                throw new RunException("'su' not found. Do you have root?", e);
            }
            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            try {
                STDIN.write((command + "\n").getBytes("UTF-8"));
                STDIN.flush();
                STDIN.write("exit\n".getBytes("UTF-8"));
                STDIN.flush();

                // wait for the su shell process to finish
                process.waitFor();

                // exit code != 0 ... something went wrong
                if (process.exitValue() != 0) {
                    throw new RunException(String.format("su finished with non-zero exit code: %d",
                            process.exitValue()));
                }
            } finally {
                // make sure our our stream is closed and the process is destroyed
                try { STDIN.close(); }
                catch (IOException e) { /* might be closed already */ }
                process.destroy();
            }
        } catch (RunException e) {
            throw e;
        } catch (IOException e) {
            if (e.getMessage().contains("EPIPE") || e.getMessage().contains("Broken pipe")) {
                Log.w(OomAdjOverrider.TAG, String.format("EPIPE when executing %s", command));
                if (!runSuccessfully) {
                    throw new RunException("Broken pipe when executing su. Do you have root?", e);
                }
            } else {
                Log.w(OomAdjOverrider.TAG, String.format("IOException (%s) when executing %s",
                        e.toString(), command));
                throw new RunException(String.format("IOException when executing su: %s",
                        e.getMessage()), e);
            }
        } catch (Exception e) {
            Log.w(OomAdjOverrider.TAG, String.format("Exception (%s) when executing %s",
                    e.toString(), command));
            throw new RunException(String.format("Exception when executing su: %s",
                    e.getMessage()), e);
        }
        runSuccessfully = true;
    }
}

class RunException extends Exception {
    public RunException() {
        super();
    }

    public RunException(String detailMessage) {
        super(detailMessage);
    }

    public RunException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
