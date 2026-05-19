package com.example.liftlog.ui.history;

public class HistoryItem {
    public final int sessionId;
    public final String dateIso;
    public final int durationMinutes;
    public final float volumeKg;

    public HistoryItem(int sessionId, String dateIso, int durationMinutes, float volumeKg) {
        this.sessionId = sessionId;
        this.dateIso = dateIso;
        this.durationMinutes = durationMinutes;
        this.volumeKg = volumeKg;
    }
}
