package com.motycka.edu.game.matches.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FlightTest {

    @Test
    fun `Flight constructor should create a valid Flight object`() {
        // Given
        val flightType = FlightType.ATTACK_FLIGHT
        val distance = 40
        val duration = 3
        val success = true
        
        // When
        val flight = Flight(
            flightType = flightType,
            distance = distance,
            duration = duration,
            success = success
        )
        
        // Then
        assertEquals(flightType, flight.flightType)
        assertEquals(distance, flight.distance)
        assertEquals(duration, flight.duration)
        assertEquals(success, flight.success)
    }
    
    @Test
    fun `Flight should be comparable with equals`() {
        // Given
        val flight1 = Flight(
            flightType = FlightType.ATTACK_FLIGHT,
            distance = 40,
            duration = 3,
            success = true
        )
        
        val flight2 = Flight(
            flightType = FlightType.ATTACK_FLIGHT,
            distance = 40,
            duration = 3,
            success = true
        )
        
        val flight3 = Flight(
            flightType = FlightType.POWER_FLIGHT,
            distance = 35,
            duration = 4,
            success = true
        )
        
        // Then
        assertEquals(flight1, flight2)
        assertNotEquals(flight1, flight3)
    }
    
    @Test
    fun `FlightType enum should have all required flight types`() {
        // Then
        assertEquals(5, FlightType.values().size)
        assertTrue(FlightType.values().contains(FlightType.ATTACK_FLIGHT))
        assertTrue(FlightType.values().contains(FlightType.DEFENSIVE_FLIGHT))
        assertTrue(FlightType.values().contains(FlightType.EVASIVE_FLIGHT))
        assertTrue(FlightType.values().contains(FlightType.HEALING_FLIGHT))
        assertTrue(FlightType.values().contains(FlightType.POWER_FLIGHT))
    }
} 