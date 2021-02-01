package com.parkit.parkingsystem;

import static org.mockito.Mockito.*;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

@Tag("FareCalculatorServiceTest")
@DisplayName("Tests Unitaires de la classe FareCalculatorService - méthode calculateFare()")
@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private static ParkingSpot parkingSpotCar;
    private static ParkingSpot parkingSpotBike;
    private static ParkingSpot parkingSpotNull;
    private Ticket ticket;
    private Date inTime;
    private Date outTime;
    private double reduction;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
        parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, false);
        parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE, false);
        parkingSpotNull = new ParkingSpot(1, null, false);
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        inTime = new Date();
        outTime = new Date();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
    }

    @Mock
    ParkingSpot parkingSpotMock;

    @DisplayName("Soit entrée voiture une heure avant, on obtient tarif égal au tarif horaire voiture")
    @Test
    public void calculateFareCar(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotCar);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @DisplayName("Soit entrée vélo une heure avant, on obtient tarif égal au tarif horaire vélo")
    @Test
    public void calculateFareBike(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotBike);
        reduction = 1;


        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @DisplayName("Soit entrée véhicule de type null une heure avant, NullPointerException levée et renvoyée")
    @Test
    public void calculateFareNullType(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotNull);
        reduction = 1;

        //Act and Assert
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, reduction));
    }

    @DisplayName("Soit entrée vélo future : dans une heure, IllegalArgumentException levée et renvoyée avec le message")
    @Test
    public void calculateFareBikeWithFutureInTime(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotBike);
        reduction = 1;

        //Act and Assert
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, reduction));
        assertEquals(exception.getMessage(), "Out time provided is incorrect:" + outTime);
    }

    @DisplayName("Soit entrée vélo moins d'une heure avant : 45 minutes, on obtient tarif égal à 0.75 * le tarif horaire vélo")
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        ticket.setParkingSpot(parkingSpotBike);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @DisplayName("Soit entrée voiture moins d'une heure avant : 45 minutes, on obtient tarif égal à 0.75 * le tarif horaire voiture")
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        ticket.setParkingSpot(parkingSpotCar);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @DisplayName("Soit entrée voiture le jour précédent : 24 heures avant, on obtient tarif égal à 24 * le tarif horaire voiture")
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        ticket.setParkingSpot(parkingSpotCar);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @DisplayName("Soit entrée véhicule de type inconnu, IllegalArgumentException levée et renvoyée avec le bon message")
    @Test
    public void calculateFareUnkownType(){
        //Arrange
        when(parkingSpotMock.getParkingType()).thenReturn(ParkingType.valueOf("SCOOTER"));
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotMock);
        reduction = 1;

        //Act and Assert
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, reduction));
        assertEquals(exception.getMessage(), "Unkown Parking Type");
    }

    @DisplayName("Soit entrée voiture 30 minutes avant, on obtient tarif égal à 0")
    @Test
    public void calculateFare30minFreeCar(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - (  30 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotCar);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals(ticket.getPrice(), 0);
    }

    @DisplayName("Soit entrée vélo 15 minutes avant, on obtient tarif égal à 0")
    @Test
    public void calculateFare15minFreeBike(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - ( 15 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotBike);
        reduction = 1;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals(ticket.getPrice(), 0);
    }

    @DisplayName("Soit entrée voiture avec coef réduction à 0.95 une heure avant, on obtient tarif à 0.95 * le tarif horaire voiture")
    @Test
    public void calculateFare5PourcentReducCar(){
        //Arrange
        inTime.setTime( System.currentTimeMillis() - ( 60 * 60 * 1000) );
        ticket.setParkingSpot(parkingSpotCar);
        ticket.setVehicleRegNumber("AA123BB");
        double reduction = 0.95;

        //Act
        fareCalculatorService.calculateFare(ticket, reduction);

        //Assert
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR *0.95);
    }

}
