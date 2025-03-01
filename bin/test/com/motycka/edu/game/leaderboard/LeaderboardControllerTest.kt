package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.Warrior
import com.motycka.edu.game.leaderboard.model.LeaderboardEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class LeaderboardControllerTest {

    private lateinit var leaderboardService: InterfaceLeaderboardService
    private lateinit var leaderboardController: LeaderboardController

    @BeforeEach
    fun setUp() {
        leaderboardService = mockk(relaxed = true)
        leaderboardController = LeaderboardController(leaderboardService)
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
        
        every { leaderboardService.getLeaderboard(any()) } returns entries
        
        // When
        val response = leaderboardController.getLeaderboard(null)
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        assertEquals("Warrior1", response.body?.get(0)?.character?.name)
        assertEquals(10, response.body?.get(0)?.wins)
        
        verify { leaderboardService.getLeaderboard(null) }
    }
    
    @Test
    fun `getLeaderboard with class filter should filter by character class`() {
        // Given
        val warrior = createWarrior(1L, "Warrior1")
        
        val entries = listOf(
            LeaderboardEntry(1, warrior, 10, 2, 1)
        )
        
        every { leaderboardService.getLeaderboard("WARRIOR") } returns entries
        
        // When
        val response = leaderboardController.getLeaderboard("WARRIOR")
        
        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertEquals("Warrior1", response.body?.get(0)?.character?.name)
        assertEquals("WARRIOR", response.body?.get(0)?.character?.characterClass)
        
        verify { leaderboardService.getLeaderboard("WARRIOR") }
    }
    
    @Test
    fun `endpoint should be correctly mapped to api-leaderboards`() {
        // Given
        val mockMvc = MockMvcBuilders.standaloneSetup(leaderboardController).build()
        val entries = listOf(
            LeaderboardEntry(1, createWarrior(1L, "Warrior1"), 10, 2, 1)
        )
        
        every { leaderboardService.getLeaderboard(any()) } returns entries
        
        // When/Then
        mockMvc.perform(get("/api/leaderboards"))
            .andExpect(status().isOk)
        
        verify { leaderboardService.getLeaderboard(null) }
    }
    
    @Test
    fun `endpoint should accept class filter parameter`() {
        // Given
        val mockMvc = MockMvcBuilders.standaloneSetup(leaderboardController).build()
        val entries = listOf(
            LeaderboardEntry(1, createWarrior(1L, "Warrior1"), 10, 2, 1)
        )
        
        every { leaderboardService.getLeaderboard("WARRIOR") } returns entries
        
        // When/Then
        mockMvc.perform(get("/api/leaderboards?characterClass=WARRIOR"))
            .andExpect(status().isOk)
        
        verify { leaderboardService.getLeaderboard("WARRIOR") }
    }
    
    private fun createWarrior(id: Long, name: String): Warrior {
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