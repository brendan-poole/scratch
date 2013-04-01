package com.gosseyn.betfair.domain;

import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity
@Table(schema="greyhound")
public class Snapshot {
	
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    @Index(name = "snapshot_time_idx")
    private Date time_;

	@ManyToOne(fetch=FetchType.LAZY)
	private Selection selection;
	
    @Index(name = "snapshot_number_idx")
	private Integer snapshot_number;
	
    @Index(name = "price_price_idx")
	private Double price;
}
