package com.motycka.edu.game.matches

import com.motycka.edu.game.account.InterfaceAccountService
import com.motycka.edu.game.characters.InterfaceCharacterService
import com.motycka.edu.game.characters.model.Character
import com.motycka.edu.game.leaderboard.InterfaceLeaderboardService
import com.motycka.edu.game.matches.model.*
import com.motycka.edu.game.matches.rest.MatchRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.random.Random
import com.motycka.edu.game.characters.model.CharacterClass

private val logger = KotlinLogging.logger {}

interface InterfaceMatchService {
    fun getAllMatches(): List<Match>
    fun getMatchById(id: Long): Match?
    fun createMatch(matchRequest: MatchRequest): Match
    fun processMatch(matchId: Long, numRounds: Int = 5): Match
}

@Service
class MatchService(
    private val characterService: InterfaceCharacterService,
    private val accountService: InterfaceAccountService,
    private val leaderboardService: InterfaceLeaderboardService,
    private val matchRepository: MatchRepository
) : InterfaceMatchService {
    // Store matches in memory
    private val matches = mutableListOf<Match>()
    private var nextMatchId: Long = 1

    override fun getAllMatches(): List<Match> {
        val list = matchRepository.getAllMatches()
        return list
    }

    override fun getMatchById(id: Long): Match? {
        return matchRepository.getMatchById(id)
    }

    override fun createMatch(matchRequest: MatchRequest): Match {
        val challenger = characterService.getCharacterById(matchRequest.challengerId)
        val opponent = characterService.getCharacterById(matchRequest.opponentId)
        
        val challengerMatchChar = MatchCharacter(
            id = challenger.id.toLong(),
            name = challenger.name,
            characterClass = challenger.characterClass,
            level = challenger.level.toString(),
            experienceTotal = challenger.experience ?: 0,
            experienceGained = 0,
            isVictor = false
        )
        
        val opponentMatchChar = MatchCharacter(
            id = opponent.id.toLong(),
            name = opponent.name,
            characterClass = opponent.characterClass,
            level = opponent.level.toString(),
            experienceTotal = opponent.experience ?: 0,
            experienceGained = 0,
            isVictor = false
        )
        
        val match = Match(
            id = nextMatchId++,
            challenger = challengerMatchChar,
            opponent = opponentMatchChar,
            rounds = emptyList()
        )
        
        val savedMatch = matchRepository.saveMatch(match)
        
        // Use the rounds from the matchRequest, defaulting to 5 if not specified
        val numRounds = matchRequest.rounds ?: 5
        
        return processMatch(savedMatch.id!!, numRounds)
    }
    
    override fun processMatch(matchId: Long, numRounds: Int): Match {
        val match = matchRepository.getMatchById(matchId) ?: throw IllegalArgumentException("Match not found")
        
        val challenger = characterService.getCharacterById(match.challenger.id)
        val opponent = characterService.getCharacterById(match.opponent.id)
        
        // Process rounds with the specified number
        val rounds = processRounds(match.id!!, challenger, opponent, numRounds)
        
        // Determine winner
        val (challengerFinalHealth, opponentFinalHealth) = calculateFinalHealth(challenger, opponent, rounds)
        
        // Determine match outcome
        val matchOutcome = when {
            challengerFinalHealth <= 0 && opponentFinalHealth <= 0 -> MatchOutcome.DRAW
            challengerFinalHealth <= 0 -> MatchOutcome.OPPONENT_WON
            opponentFinalHealth <= 0 -> MatchOutcome.CHALLENGER_WON
            challengerFinalHealth > opponentFinalHealth -> MatchOutcome.CHALLENGER_WON
            opponentFinalHealth > challengerFinalHealth -> MatchOutcome.OPPONENT_WON
            else -> MatchOutcome.DRAW
        }
        
        val challengerWon = matchOutcome == MatchOutcome.CHALLENGER_WON
        
        // Calculate experience
        val (challengerExp, opponentExp) = calculateExperience(
            challengerWon, 
            challenger.level, 
            opponent.level
        )
        
        // Update match with results
        val updatedMatch = match.copy(
            challenger = match.challenger.copy(
                experienceGained = challengerExp,
                isVictor = challengerWon
            ),
            opponent = match.opponent.copy(
                experienceGained = opponentExp,
                isVictor = !challengerWon
            ),
            rounds = rounds,
            matchOutcome = matchOutcome
        )
        
        // Update leaderboard
        leaderboardService.updateLeaderboardForMatch(
            challengerId = challenger.id.toLong(),
            opponentId = opponent.id.toLong(),
            challengerWon = challengerWon
        )
        
        // Update character experience
        characterService.updateExperience(challenger.id.toLong(), challenger.experience!! + challengerExp)
        characterService.updateExperience(opponent.id.toLong(), opponent.experience!! + opponentExp)
        
        // Save updated match
        return matchRepository.updateMatch(updatedMatch)
    }
    
    private fun processRounds(
        matchId: Long,
        challenger: Character,
        opponent: Character,
        numRounds: Int
    ): List<MatchRound> {
        val rounds = mutableListOf<MatchRound>()
        
        var challengerHealth = challenger.health
        var opponentHealth = opponent.health
        var challengerStamina = challenger.stamina
        var opponentStamina = opponent.stamina
        var challengerMana = challenger.mana
        var opponentMana = opponent.mana
        
        // Determine who goes first (higher level or random if same level)
        var challengerTurn = if (challenger.level > opponent.level) true
                            else if (opponent.level > challenger.level) false
                            else Random.nextBoolean()
        
        for (roundNum in 1..numRounds) {
            // Skip round if both characters are defeated
            if (challengerHealth <= 0 && opponentHealth <= 0) {
                logger.info { "Both characters defeated, ending match at round $roundNum" }
                break
            }
            
            // If it's the challenger's turn but they're defeated, skip their turn
            if (challengerTurn && challengerHealth <= 0) {
                logger.info { "Challenger defeated, skipping their turn in round $roundNum" }
                challengerTurn = !challengerTurn
                continue
            }
            
            // If it's the opponent's turn but they're defeated, skip their turn
            if (!challengerTurn && opponentHealth <= 0) {
                logger.info { "Opponent defeated, skipping their turn in round $roundNum" }
                challengerTurn = !challengerTurn
                continue
            }
            
            // If both characters have skipped their turns, end the match
            if ((challengerHealth <= 0 && opponentHealth <= 0) ||
                (roundNum > 23 && (challengerHealth <= 0 || opponentHealth <= 0))) {
                logger.info { "Match ended at round $roundNum due to character defeat" }
                break
            }
            
            // Get active character info
            val activeCharacter = if (challengerTurn) challenger else opponent
            val activeStamina = if (challengerTurn) challengerStamina else opponentStamina
            val activeMana = if (challengerTurn) challengerMana else opponentMana
            
            // Calculate turn deltas and flight
            val turnResult = calculateTurnAction(
                activeCharacter.characterClass,
                activeStamina,
                activeMana,
                roundNum
            )
            
            // Apply deltas
            if (challengerTurn) {
                // Only apply damage if the opponent is still alive
                if (opponentHealth > 0) {
                    opponentHealth += turnResult.healthDelta
                }
                challengerStamina += turnResult.staminaDelta
                challengerMana += turnResult.manaDelta
                
                rounds.add(MatchRound(
                    round = roundNum,
                    characterId = challenger.id.toLong(),
                    healthDelta = turnResult.healthDelta,
                    staminaDelta = turnResult.staminaDelta,
                    manaDelta = turnResult.manaDelta,
                    flight = turnResult.flight
                ))
            } else {
                // Only apply damage if the challenger is still alive
                if (challengerHealth > 0) {
                    challengerHealth += turnResult.healthDelta
                }
                opponentStamina += turnResult.staminaDelta
                opponentMana += turnResult.manaDelta
                
                rounds.add(MatchRound(
                    round = roundNum,
                    characterId = opponent.id.toLong(),
                    healthDelta = turnResult.healthDelta,
                    staminaDelta = turnResult.staminaDelta,
                    manaDelta = turnResult.manaDelta,
                    flight = turnResult.flight
                ))
            }
            
            // Switch turns
            challengerTurn = !challengerTurn
            
            // Check if the match should end due to a character being defeated
            if (challengerHealth <= 0 || opponentHealth <= 0) {
                logger.info { "Character defeated, ending match at round $roundNum" }
                // Add one more round to show the final state, then break
                if (roundNum < numRounds) {
                    rounds.add(MatchRound(
                        round = roundNum + 1,
                        characterId = if (challengerHealth <= 0) opponent.id.toLong() else challenger.id.toLong(),
                        healthDelta = 0,
                        staminaDelta = 0,
                        manaDelta = 0,
                        flight = Flight(
                            flightType = FlightType.POWER_FLIGHT,
                            distance = 0,
                            duration = 0,
                            success = true,
                            description = "Match ended - ${if (challengerHealth <= 0) opponent.name else challenger.name} is victorious!"
                        )
                    ))
                }
                break
            }
        }
        
        return rounds
    }
    
    private data class ActiveCharacterData(
        val activeCharacter: Character,
        val targetCharacter: Character,
        val activeHealth: Int,
        val activeStamina: Int,
        val activeMana: Int
    )
    
    private fun calculateFinalHealth(
        challenger: Character,
        opponent: Character,
        rounds: List<MatchRound>
    ): Pair<Int, Int> {
        var challengerHealth = challenger.health
        var opponentHealth = opponent.health
        
        for (round in rounds) {
            if (round.characterId == challenger.id.toLong()) {
                opponentHealth += round.healthDelta
            } else {
                challengerHealth += round.healthDelta
            }
        }
        
        return Pair(challengerHealth, opponentHealth)
    }

    // Replace the Triple with a data class to hold the turn results
    private data class TurnResult(
        val healthDelta: Int,
        val staminaDelta: Int,
        val manaDelta: Int,
        val flight: Flight?
    )

    private fun calculateTurnAction(
        characterClass: String,
        stamina: Int,
        mana: Int,
        roundNum: Int
    ): TurnResult {
        val random = Random.nextInt(100)
        
        // Generate a flight based on character class and random chance
        val flight = when {
            random < 20 -> {
                val flightType = when (characterClass) {
                    "WARRIOR" -> FlightType.ATTACK_FLIGHT
                    "MAGE" -> FlightType.POWER_FLIGHT
                    "ROGUE" -> FlightType.EVASIVE_FLIGHT
                    "HEALER" -> FlightType.HEALING_FLIGHT
                    else -> FlightType.ATTACK_FLIGHT // Default case
                }
                
                Flight(
                    flightType = flightType,
                    distance = Random.nextInt(10, 100),
                    duration = Random.nextInt(1, 10),
                    success = Random.nextBoolean(),
                    description = "Performed a ${flightType.getDisplayName()} flight"
                )
            }
            else -> null
        }
        
        // Calculate deltas based on flight
        val healthDelta = flight?.let {
            when (it.flightType) {
                FlightType.ATTACK_FLIGHT -> if (it.success) -Random.nextInt(10, 30) else -Random.nextInt(1, 10)
                FlightType.POWER_FLIGHT -> if (it.success) -Random.nextInt(15, 40) else -Random.nextInt(1, 15)
                FlightType.EVASIVE_FLIGHT -> 0
                FlightType.HEALING_FLIGHT -> if (it.success) Random.nextInt(10, 30) else Random.nextInt(1, 10)
                FlightType.DEFENSIVE_FLIGHT -> 0
            }
        } ?: -Random.nextInt(5, 15)
        
        val staminaDelta = flight?.let {
            when (it.flightType) {
                FlightType.ATTACK_FLIGHT -> -Random.nextInt(5, 10)
                FlightType.POWER_FLIGHT -> -Random.nextInt(10, 20)
                FlightType.EVASIVE_FLIGHT -> -Random.nextInt(3, 8)
                FlightType.HEALING_FLIGHT -> -Random.nextInt(5, 15)
                FlightType.DEFENSIVE_FLIGHT -> -Random.nextInt(5, 10)
            }
        } ?: -Random.nextInt(1, 5)
        
        val manaDelta = flight?.let {
            when (it.flightType) {
                FlightType.ATTACK_FLIGHT -> -Random.nextInt(1, 5)
                FlightType.POWER_FLIGHT -> -Random.nextInt(10, 20)
                FlightType.EVASIVE_FLIGHT -> -Random.nextInt(1, 3)
                FlightType.HEALING_FLIGHT -> -Random.nextInt(5, 15)
                FlightType.DEFENSIVE_FLIGHT -> -Random.nextInt(3, 8)
            }
        } ?: -Random.nextInt(1, 3)
        
        return TurnResult(healthDelta, staminaDelta, manaDelta, flight)
    }
    
    private fun calculateExperience(challengerWon: Boolean, challengerLevel: Int, opponentLevel: Int): Pair<Int, Int> {
        val baseExp = 100
        val levelDiff = opponentLevel - challengerLevel
        val multiplier = when {
            levelDiff > 0 -> 1.5  // More exp for defeating higher level opponent
            levelDiff < 0 -> 0.5  // Less exp for defeating lower level opponent
            else -> 1.0          // Normal exp for same level
        }

        return if (challengerWon) {
            Pair(
                (baseExp * multiplier).toInt(),  // Winner gets full exp
                (baseExp * 0.25).toInt()         // Loser gets consolation exp
            )
        } else {
            Pair(
                (baseExp * 0.25).toInt(),        // Loser gets consolation exp
                (baseExp * multiplier).toInt()    // Winner gets full exp
            )
        }
    }
} 