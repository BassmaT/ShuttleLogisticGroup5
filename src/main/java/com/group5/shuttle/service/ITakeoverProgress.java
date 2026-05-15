package com.group5.shuttle.service;

// ISP: progress and activity data of the takeover workflow.
// Separated from IPartApproval, since dashboard clients (MainController) only need these
// metrics and never trigger the approval actions.
public interface ITakeoverProgress {
    double getProgress();
    boolean isTakeoverComplete();
    String getLastActivity();
}
