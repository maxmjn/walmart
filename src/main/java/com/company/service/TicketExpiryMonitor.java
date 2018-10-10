package com.company.service;

import com.company.dao.SeatHold;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketExpiryMonitor implements Runnable {

  private final static TicketExpiryMonitor INSTANCE = new TicketExpiryMonitor();

  private TicketExpiryMonitor() {
    // Exists only to defeat instantiation.
  }
  public static TicketExpiryMonitor getInstance(){
    return INSTANCE;
  }
  @Override
  public void run() {
    long expiryInMilliSecs = TicketService.getExpiryInMillisecs();
    while (true){
      try{
        //once app starts wait until first object is expected to expire
        Thread.sleep(expiryInMilliSecs);

        //remove expired entries from the map
        Set<Entry<String, List<SeatHold>>> entrySet = TicketService.seatHoldMap.entrySet();
        List<SeatHold> toRemove = new LinkedList<>();
        for(Entry<String, List<SeatHold>> entry : entrySet){
          List<SeatHold> list = entry.getValue();
          toRemove.clear();
          for(SeatHold seatHold: list){

            long current = System.currentTimeMillis();
            int seats = seatHold.getSeatList().size();
            long createdTS = seatHold.getCreationTS();
            if(createdTS + expiryInMilliSecs < current){
              AtomicInteger seatsAvailable = TicketService.getSeatsAvailable();
              seatsAvailable.getAndAdd(seats);
              toRemove.add(seatHold);
            }
          }

          //TicketService.seatHoldMap hold Customer -> List of seatHolds
          //After removing expired seatHolds if list is empty remove the customer
          if(toRemove.size() > 0){
            list.removeAll(toRemove);
            if(list.size() == 0){
              TicketService.seatHoldMap.remove(entry.getKey());
            }
          }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
