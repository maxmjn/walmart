package com.company.service;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TicketService1_Test {

  @Before
  public void init(){

  }

  @After
  public void cleanup(){

  }

  @Test
  public void numSeatsAvailable() {
    TicketService ticketService = TicketService.getInstance();
    TicketService.setSeatsAvailable(new AtomicInteger(Common.SEATS_AVAILABLE));
    Assert.assertEquals("Failed numSeatsAvailable", Common.SEATS_AVAILABLE, ticketService.numSeatsAvailable());
  }

}