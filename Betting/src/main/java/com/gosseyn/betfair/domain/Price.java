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
public class Price {
	
    @Index(name = "price_price_idx")
	private Double price;
	
    @Index(name = "price_back_amount_available_idx")
	private Double backAmountAvailable;
    @Index(name = "price_lay_amount_available_idx")
	private Double layAmountAvailable;
	private Double totalBSPLayLiability;
	private Double totalBSPBackersStake;

	@Transient
	private Integer selectionId;
}
