package mint.woman.female.workout.activity.ui.preview;

public enum DownloadStatus {
    DONE(0),
    NEED_DOWNLOAD(1),
    DOWNLOADING(2),
    FAIL(3);
    private final int value;

    DownloadStatus(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
