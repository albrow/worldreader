package info.alexbrowne.eyes;

import java.io.File;

/**
 * Created by alex on 11/16/14.
 */
public class ProcessManager {
    private boolean isReady = true;
    private Speaker speaker;
    private ImageProcessor p;

    public ProcessManager(Speaker speaker) {
        this.speaker = speaker;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void run(File imageFile) {
        isReady = false;
        p = new ImageProcessor(speaker, this);
        p.execute(imageFile);
    }

    public void cancel(boolean mayInterruptIfRunning) {
        p.cancel(mayInterruptIfRunning);
    }
}
