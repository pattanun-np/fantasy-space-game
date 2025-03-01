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

private val logger = KotlinLogging.logger {}

interface InterfaceMatchService {
    fun getAllMatches(): List<Match>
    fun getMatchById(id: Long): Match?
    fun createMatch(matchRequest: MatchRequest): Match
    fun processMatch(matchId: Long): Match
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
        return processMatch(savedMatch.id!!)
    }
    
    override fun processMatch(matchId: Long): Match {
        val match = matchRepository.getMatchById(matchId) ?: throw IllegalArgumentException("Match not found")
        
        val challenger = characterService.getCharacterById(match.challenger.id)
        val opponent = characterService.getCharacterById(match.opponent.id)
        
        // Process rounds
        val rounds = processRounds(match.id!!, challenger, opponent, 5) // Default to 5 rounds
        
        // Determine winner
        val (challengerFinalHealth, opponentFinalHealth) = calculateFinalHealth(challenger, opponent, rounds)
        val challengerWon = challengerFinalHealth > 0 && (opponentFinalHealth <= 0 || challengerFinalHealth > opponentFinalHealth)
        
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
            rounds = rounds
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
            if (challengerHealth <= 0 && opponentHealth <= 0) break
            
            val activeData = if (challengerTurn) {
                ActiveCharacterData(
                    activeCharacter = challenger,
                    targetCharacter = opponent,
                    activeHealth = challengerHealth,
                    activeStamina = challengerStamina,
                    activeMana = challengerMana
                )
            } else {
                ActiveCharacterData(
                    activeCharacter = opponent,
                    targetCharacter = challenger,
                    activeHealth = opponentHealth,
                    activeStamina = opponentStamina,
                    activeMana = opponentMana
                )
            }
            
            // Skip turn if character is defeated
            if (activeData.activeHealth <= 0) {
                challengerTurn = !challengerTurn
                continue
            }
            
            // Calculate turn deltas and flight
            val (healthDelta, staminaDelta, manaDelta, flight) = calculateTurnAction(
                activeData.activeCharacter.characterClass,
                activeData.activeStamina,
                activeData.activeMana,
                roundNum
            )
            
            // Apply deltas
            if (challengerTurn) {
                opponentHealth += healthDelta
                challengerStamina += staminaDelta
                challengerMana += manaDelta
                
                rounds.add(MatchRound(
                    round = roundNum,
                    characterId = challenger.id.toLong(),
                    healthDelta = healthDelta,
                    staminaDelta = staminaDelta,
                    manaDelta = manaDelta,
                    flight = flight
                ))
            } else {
                challengerHealth += healthDelta
                opponentStamina += staminaDelta
                opponentMana += manaDelta
                
                rounds.add(MatchRound(
                    round = roundNum,
                    characterId = opponent.id.toLong(),
                    healthDelta = healthDelta,
                    staminaDelta = staminaDelta,
                    manaDelta = manaDelta,
                    flight = flight
                ))
            }
            
            // Switch turns
            challengerTurn = !challengerTurn
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

    private fun calculateTurnAction(
        characterClass: String,
        stamina: Int,
        mana: Int,
        roundNumber: Int
    ): Tuple4<Int, Int, Int, Flight> {
        // Determine flight type based on character class, resources, and round number
        val flightType = determineFlightType(characterClass, stamina, mana, roundNumber)
        
        // Calculate flight parameters
        val (distance, duration, success) = calculateFlightParameters(flightType, stamina, mana)
        
        // Create flight object
        val flight = Flight(
            flightType = flightType,
            distance = distance,
            duration = duration,
            success = success
        )
        
        // Calculate deltas based on flight
        val (healthDelta, staminaDelta, manaDelta) = calculateDeltasFromFlight(flight, characterClass)
        
        return Tuple4(healthDelta, staminaDelta, manaDelta, flight)
    }
    
    private fun determineFlightType(
        characterClass: String,
        stamina: Int,
        mana: Int,
        roundNumber: Int
    ): FlightType {
        // Different flight strategies based on character class and round number
        return when {
            // Warriors prefer attack flights early, defensive flights when low on stamina
            characterClass == "WARRIOR" && stamina >= 15 -> FlightType.ATTACK_FLIGHT
            characterClass == "WARRIOR" && stamina < 15 -> FlightType.DEFENSIVE_FLIGHT
            
            // Sorcerers prefer power flights when they have mana, healing flights when needed
            characterClass == "SORCERER" && mana >= 20 -> FlightType.POWER_FLIGHT
            characterClass == "SORCERER" && mana < 20 -> FlightType.HEALING_FLIGHT
            
            // Both classes might use evasive flights in later rounds
            roundNumber > 3 && Random.nextInt(10) > 7 -> FlightType.EVASIVE_FLIGHT
            
            // Default flights based on class
            characterClass == "WARRIOR" -> FlightType.ATTACK_FLIGHT
            else -> FlightType.POWER_FLIGHT
        }
    }
    
    private fun calculateFlightParameters(
        flightType: FlightType,
        stamina: Int,
        mana: Int
    ): Triple<Int, Int, Boolean> {
        // Calculate distance based on flight type
        val baseDistance = when (flightType) {
            FlightType.ATTACK_FLIGHT -> 30 + Random.nextInt(20)
            FlightType.DEFENSIVE_FLIGHT -> 15 + Random.nextInt(15)
            FlightType.EVASIVE_FLIGHT -> 40 + Random.nextInt(30)
            FlightType.HEALING_FLIGHT -> 10 + Random.nextInt(10)
            FlightType.POWER_FLIGHT -> 25 + Random.nextInt(25)
        }
        
        // Calculate duration based on flight type
        val baseDuration = when (flightType) {
            FlightType.ATTACK_FLIGHT -> 3 + Random.nextInt(2)
            FlightType.DEFENSIVE_FLIGHT -> 5 + Random.nextInt(3)
            FlightType.EVASIVE_FLIGHT -> 2 + Random.nextInt(2)
            FlightType.HEALING_FLIGHT -> 6 + Random.nextInt(4)
            FlightType.POWER_FLIGHT -> 4 + Random.nextInt(3)
        }
        
        // Calculate success chance based on resources and flight type
        val successChance = when (flightType) {
            FlightType.ATTACK_FLIGHT -> 0.7 + (stamina / 100.0)
            FlightType.DEFENSIVE_FLIGHT -> 0.8 + (stamina / 150.0)
            FlightType.EVASIVE_FLIGHT -> 0.6 + (stamina / 80.0)
            FlightType.HEALING_FLIGHT -> 0.75 + (mana / 100.0)
            FlightType.POWER_FLIGHT -> 0.65 + (mana / 80.0)
        }.coerceIn(0.0, 1.0)
        
        val success = Random.nextDouble() < successChance
        
        return Triple(baseDistance, baseDuration, success)
    }
    
    private fun calculateDeltasFromFlight(
        flight: Flight,
        characterClass: String
    ): Triple<Int, Int, Int> {
        // Base damage/healing values
        val baseDamage = when (flight.flightType) {
            FlightType.ATTACK_FLIGHT -> -Random.nextInt(15, 26)
            FlightType.POWER_FLIGHT -> -Random.nextInt(20, 31)
            FlightType.DEFENSIVE_FLIGHT -> -Random.nextInt(5, 16)
            FlightType.EVASIVE_FLIGHT -> -Random.nextInt(3, 11)
            FlightType.HEALING_FLIGHT -> -Random.nextInt(5, 11)
        }
        
        // Adjust damage based on success and distance
        val adjustedDamage = if (flight.success) {
            // Successful flights do full damage, with bonus for longer distances
            (baseDamage * (1.0 + flight.distance / 100.0)).toInt()
        } else {
            // Failed flights do reduced damage
            (baseDamage * 0.3).toInt()
        }
        
        // Resource costs based on flight type and character class
        val (staminaDelta, manaDelta) = when {
            characterClass == "WARRIOR" && flight.flightType == FlightType.ATTACK_FLIGHT -> Pair(-10, 0)
            characterClass == "WARRIOR" && flight.flightType == FlightType.DEFENSIVE_FLIGHT -> Pair(5, 0)
            characterClass == "WARRIOR" && flight.flightType == FlightType.EVASIVE_FLIGHT -> Pair(-15, 0)
            characterClass == "SORCERER" && flight.flightType == FlightType.POWER_FLIGHT -> Pair(0, -15)
            characterClass == "SORCERER" && flight.flightType == FlightType.HEALING_FLIGHT -> Pair(0, 10)
            characterClass == "SORCERER" && flight.flightType == FlightType.EVASIVE_FLIGHT -> Pair(0, -20)
            else -> Pair(-5, -5) // Default resource cost
        }
        
        return Triple(adjustedDamage, staminaDelta, manaDelta)
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
    
    // Helper class for returning 4 values
    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
} 