package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DateTime;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests Unitaires de la classe ParkingServcice - méthode processExitingVehicle()")
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            DateTime dateTime = new DateTime();
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    @DisplayName("Processus de sortie normal - appel de la mise à jour Parking 1 fois ")
    public void processExitingVehicleTest() {
        //Arrange
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //Act
        parkingService.processExitingVehicle();

        //Assert
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Processus de sortie - Si ticket non mis à jour alors parking non mis à jour")
    public void processExitingVehicle_notUpdateTicketTest(){
        //Arrange
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        //Act
        parkingService.processExitingVehicle();

        //Assert
        verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("Processus de sortie - Si exception alors elle est catchée et ticket non mis à jour")
    public void processExitingVehicle_catchExceptionTest() {

        //Arrange
        when(ticketDAO.getTicket(anyString())).thenReturn(null);

        //Act and Assert
        assertAll(() -> parkingService.processExitingVehicle());
        verify(ticketDAO, Mockito.never()).updateTicket(any(Ticket.class));
    }
}
