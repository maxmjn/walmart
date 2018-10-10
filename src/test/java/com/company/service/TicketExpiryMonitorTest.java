package com.company.service;

import com.company.dao.SeatHold;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class TicketExpiryMonitorTest {

  @Test
  public void testSeatHoldExpiry_ReturnsSeatsToPool() throws Exception {

    TicketService.setSeatsAvailable(new AtomicInteger(Common.SEATS_AVAILABLE));
    TicketService.setExpiryInMillisecs(Common.EXPIRY_MILLISECS);
    TicketService ticketService = TicketService.getInstance();

    SeatHold seatHold30 = ticketService.findAndHoldSeats(30, "x@x.com");
    SeatHold seatHold20 = ticketService.findAndHoldSeats(20, "x@x.com");
    Assert.assertEquals("Seat hold for 50 failed", 1, TicketService.seatHoldMap.size());

    Thread.sleep(Common.EXPIRY_MILLISECS+1000); //wait expiry + some extra time
    Assert.assertEquals("50 seats return failed", 0, TicketService.seatHoldMap.size());//expired entries removed
  }
}