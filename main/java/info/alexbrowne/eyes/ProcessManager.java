package info.alexbrowne.eyes;

import java.io.File;
import java.util.concurrent.Semaphore;

/**
 * Created by alex on 11/16/14.
 */
public class ProcessManager {
    private boolean isReady = true;
    private Speaker speaker;
    private ImageProcessor p;
    private Semaphore processSem = new Semaphore(1);

    public ProcessManager(Speaker speaker) {
        this.speaker = speaker;
    }

    public boolean isReady() {
        try {
            processSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean val = isReady;
        processSem.release();
        return val;
    }

    public void setReady(boolean ready) {
        try {
            processSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isReady = ready;
        processSem.release();
    }

    public void run(byte[] imageData) {
        try {
            processSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isReady = false;
        processSem.release();
        p = new ImageProcessor(speaker, this);
        p.execute(imageData);
    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (p != null) {
            p.cancel(mayInterruptIfRunning);
        }
    }
}
