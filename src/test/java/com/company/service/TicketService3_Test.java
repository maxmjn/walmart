package com.company.service;

import com.company.dao.SeatHold;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TicketService3_Test {

  @Before
  public void init(){

  }

  @After
  public void cleanup(){

  }

  @Test
  public void reserveSeats() {

    TicketService ticketService = TicketService.getInstance();
    TicketService.setSeatsAvailable(new AtomicInteger(Common.SEATS_AVAILABLE));

    SeatHold seatHold5 = ticketService.findAndHoldSeats(5, "x@x.com");
    int seatHold5_Id = seatHold5.getId();
    String seatHold5_ReservationCode = ticketService.reserveSeats(seatHold5_Id, "x@x.com");
    String expected = "Seat-50/Seat-49/Seat-48/Seat-47/Seat-46";
    Assert.assertEquals("reserveSeats for 5 failed", expected, seatHold5_ReservationCode);
  }

  @Test
  public void reserveSeats_Expired() throws Exception{
    TicketService.setExpiryInMillisecs(Common.EXPIRY_MILLISECS);
    TicketService ticketService = TicketService.getInstance();
    TicketService.setSeatsAvailable(new AtomicInteger(Common.SEATS_AVAILABLE));
    
    SeatHold seatHold5 = ticketService.findAndHoldSeats(5, "x@x.com");
    Thread.sleep(Common.EXPIRY_MILLISECS + 1000); //extra 1000 for execution of com.company.service.TicketExpiryMonitor.run
    int seatHold5_Id = seatHold5.getId();
    String seatHold5_ReservationCode = ticketService.reserveSeats(seatHold5_Id, "x@x.com");
    System.out.println(seatHold5_ReservationCode);
    String expected = "No SeatHolds available! for customer:x@x.com";
    Assert.assertEquals("reserveSeats expired for 5 failed", expected, seatHold5_ReservationCode);
  }
}