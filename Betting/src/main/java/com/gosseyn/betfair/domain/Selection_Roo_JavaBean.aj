// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.gosseyn.betfair.domain;

import com.gosseyn.betfair.domain.Market;
import com.gosseyn.betfair.domain.Snapshot;
import java.lang.Integer;
import java.lang.String;
import java.util.Set;

privileged aspect Selection_Roo_JavaBean {
    
    public Market Selection.getMarket() {
        return this.market;
    }
    
    public void Selection.setMarket(Market market) {
        this.market = market;
    }
    
    public Integer Selection.getSelectionId() {
        return this.selectionId;
    }
    
    public void Selection.setSelectionId(Integer selectionId) {
        this.selectionId = selectionId;
    }
    
    public String Selection.getName() {
        return this.name;
    }
    
    public void Selection.setName(String name) {
        this.name = name;
    }
    
    public Set<Snapshot> Selection.getSnapshots() {
        return this.snapshots;
    }
    
    public void Selection.setSnapshots(Set<Snapshot> snapshots) {
        this.snapshots = snapshots;
    }
    
}
