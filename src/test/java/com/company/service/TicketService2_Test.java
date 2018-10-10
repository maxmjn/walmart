package com.company.service;

import com.company.dao.SeatHold;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TicketService2_Test {

  @Before
  public void init(){

  }

  @After
  public void cleanup(){

  }

  @Test
  public void findAndHoldSeats() throws Exception{
    TicketService ticketService = TicketService.getInstance();
    TicketService.setSeatsAvailable(new AtomicInteger(Common.SEATS_AVAILABLE));
    SeatHold seatHold30 = ticketService.findAndHoldSeats(30, "x@x.com");
    SeatHold seatHold20 = ticketService.findAndHoldSeats(20, "x@x.com");
    SeatHold seatHold1 = ticketService.findAndHoldSeats(1, "y@y.com");

    List<String> list = seatHold30.getSeatList();
    Assert.assertEquals("seatHold for 30 failed", 30, list.size());

    list = seatHold20.getSeatList();
    Assert.assertEquals("seatHold for 20 failed", 20, list.size());

    Assert.assertEquals("seatHold for 1 failed", null, seatHold1);
  }

}