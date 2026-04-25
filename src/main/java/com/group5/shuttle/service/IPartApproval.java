package com.group5.shuttle.service;

// ISP: Nur der Freigabe-Workflow (isPartApproved, canApprove, approve).
// Fortschritts-Metriken (getProgress, isTakeoverComplete, getLastActivity)
// sind in ITakeoverProgress ausgelagert.
public interface IPartApproval {
    boolean isPartApproved(String partKey);
    boolean canApprove(String partKey);
    void approve(String partKey, String chiefName);
}
