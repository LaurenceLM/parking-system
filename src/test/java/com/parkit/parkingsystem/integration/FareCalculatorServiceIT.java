package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tests d'intégrations calculateReduction")
public class FareCalculatorServiceIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;
    private static TicketDAO ticketDAO;
    private static ParkingSpot parkingSpotCar;
    private Ticket ticketPrecedent;
    private Date inTime;
    private Date outTime;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, false);
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        inTime = new Date();
        outTime = new Date();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @DisplayName("Soit sortie véhicule client fidèle, on obtient coef de réduction égal à 0.95")
    @Test
    public void calculateReductionIt(){
        //Arrange
        //création ticket précédent avec le même numéro d'immatriculation
        ticketPrecedent = new Ticket();
        Date inTimePrecedent = new Date();
        inTimePrecedent.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );
        Date outTimePrecedent = new Date();
        outTimePrecedent.setTime( System.currentTimeMillis() - (  23 * 60 * 60 * 1000) );
        ticketPrecedent.setInTime(inTimePrecedent);
        ticketPrecedent.setOutTime(outTimePrecedent);
        ticketPrecedent.setParkingSpot(parkingSpotCar);
        ticketPrecedent.setPrice(Fare.CAR_RATE_PER_HOUR);
        ticketPrecedent.setVehicleRegNumber("AA123BB");
        ticketDAO.saveTicket(ticketPrecedent);

        inTime.setTime( System.currentTimeMillis() - ( 60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotCar);
        ticket.setVehicleRegNumber("AA123BB");

        //Act
        FareCalculatorService fareCalculatorService = new FareCalculatorService(ticketDAO);
        double reduction = fareCalculatorService.calculateReduction(ticket);

        //Assert
        assertEquals(reduction, 0.95);
    }

}
