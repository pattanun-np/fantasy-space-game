package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class MatchRepositoryTest {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var matchRepository: MatchRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mock(JdbcTemplate::class.java)
        matchRepository = MatchRepository(jdbcTemplate)
    }

    @Test
    fun `saveMatch should save match with flight information`() {
        // Given
        val match = createMatchWithFlight(1L, 1L, 2L)
        
        // Mock the JdbcTemplate behavior for saving a match
        `when`(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1)
        `when`(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1)
        
        // Mock the behavior for generating keys
        val statement = mock(Statement::class.java)
        val resultSet = mock(ResultSet::class.java)
        `when`(resultSet.next()).thenReturn(true)
        `when`(resultSet.getLong(1)).thenReturn(1L)
        `when`(statement.generatedKeys).thenReturn(resultSet)
        
        // When
        val savedMatch = matchRepository.saveMatch(match)
        
        // Then
        assertNotNull(savedMatch)
        assertEquals(match.id, savedMatch.id)
        assertEquals(match.challenger.id, savedMatch.challenger.id)
        assertEquals(match.opponent.id, savedMatch.opponent.id)
        
        // Verify that the flight information is preserved
        assertEquals(match.rounds.size, savedMatch.rounds.size)
        for (i in match.rounds.indices) {
            val originalRound = match.rounds[i]
            val savedRound = savedMatch.rounds[i]
            
            assertEquals(originalRound.round, savedRound.round)
            assertEquals(originalRound.characterId, savedRound.characterId)
            assertEquals(originalRound.healthDelta, savedRound.healthDelta)
            assertEquals(originalRound.staminaDelta, savedRound.staminaDelta)
            assertEquals(originalRound.manaDelta, savedRound.manaDelta)
            
            // Verify flight information
            assertNotNull(savedRound.flight)
            assertEquals(originalRound.flight?.flightType, savedRound.flight?.flightType)
            assertEquals(originalRound.flight?.distance, savedRound.flight?.distance)
            assertEquals(originalRound.flight?.duration, savedRound.flight?.duration)
            assertEquals(originalRound.flight?.success, savedRound.flight?.success)
        }
    }
    
    @Test
    fun `getMatchById should retrieve match with flight information`() {
        // Given
        val matchId = 1L
        val match = createMatchWithFlight(matchId, 1L, 2L)
        
        // Mock the JdbcTemplate behavior for retrieving a match
        `when`(jdbcTemplate.query(anyString(), any<RowMapper<Match>>(), eq(matchId))).thenAnswer { invocation ->
            val rowMapper = invocation.getArgument<RowMapper<Match>>(1)
            val resultSet = mock(ResultSet::class.java)
            
            // Mock match data
            `when`(resultSet.getLong("match_id")).thenReturn(match.id!!)
            `when`(resultSet.getLong("challenger_id")).thenReturn(match.challenger.id)
            `when`(resultSet.getString("challenger_name")).thenReturn(match.challenger.name)
            `when`(resultSet.getString("challenger_class")).thenReturn(match.challenger.characterClass)
            `when`(resultSet.getString("challenger_level")).thenReturn(match.challenger.level)
            `when`(resultSet.getInt("challenger_exp_total")).thenReturn(match.challenger.experienceTotal)
            `when`(resultSet.getInt("challenger_exp_gained")).thenReturn(match.challenger.experienceGained)
            `when`(resultSet.getBoolean("challenger_victor")).thenReturn(match.challenger.isVictor)
            
            `when`(resultSet.getLong("opponent_id")).thenReturn(match.opponent.id)
            `when`(resultSet.getString("opponent_name")).thenReturn(match.opponent.name)
            `when`(resultSet.getString("opponent_class")).thenReturn(match.opponent.characterClass)
            `when`(resultSet.getString("opponent_level")).thenReturn(match.opponent.level)
            `when`(resultSet.getInt("opponent_exp_total")).thenReturn(match.opponent.experienceTotal)
            `when`(resultSet.getInt("opponent_exp_gained")).thenReturn(match.opponent.experienceGained)
            `when`(resultSet.getBoolean("opponent_victor")).thenReturn(match.opponent.isVictor)
            
            // Mock round data
            `when`(resultSet.next()).thenReturn(true, true, false)
            
            try {
                listOf(rowMapper.mapRow(resultSet, 0))
            } catch (e: SQLException) {
                emptyList<Match>()
            }
        }
        
        // Mock the JdbcTemplate behavior for retrieving rounds
        `when`(jdbcTemplate.query(anyString(), any<RowMapper<MatchRound>>(), eq(matchId))).thenAnswer { invocation ->
            val rowMapper = invocation.getArgument<RowMapper<MatchRound>>(1)
            val resultSet = mock(ResultSet::class.java)
            
            // Mock round data for the first round
            `when`(resultSet.getInt("round_number")).thenReturn(1, 2)
            `when`(resultSet.getLong("character_id")).thenReturn(1L, 2L)
            `when`(resultSet.getInt("health_delta")).thenReturn(-15, -20)
            `when`(resultSet.getInt("stamina_delta")).thenReturn(-10, 0)
            `when`(resultSet.getInt("mana_delta")).thenReturn(0, -15)
            
            // Mock flight data
            `when`(resultSet.getString("flight_type")).thenReturn("ATTACK_FLIGHT", "POWER_FLIGHT")
            `when`(resultSet.getInt("flight_distance")).thenReturn(40, 35)
            `when`(resultSet.getInt("flight_duration")).thenReturn(3, 4)
            `when`(resultSet.getBoolean("flight_success")).thenReturn(true, true)
            
            `when`(resultSet.next()).thenReturn(true, true, false)
            
            try {
                listOf(
                    rowMapper.mapRow(resultSet, 0),
                    rowMapper.mapRow(resultSet, 1)
                )
            } catch (e: SQLException) {
                emptyList<MatchRound>()
            }
        }
        
        // When
        val retrievedMatch = matchRepository.getMatchById(matchId)
        
        // Then
        assertNotNull(retrievedMatch)
        assertEquals(match.id, retrievedMatch?.id)
        
        // Verify that the flight information is retrieved correctly
        assertNotNull(retrievedMatch?.rounds)
        assertEquals(2, retrievedMatch?.rounds?.size)
        
        val firstRound = retrievedMatch?.rounds?.get(0)
        assertNotNull(firstRound?.flight)
        assertEquals(FlightType.ATTACK_FLIGHT, firstRound?.flight?.flightType)
        assertEquals(40, firstRound?.flight?.distance)
        assertEquals(3, firstRound?.flight?.duration)
        assertTrue(firstRound?.flight?.success ?: false)
        
        val secondRound = retrievedMatch?.rounds?.get(1)
        assertNotNull(secondRound?.flight)
        assertEquals(FlightType.POWER_FLIGHT, secondRound?.flight?.flightType)
        assertEquals(35, secondRound?.flight?.distance)
        assertEquals(4, secondRound?.flight?.duration)
        assertTrue(secondRound?.flight?.success ?: false)
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