package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DateTime;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Tests d'intégrations ParkingDataBase")
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private Date time;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static DateTime dateTime;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
        time = new Date();
    }

    @AfterAll
    private static void tearDown(){

    }

    @DisplayName("Check that a ticket is actualy saved in DB and Parking table is updated with availability")
    @Test
    public void testParkingACar(){

        //Arrange
        time.setTime( System.currentTimeMillis());
        when(dateTime.getDate()).thenReturn(time);

        //Act
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);
        parkingService.processIncomingVehicle();

        //Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket).isNotEqualTo(null);
        assertFalse(ticket.getParkingSpot().isAvailable());
    }

    @DisplayName("Check that the fare generated and out time are populated correctly in the database")
    @Test
    public void testParkingLotExit(){

        //Arrange
        time.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        when(dateTime.getDate()).thenReturn(time);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);
        parkingService.processIncomingVehicle();

        time.setTime( System.currentTimeMillis() );
        when(dateTime.getDate()).thenReturn(time);

        //Act
        ParkingService parkingService1 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);
        parkingService1.processExitingVehicle();

        //Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
        assertEquals(Math.round(time.getTime() * 100) / 100, Math.round(ticket.getOutTime().getTime() * 100) / 100);
    }


    @DisplayName("Si NullPointerException lors de la récupération heure d'entrée, alors elle est catchée")
    @Test
    public void processIncomingVehicle_catchExceptionTest() {

        //Arrange
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);
        when(dateTime.getDate()).thenThrow(new NullPointerException());

        //Act and Assert
        assertAll(() -> parkingService.processIncomingVehicle());
    }

}
