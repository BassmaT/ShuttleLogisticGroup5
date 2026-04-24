package com.group5.shuttle;

import com.group5.shuttle.service.TakeoverState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TakeoverStateTest {

    private TakeoverState state;

    @BeforeEach
    void setUp() {
        state = TakeoverState.getInstance();
        state.reset();
    }

    // ── getProgress ───────────────────────────────────────────────────────────

    @Test
    void getProgress_initial_isZero() {
        assertEquals(0.0, state.getProgress());
    }

    @Test
    void getProgress_onePartApproved_isOneThird() {
        state.markTechnicianDone("orbiter", "Ellen");
        state.approve("orbiter", "Markus");
        assertEquals(1.0 / 3.0, state.getProgress(), 0.001);
    }

    @Test
    void getProgress_allPartsApproved_isOne() {
        for (String key : TakeoverState.PART_KEYS) {
            state.markTechnicianDone(key, "Ellen");
            state.approve(key, "Markus");
        }
        assertEquals(1.0, state.getProgress(), 0.001);
    }

    // ── isTakeoverComplete ────────────────────────────────────────────────────

    @Test
    void isTakeoverComplete_initial_isFalse() {
        assertFalse(state.isTakeoverComplete());
    }

    @Test
    void isTakeoverComplete_allApproved_isTrue() {
        for (String key : TakeoverState.PART_KEYS) {
            state.markTechnicianDone(key, "Ellen");
            state.approve(key, "Markus");
        }
        assertTrue(state.isTakeoverComplete());
    }

    // ── canApprove ────────────────────────────────────────────────────────────

    @Test
    void canApprove_beforeTechnicianDone_isFalse() {
        assertFalse(state.canApprove("orbiter"));
    }

    @Test
    void canApprove_afterTechnicianDone_isTrue() {
        state.markTechnicianDone("orbiter", "Ellen");
        assertTrue(state.canApprove("orbiter"));
    }

    @Test
    void canApprove_afterAlreadyApproved_isFalse() {
        state.markTechnicianDone("orbiter", "Ellen");
        state.approve("orbiter", "Markus");
        assertFalse(state.canApprove("orbiter"));
    }

    // ── registerWorker / getWorkerInfo ────────────────────────────────────────

    @Test
    void registerWorker_technician_storedCorrectly() {
        state.registerWorker("orbiter", "Ellen Vance", "Technician");
        assertEquals("Ellen Vance (Technician)", state.getWorkerInfo("orbiter"));
    }

    @Test
    void registerWorker_securityChief_storedCorrectly() {
        state.registerWorker("srb", "Markus Reuter", "Security Chief");
        assertEquals("Markus Reuter (Security Chief)", state.getWorkerInfo("srb"));
    }

    @Test
    void registerWorker_both_showsCombined() {
        state.registerWorker("orbiter", "Ellen Vance", "Technician");
        state.registerWorker("orbiter", "Markus Reuter", "Security Chief");
        assertEquals("Ellen Vance (Tech) / Markus Reuter (Chief)", state.getWorkerInfo("orbiter"));
    }

    @Test
    void getWorkerInfo_noRegistration_isNull() {
        assertNull(state.getWorkerInfo("externalTank"));
    }

    // ── getDisplayName ────────────────────────────────────────────────────────

    @Test
    void getDisplayName_orbiter() {
        assertEquals("Orbiter", TakeoverState.getDisplayName("orbiter"));
    }

    @Test
    void getDisplayName_srb() {
        assertEquals("SRB", TakeoverState.getDisplayName("srb"));
    }

    @Test
    void getDisplayName_externalTank() {
        assertEquals("External Tank", TakeoverState.getDisplayName("externalTank"));
    }

    @Test
    void getDisplayName_unknownKey_returnsKeyUnchanged() {
        assertEquals("unknown", TakeoverState.getDisplayName("unknown"));
    }
}
