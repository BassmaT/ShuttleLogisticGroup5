package com.group5.shuttle.service;

// ISP: Only the approval workflow (isPartApproved, canApprove, approve).
// Progress metrics (getProgress, isTakeoverComplete, getLastActivity)
// are separated out into ITakeoverProgress.
public interface IPartApproval {
    boolean isPartApproved(String partKey);
    boolean canApprove(String partKey);
    void approve(String partKey, String chiefName);
}
