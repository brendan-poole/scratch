//select name,start_time,selection_id,a.snapshot,sum(back_amount_available) from price a join market b on a.market=b.id group by name,start_time,selection_id,a.snapshot order by selection_id,a.snapshot;

package com.gosseyn.betfair;

import generated.exchange.BFExchangeServiceStub.Runner;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import com.gosseyn.betfair.domain.Market;
import com.gosseyn.betfair.domain.Price;
import com.gosseyn.betfair.domain.Selection;

import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.InflatedCompleteMarketPrices;
import demo.util.InflatedCompleteMarketPrices.InflatedCompletePrice;
import demo.util.InflatedCompleteMarketPrices.InflatedCompleteRunner;


public class BetfairService {
	protected final Log logger = LogFactory.getLog(getClass());
	
	// The session token
	private static APIContext apiContext = new APIContext();

	@PersistenceContext
	transient EntityManager em;
	
	private String username;
	private String password;
	private Long delay;
	private Integer sampleWindowMins;
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "./META-INF/spring/applicationContext.xml" });
		// of course, an ApplicationContext is just a BeanFactory
		BeanFactory factory = (BeanFactory) appContext;
		/*
		EntityManagerFactory emf = (EntityManagerFactory) factory
				.getBean("entityManagerFactory");
		EntityManager entityManager;
		entityManager = emf.createEntityManager();
		*/
		BetfairService betfairService = (BetfairService) factory.getBean("betfairService");
		
		betfairService.login();
		betfairService.run();
	}
	
	public void run() throws InterruptedException {
		logger.info("Running - delay="+delay+"ms window="+sampleWindowMins+"mins");
		while(true) {
			try {
				update();
			} catch (Exception e) {
				logger.error(e);
			}
			Thread.sleep(delay);
		}
	}
	
	@Transactional
	public void update() throws BetfairApiException {
		logger.info("Started");
		MarketSummary[] marketSummary = findMarketSummaryByEventTypeName("Greyhound - Todays Card");
		
		// Run the following once each day
		for(MarketSummary ms : marketSummary) {
			if(ms.getMarketName().contains("(FC)") || 
					ms.getMarketName().contains("(Place)")|| 
					ms.getMarketName().contains("(AUS)")) {
				logger.debug("Skipping (fc/place/aus): "+ms.getMarketName()+" "+ms.getStartTime().getTime());
						continue;
			}
			
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MINUTE, sampleWindowMins);
			if(ms.getStartTime().after(c)) {
				logger.debug("Skipping - wrong time: "+ms.getMarketName()+" "+ms.getStartTime().getTime());
				continue;
			}
					
			Market m = null;
			try {
				m = Market.findMarketsByMarketIdEquals(ms.getMarketId()).getSingleResult();
				logger.info("Existing market: "+ms.getMarketId()+" "+ms.getMarketName()+" "+ms.getStartTime().getTime());
			} catch (EmptyResultDataAccessException e) {
				logger.info("New market: "+ms.getMarketId()+" "+ms.getMarketName()+" "+ms.getStartTime().getTime());
				m = new Market();				
				m.setMarketId(ms.getMarketId());
				m.setName(ms.getMarketName());
				m.setStartTime(ms.getStartTime().getTime());
				m.setVenue(ms.getVenue());
				m.setSnapshot(1);
				m.setSelections(new HashSet<Selection>());
				m.persist();
				m.flush();
			}
			Set<Price> prices = null;
			try {
				prices = findPricesByMarket(ms,m);
				for(Price p : prices) {
					Selection s = null;
					try {
						s = Selection.findSelectionsBySelectionIdEquals(p.getSelectionId()).getSingleResult();
					} catch (EmptyResultDataAccessException e) {
						s = new Selection();
						/* Querying name caused it to exceed 'THROTTLE'
						generated.exchange.BFExchangeServiceStub.Market bfm = findMarket(m.getMarketId().intValue());
						for (Runner mr: bfm.getRunners().getRunner()) {
							if (mr.getSelectionId() == p.getSelectionId()) {
								s.setName(mr.getName());
								break;
							}
						}
						
						if(s.getName().isEmpty()) {
							throw new BetfairApiException("Unable to find name of selection");
						}
						*/
						s.setMarket(m);
						s.setSelectionId(p.getSelectionId());
						s.persist();
						s.flush();
					}
					p.setSelection(s);
					p.persist();
				}
				m.setSnapshot(m.getSnapshot()+1);
				m.merge();
			} catch (EventClosedException e) {
			} catch (BetfairApiException e) {
				logger.error("Only partial update was possible."+e.getMessage());				
				break;
			}
		}
		logger.info("Ended");
	}

	public generated.exchange.BFExchangeServiceStub.Market findMarket(int marketId) throws BetfairApiException {
		try {
			return ExchangeAPI.getMarket(Exchange.UK, apiContext,marketId);
		} catch (Exception e) {
			throw new BetfairApiException(e.getMessage()); 
		}

	}
	public Set<Price> findPricesByMarket(MarketSummary marketSummary, Market market) throws EventClosedException, BetfairApiException {
		InflatedCompleteMarketPrices marketPrices = null;
		Date date = new Date();
		try {
			marketPrices = ExchangeAPI.getCompleteMarketPrices(Exchange.UK,
					apiContext,marketSummary.getMarketId());
		} catch (Exception e) {
			if(e.getMessage().contains("EVENT_SUSPENDED")) {
				logger.info("Event suspended: "+market.getMarketId()+" "+market.getName()+" "+market.getStartTime());				
				throw new EventClosedException();
			} else if(e.getMessage().contains("EVENT_CLOSED")) {
				logger.info("Event closed: "+market.getMarketId()+" "+market.getName()+" "+market.getStartTime());				
				throw new EventClosedException();
			} else {
				logger.error(e);
				throw new BetfairApiException(e.getMessage()); 
			}
		}

		Set<Price> prices = new HashSet<Price>();
		StringBuffer selections = new StringBuffer();
		for(InflatedCompleteRunner r : marketPrices.getRunners()) {
			selections.append(r.getSelectionId()).append(' ');
			
			for(InflatedCompletePrice p : r.getPrices()) {
				Price price = new Price();
				price.setBackAmountAvailable(p.getBackAmountAvailable());
				price.setLayAmountAvailable(p.getLayAmountAvailable());
				price.setPrice(p.getPrice());
				price.setTotalBSPBackersStake(p.getTotalBSPBackersStake());
				price.setTotalBSPLayLiability(p.getTotalBSPBackersStake());
				price.setSelectionId(r.getSelectionId());
				price.setTime_(date);
				price.setSnapshot(market.getSnapshot());
				prices.add(price);

			}
		}
		logger.info("Prices read for: "+selections.toString()+" count: "+prices.size());
		return prices;
	}

	public MarketSummary[] findMarketSummaryByEventTypeName(String name) throws BetfairApiException {
		try {
			EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
			for(EventType t : types) {
				if(t.getName().equals(name)) {
					GetEventsResp resp = GlobalAPI.getEvents(apiContext, t.getId());
					return resp.getMarketItems().getMarketSummary();
				}
			}
			throw new BetfairApiException("No market with given name: "+name);
		} catch (Exception e) {
			throw new BetfairApiException(e.getMessage());
		}
	}
	
	public void login() throws BetfairApiException {
		
		try {
			GlobalAPI.login(apiContext, username, password);
			logger.info("Log in successful: "+username);
		} catch (Exception e) {
			throw new BetfairApiException(e.getMessage());
		}		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Integer getSampleWindowMins() {
		return sampleWindowMins;
	}

	public void setSampleWindowMins(Integer sampleWindowMins) {
		this.sampleWindowMins = sampleWindowMins;
	}

	
}
