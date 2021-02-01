package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Classe des services de calcul des tarifs
 */
public class FareCalculatorService {

    private TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public FareCalculatorService() {
        this.ticketDAO = new TicketDAO();
    }

    /**
     * méthode de calcul du tarif.
     * @param ticket : Objet ticket, reduction : coefficient de réduction
     */
    public void calculateFare(Ticket ticket, double reduction) {

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        float duration = (float)(outHour - inHour) / 3600000;
        duration = (float)Math.round(duration * 100) / 100;

        if (duration <= 0.5) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * reduction);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * reduction);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    /**
     * méthode de calcul du coef de reduction.
     * @param ticket : Objet ticket
     */
    public double calculateReduction(Ticket ticket) {
        double reduction;
        if (ticketDAO.getOldTicket(ticket.getVehicleRegNumber()) != null) {
            reduction = 0.95;
        }else {
            reduction = 1;
        }
        return reduction;
    }
}