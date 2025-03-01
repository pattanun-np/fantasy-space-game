package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.characters.CharacterRepository
import com.motycka.edu.game.characters.model.Character
import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.Warrior
import com.motycka.edu.game.leaderboard.model.LeaderboardEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

class LeaderboardServiceTest {

    private lateinit var leaderboardRepository: LeaderboardRepository
    private lateinit var leaderboardService: LeaderboardService
    private lateinit var characterRepository: CharacterRepository
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        characterRepository = mockk(relaxed = true)
        leaderboardRepository = mockk(relaxed = true)
        leaderboardService = LeaderboardService(leaderboardRepository)
    }

    @Test
    fun `getLeaderboard should return leaderboard entries`() {
        // Given
        val warrior1 = createWarrior(1L, "Warrior1")
        val warrior2 = createWarrior(2L, "Warrior2")
        
        val entries = listOf(
            LeaderboardEntry(1, warrior1, 10, 2, 1),
            LeaderboardEntry(2, warrior2, 5, 5, 2)
        )
        
        every { leaderboardRepository.getLeaderboard(any()) } returns entries
        
        // When
        val result = leaderboardService.getLeaderboard()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("Warrior1", result[0].character.name)
        assertEquals(10, result[0].wins)
        assertEquals("Warrior2", result[1].character.name)
        assertEquals(5, result[1].wins)
        
        verify { leaderboardRepository.getLeaderboard(null) }
    }
    
    @Test
    fun `getLeaderboard with class filter should filter by character class`() {
        // Given
        val warrior = createWarrior(1L, "Warrior1")
        
        val entries = listOf(
            LeaderboardEntry(1, warrior, 10, 2, 1)
        )
        
        every { leaderboardRepository.getLeaderboard("WARRIOR") } returns entries
        
        // When
        val result = leaderboardService.getLeaderboard("WARRIOR")
        
        // Then
        assertEquals(1, result.size)
        assertEquals("Warrior1", result[0].character.name)
        assertEquals("WARRIOR", result[0].character.characterClass)
        
        verify { leaderboardRepository.getLeaderboard("WARRIOR") }
    }
    
    @Test
    fun `updateLeaderboardForMatch should update leaderboard for challenger win`() {
        // Given
        val challengerId = 1L
        val opponentId = 2L
        val challengerWon = true
        
        // When
        leaderboardService.updateLeaderboardForMatch(challengerId, opponentId, challengerWon)
        
        // Then
        verify { leaderboardRepository.updateLeaderboard(challengerId, true, false, false) }
        verify { leaderboardRepository.updateLeaderboard(opponentId, false, true, false) }
    }
    
    @Test
    fun `updateLeaderboardForMatch should update leaderboard for opponent win`() {
        // Given
        val challengerId = 1L
        val opponentId = 2L
        val challengerWon = false
        
        // When
        leaderboardService.updateLeaderboardForMatch(challengerId, opponentId, challengerWon)
        
        // Then
        verify { leaderboardRepository.updateLeaderboard(challengerId, false, true, false) }
        verify { leaderboardRepository.updateLeaderboard(opponentId, true, false, false) }
    }
    
    private fun createWarrior(id: Long, name: String): Character {
        return Warrior(
            id = id,
            accountId = 1L,
            name = name,
            health = 100,
            attackPower = 50,
            stamina = 30,
            defensePower = 20,
            level = CharacterLevel.LEVEL_5,
            experience = 2000
        )
    }
} 