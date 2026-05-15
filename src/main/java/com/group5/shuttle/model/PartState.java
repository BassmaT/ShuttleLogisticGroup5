package com.group5.shuttle.model;

import java.util.ArrayList;
import java.util.List;

// Encapsulates the entire state of a shuttle part (Orbiter, SRB, External Tank).
// Used by TakeoverState as a unified data structure per part.
public class PartState {

    private boolean          securityApproved  = false;
    private List<RepairTask> repairs           = new ArrayList<>();
    private String           technicianName    = null;
    private String           securityChiefName = null;
    private boolean          technicianDone    = false;

    public boolean isSecurityApproved()             { return securityApproved; }
    public void    setSecurityApproved(boolean v)   { securityApproved = v; }

    public List<RepairTask> getRepairs()             { return repairs; }

    public String getTechnicianName()               { return technicianName; }
    public void   setTechnicianName(String name)    { technicianName = name; }

    public String getSecurityChiefName()            { return securityChiefName; }
    public void   setSecurityChiefName(String name) { securityChiefName = name; }

    public boolean isTechnicianDone()               { return technicianDone; }
    public void    setTechnicianDone(boolean v)     { technicianDone = v; }

    public void reset() {
        securityApproved  = false;
        repairs.clear();
        technicianName    = null;
        securityChiefName = null;
        technicianDone    = false;
    }
}
