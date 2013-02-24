package net.mymam.controller;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

// debug JSF life cycle
public class LifeCycleListener implements PhaseListener {

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(PhaseEvent event) {
//        System.err.println("START PHASE " + event.getPhaseId());
    }

    public void afterPhase(PhaseEvent event) {
//        System.err.println("END PHASE " + event.getPhaseId());
    }
}
