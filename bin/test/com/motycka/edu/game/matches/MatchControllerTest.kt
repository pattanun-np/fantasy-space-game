package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.*
import com.motycka.edu.game.matches.rest.MatchRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MatchControllerTest {

    private lateinit var matchService: InterfaceMatchService
    private lateinit var matchController: MatchController

    @BeforeEach
    fun setUp() {
        matchService = mockk(relaxed = true)
        matchController = MatchController(matchService)
    }

    @Test
    fun `getMatches should return all matches`() {
        // Given
        val match1 = createMatch(1L, 1L, 2L)
        val match2 = createMatch(2L, 3L, 4L)
        val matches = listOf(match1, match2)
        
        every { matchService.getAllMatches() } returns matches
        
        // When
        val response = matchController.getMatches()
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(matches, response.body)
        verify { matchService.getAllMatches() }
    }
    
    @Test
    fun `createMatch should create a match with flight information`() {
        // Given
        val challengerId = 1L
        val opponentId = 2L
        val matchRequest = MatchRequest(challengerId, opponentId)
        
        val match = createMatchWithFlight(1L, challengerId, opponentId)
        
        every { matchService.createMatch(matchRequest) } returns match
        
        // When
        val response = matchController.createMatch(matchRequest)
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(match, response.body)
        
        // Verify flight information is present
        val responseMatch = response.body
        assertNotNull(responseMatch)
        assertTrue(responseMatch!!.rounds.isNotEmpty())
        assertNotNull(responseMatch.rounds[0].flight)
        assertEquals(FlightType.ATTACK_FLIGHT, responseMatch.rounds[0].flight?.flightType)
        
        verify { matchService.createMatch(matchRequest) }
    }
    
    private fun createMatch(id: Long, challengerId: Long, opponentId: Long): Match {
        return Match(
            id = id,
            challenger = MatchCharacter(
                id = challengerId,
                name = "Challenger $challengerId",
                characterClass = "WARRIOR",
                level = "LEVEL_5",
                experienceTotal = 2000,
                experienceGained = 100,
                isVictor = true
            ),
            opponent = MatchCharacter(
                id = opponentId,
                name = "Opponent $opponentId",
                characterClass = "SORCERER",
                level = "LEVEL_5",
                experienceTotal = 2000,
                experienceGained = 25,
                isVictor = false
            ),
            rounds = emptyList()
        )
    }
    
    private fun createMatchWithFlight(id: Long, challengerId: Long, opponentId: Long): Match {
        return Match(
            id = id,
            challenger = MatchCharacter(
                id = challengerId,
                name = "Challenger $challengerId",
                characterClass = "WARRIOR",
                level = "LEVEL_5",
                experienceTotal = 2000,
                experienceGained = 100,
                isVictor = true
            ),
            opponent = MatchCharacter(
                id = opponentId,
                name = "Opponent $opponentId",
                characterClass = "SORCERER",
                level = "LEVEL_5",
                experienceTotal = 2000,
                experienceGained = 25,
                isVictor = false
            ),
            rounds = listOf(
                MatchRound(
                    round = 1,
                    characterId = challengerId,
                    healthDelta = -15,
                    staminaDelta = -10,
                    manaDelta = 0,
                    flight = Flight(
                        flightType = FlightType.ATTACK_FLIGHT,
                        distance = 40,
                        duration = 3,
                        success = true
                    )
                ),
                MatchRound(
                    round = 2,
                    characterId = opponentId,
                    healthDelta = -20,
                    staminaDelta = 0,
                    manaDelta = -15,
                    flight = Flight(
                        flightType = FlightType.POWER_FLIGHT,
                        distance = 35,
                        duration = 4,
                        success = true
                    )
                )
            )
        )
    }
} 