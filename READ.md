### Build
- This project works with Java-8
- Maven ` mvn clean install`

### Usage
- Build project to create `ticket-service-1.0.jar`
- Import jar into your project
```
    int SEATS_AVAILABLE = 50;
    TicketService.setSeatsAvailable(SEATS_AVAILABLE);
    SeatHold seatHold5 = ticketService.findAndHoldSeats(5, "x@x.com");
    int seatHold5_Id = seatHold5.getId();
    String seatHold5_ReservationCode = ticketService.reserveSeats(seatHold5_Id, "x@x.com");
```

### Overview
- TicketExpiryMonitor
    <br> - Runnable Singleton thread
    <br> - checks expiry on seat holds and returns back to pool if expired
- TicketService
    <br> - Singleton instance implements ITicketService
    <br> - invokes TicketExpiryMonitor