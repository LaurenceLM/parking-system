package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.DateTime;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@DisplayName("Tests Unitaires de la classe ParkingServcice - méthode getNextParkingNumberIfAvailable()")
@ExtendWith(MockitoExtension.class)
public class ParkingServiceNumberTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() throws Exception {
        DateTime dateTime = new DateTime();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateTime);;
    }

    @DisplayName("Si exception parking rempli alors elle est catchée")
    @Test
    public void getNextParkingNumberIfAvailable_catchExceptionFullParkingTest() {

        //Arrange
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        when(inputReaderUtil.readSelection()).thenReturn(2);

        //Act and Assert
        assertAll(() -> parkingService.getNextParkingNumberIfAvailable());
    }

    @DisplayName("Si illegalArgumentException alors elle est catchée")
    @Test
    public void getNextParkingNumberIfAvailable_catchIllegalArgumentExceptionTest() {

        //Arrange
        when(inputReaderUtil.readSelection()).thenReturn(3);

        //Act and Assert
        assertAll(() -> parkingService.getNextParkingNumberIfAvailable());
    }

}
