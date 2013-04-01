package com.gosseyn.betfair.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findSelectionsBySelectionIdEquals" })
@Table(schema="greyhound")

public class Selection {
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Market market;
	
    @Index(name = "selection_selection_id_idx")
	private Integer selectionId;
	
    @Index(name = "selection_name_idx")
	private String name;
	
    @OneToMany(mappedBy = "selection", cascade = CascadeType.ALL)
    private Set<Snapshot> snapshots = new HashSet<Snapshot>();
}
