package com.company.service;

import com.company.dao.SeatHold;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class TicketService implements ITicketService {

  private static long EXPIRY_IN_MILLISECS = 180000L;

  private static AtomicInteger seatsAvailable = new AtomicInteger(0);

  public static ConcurrentMap<String, List<SeatHold>> seatHoldMap = new ConcurrentHashMap<>();

  private final Semaphore semaphore = new Semaphore(1, true);

  private final static TicketService INSTANCE = new TicketService();
  private TicketService() {
    // Exists only to defeat instantiation.
  }
  public static TicketService getInstance(){
    Thread ticketExpiryMonitorThread = new Thread(TicketExpiryMonitor.getInstance());
    ticketExpiryMonitorThread.setName("TicketExpiryMonitor");
    ticketExpiryMonitorThread.start();

    return INSTANCE;
  }

  @Override
  public int numSeatsAvailable() {
    return seatsAvailable.get();
  }

  /**
   * Check availabilty and hold seats.
   *
   * @param numSeats the number of seats to find and hold
   * @param customerEmail unique identifier for the customer
   * @return
   */
  @Override
  public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
    int seats = seatsAvailable.get();

    if(seats < numSeats)
      return null; //could throw Exception but requires change to interface

    //synchronized block using Semaphore
    try{
      if(semaphore.tryAcquire(1, TimeUnit.SECONDS)){
        int expect = seats;
        int update = seats - numSeats;
        if(!seatsAvailable.compareAndSet(expect, update))
          throw new Exception();

        SeatHold seatHold = new SeatHold();

        if(seatHoldMap.containsKey(customerEmail)){
          List<SeatHold> list = seatHoldMap.get(customerEmail);
          int size = list.size();
          seatHold.setCreationTS(System.currentTimeMillis());
          seatHold.setId(++size);
          List<String> holdInfo = new LinkedList<>();
          for (int i = expect; i > update; i--) {
            holdInfo.add("Seat-" + i);
          }
          seatHold.setSeatList(holdInfo);
          list.add(seatHold);

        } else{
          List<SeatHold> list = new LinkedList<>();
          seatHold.setCreationTS(System.currentTimeMillis());
          seatHold.setId(1);
          List<String> holdInfo = new LinkedList<>();
          for (int i = expect; i > update; i--) {
            holdInfo.add("Seat-" + i);
          }
          seatHold.setSeatList(holdInfo);

          list.add(seatHold);
          seatHoldMap.put(customerEmail, list);
        }

        semaphore.release();
        return seatHold;
      }
    }catch (Exception e){
      if(semaphore.availablePermits() > 0){
        semaphore.release();
      }
    }

    return null;
  }

  /**
   * If seatHoldId is found check if not expired and return.
   *
   * @param seatHoldId the seat hold identifier
   * @param customerEmail the email address of the customer to which the
  seat hold is assigned
   * @return
   */
  @Override
  public String reserveSeats(int seatHoldId, String customerEmail) {
    if(seatHoldMap.containsKey(customerEmail)){
      List<SeatHold> list = seatHoldMap.get(customerEmail);
      list = list.stream()
                  .filter(s -> s.getId()==seatHoldId)
                  .collect(Collectors.toList());
      if(list==null || list.size()==0){
        return null;
      }
      SeatHold seatHold = list.get(0);
      List<String> holdInfo = seatHold.getSeatList();
      long created = seatHold.getCreationTS();
      String reservationCode = StringUtils.join(holdInfo, "/");
      long current = System.currentTimeMillis();
      if(created + EXPIRY_IN_MILLISECS >= current){
        return reservationCode;
      }

      //return seats to pool
      seatsAvailable.getAndAdd(holdInfo.size());
      return "Customer:" + customerEmail + " Hold:" + seatHold.getSeatList()
          + " Expired(createdTS:" + created + "+" + EXPIRY_IN_MILLISECS + ")" + " less than currentTS:" + current
          + " Seats returned to the pool=" + holdInfo.size()
      ;
    }
    return "No SeatHolds available! for customer:" + customerEmail;
  }

  public static long getExpiryInMillisecs() {
    return EXPIRY_IN_MILLISECS;
  }

  public static void setExpiryInMillisecs(long expiryInMillisecs) {
    EXPIRY_IN_MILLISECS = expiryInMillisecs;
  }

  public static AtomicInteger getSeatsAvailable() {
    return seatsAvailable;
  }

  public static void setSeatsAvailable(AtomicInteger seatsAvailable) {
    TicketService.seatsAvailable = seatsAvailable;
  }
}
