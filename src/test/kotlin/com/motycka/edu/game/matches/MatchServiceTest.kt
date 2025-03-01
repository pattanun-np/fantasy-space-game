package com.motycka.edu.game.matches

import com.motycka.edu.game.account.InterfaceAccountService
import com.motycka.edu.game.characters.InterfaceCharacterService
import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.Sorcerer
import com.motycka.edu.game.characters.model.Warrior
import com.motycka.edu.game.leaderboard.InterfaceLeaderboardService
import com.motycka.edu.game.matches.model.*
import com.motycka.edu.game.matches.rest.MatchRequest
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MatchServiceTest {

    private lateinit var matchService: MatchService
    private lateinit var characterService: InterfaceCharacterService
    private lateinit var accountService: InterfaceAccountService
    private lateinit var leaderboardService: InterfaceLeaderboardService
    private lateinit var matchRepository: MatchRepository

    @BeforeEach
    fun setUp() {
        characterService = mockk(relaxed = true)
        accountService = mockk(relaxed = true)
        leaderboardService = mockk(relaxed = true)
        matchRepository = mockk(relaxed = true)
        
        matchService = MatchService(
            characterService = characterService,
            accountService = accountService,
            leaderboardService = leaderboardService,
            matchRepository = matchRepository
        )
    }

    @Test
    fun `createMatch should create a match with the correct characters`() {
        // Given
        val challengerId = 1L
        val opponentId = 2L
        val matchRequest = MatchRequest(challengerId, opponentId)
        
        val challenger = createWarrior(challengerId, "Warrior")
        val opponent = createSorcerer(opponentId, "Sorcerer")
        
        every { characterService.getCharacterById(challengerId) } returns challenger
        every { characterService.getCharacterById(opponentId) } returns opponent
        
        val savedMatch = Match(
            id = 1L,
            challenger = MatchCharacter(
                id = challengerId,
                name = challenger.name,
                characterClass = challenger.characterClass,
                level = challenger.level.toString(),
                experienceTotal = challenger.experience ?: 0,
                experienceGained = 0,
                isVictor = false
            ),
            opponent = MatchCharacter(
                id = opponentId,
                name = opponent.name,
                characterClass = opponent.characterClass,
                level = opponent.level.toString(),
                experienceTotal = opponent.experience ?: 0,
                experienceGained = 0,
                isVictor = false
            ),
            rounds = emptyList()
        )
        
        every { matchRepository.saveMatch(any()) } returns savedMatch
        every { matchRepository.getMatchById(1L) } returns savedMatch
        every { matchRepository.updateMatch(any()) } returns savedMatch.copy(
            rounds = listOf(
                MatchRound(
                    round = 1,
                    characterId = challengerId,
                    healthDelta = -10,
                    staminaDelta = -5,
                    manaDelta = 0,
                    flight = Flight(
                        flightType = FlightType.ATTACK_FLIGHT,
                        distance = 40,
                        duration = 3,
                        success = true
                    )
                )
            )
        )
        
        // When
        val result = matchService.createMatch(matchRequest)
        
        // Then
        assertNotNull(result)
        assertEquals(challengerId, result.challenger.id)
        assertEquals(opponentId, result.opponent.id)
        
        verify { characterService.getCharacterById(challengerId) }
        verify { characterService.getCharacterById(opponentId) }
        verify { matchRepository.saveMatch(any()) }
        verify { matchRepository.getMatchById(1L) }
        verify { matchRepository.updateMatch(any()) }
    }
    
    @Test
    fun `processMatch should include flight information in rounds`() {
        // Given
        val matchId = 1L
        val challengerId = 1L
        val opponentId = 2L
        
        val challenger = createWarrior(challengerId, "Warrior")
        val opponent = createSorcerer(opponentId, "Sorcerer")
        
        val match = Match(
            id = matchId,
            challenger = MatchCharacter(
                id = challengerId,
                name = challenger.name,
                characterClass = challenger.characterClass,
                level = challenger.level.toString(),
                experienceTotal = challenger.experience ?: 0,
                experienceGained = 0,
                isVictor = false
            ),
            opponent = MatchCharacter(
                id = opponentId,
                name = opponent.name,
                characterClass = opponent.characterClass,
                level = opponent.level.toString(),
                experienceTotal = opponent.experience ?: 0,
                experienceGained = 0,
                isVictor = false
            ),
            rounds = emptyList()
        )
        
        every { characterService.getCharacterById(challengerId) } returns challenger
        every { characterService.getCharacterById(opponentId) } returns opponent
        every { matchRepository.getMatchById(matchId) } returns match
        
        val updatedMatch = match.copy(
            rounds = listOf(
                MatchRound(
                    round = 1,
                    characterId = challengerId,
                    healthDelta = -10,
                    staminaDelta = -5,
                    manaDelta = 0,
                    flight = Flight(
                        flightType = FlightType.ATTACK_FLIGHT,
                        distance = 40,
                        duration = 3,
                        success = true
                    )
                )
            )
        )
        
        every { matchRepository.updateMatch(any()) } returns updatedMatch
        
        // When
        val result = matchService.processMatch(matchId)
        
        // Then
        assertNotNull(result)
        assertEquals(1, result.rounds.size)
        assertNotNull(result.rounds[0].flight)
        assertEquals(FlightType.ATTACK_FLIGHT, result.rounds[0].flight?.flightType)
        assertEquals(40, result.rounds[0].flight?.distance)
        assertEquals(3, result.rounds[0].flight?.duration)
        assertTrue(result.rounds[0].flight?.success ?: false)
        
        verify { characterService.getCharacterById(challengerId) }
        verify { characterService.getCharacterById(opponentId) }
        verify { matchRepository.getMatchById(matchId) }
        verify { matchRepository.updateMatch(any()) }
        verify { leaderboardService.updateLeaderboardForMatch(any(), any(), any()) }
        verify { characterService.updateExperience(any(), any()) }
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
    
    private fun createSorcerer(id: Long, name: String): Sorcerer {
        return Sorcerer(
            id = id,
            accountId = 2L,
            name = name,
            health = 80,
            attackPower = 40,
            mana = 50,
            healingPower = 30,
            level = CharacterLevel.LEVEL_5,
            experience = 2000
        )
    }
} 