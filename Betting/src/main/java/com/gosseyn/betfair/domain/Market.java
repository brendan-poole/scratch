package com.gosseyn.betfair.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findMarketsByMarketIdEquals", "findMarketsByStartTimeGreaterThanEquals" })
@Table(schema="greyhound")
public class Market {

    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    @Index(name = "market_start_time_idx")
    private Date startTime;

    private String venue;

    @Index(name = "market_snapshot_idx")
    private Integer snapshot;
    
    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL)
    private Set<Selection> selections = new HashSet<Selection>();

    @Column(unique=true)
    private Integer marketId;

}
