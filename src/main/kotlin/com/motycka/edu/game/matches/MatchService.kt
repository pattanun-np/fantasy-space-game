package com.motycka.edu.game.matches

import com.motycka.edu.game.account.InterfaceAccountService
import com.motycka.edu.game.characters.InterfaceCharacterService
import com.motycka.edu.game.matches.model.Match
import com.motycka.edu.game.matches.model.MatchCharacter
import com.motycka.edu.game.matches.model.MatchRound
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

interface InterfaceMatchService {
    fun getAllMatches(): List<Match>
    fun createMatch(challengerId: Long, opponentId: Long, rounds: Int): Match
}

@Service
class MatchService(
    private val characterService: InterfaceCharacterService,
    private val accountService: InterfaceAccountService,
    private val matchRepository: MatchRepository
) : InterfaceMatchService {
    // Store matches in memory
    private val matches = mutableListOf<Match>()
    private var nextMatchId: Long = 1

    override fun getAllMatches(): List<Match> {
        val list =  matchRepository.getAllMatches()

        return list
    }

    override fun createMatch(challengerId: Long, opponentId: Long, rounds: Int): Match {
        logger.debug { "Creating match between challenger $challengerId and opponent $opponentId for $rounds rounds" }

        // Verify challenger ownership
        val challenger = characterService.getCharacterById(challengerId)
        val currentAccountId = accountService.getCurrentAccountId()
        if (challenger.accountId != currentAccountId) {
            throw IllegalStateException("Cannot create match: not the owner of challenger character")
        }

        // Get opponent
        val opponent = characterService.getCharacterById(opponentId)
        if (challenger.id == opponent.id) {
            throw IllegalArgumentException("Challenger and opponent cannot be the same character")
        }

        // Simulate match
        val matchRounds = mutableListOf<MatchRound>()
        var challengerHealth = challenger.health
        var opponentHealth = opponent.health
        var challengerStamina = challenger.stamina
        var opponentStamina = opponent.stamina
        var challengerMana = challenger.mana ?: 0
        var opponentMana = opponent.mana ?: 0

        for (round in 1..rounds) {
            // Challenger's turn
            val (cHealthDelta, cStaminaDelta, cManaDelta) = calculateTurnDeltas(
                challenger.characterClass,
                challengerStamina,
                challengerMana
            )
            opponentHealth += cHealthDelta
            challengerStamina += cStaminaDelta
            challengerMana += cManaDelta
            matchRounds.add(
                MatchRound(
                    round = round * 2 - 1,
                    characterId = challengerId,
                    healthDelta = cHealthDelta,
                    staminaDelta = cStaminaDelta,
                    manaDelta = cManaDelta
                )
            )

            // Check if opponent is defeated
            if (opponentHealth <= 0) break

            // Opponent's turn
            val (oHealthDelta, oStaminaDelta, oManaDelta) = calculateTurnDeltas(
                opponent.characterClass,
                opponentStamina,
                opponentMana
            )
            challengerHealth += oHealthDelta
            opponentStamina += oStaminaDelta
            opponentMana += oManaDelta
            matchRounds.add(
                MatchRound(
                    round = round * 2,
                    characterId = opponentId,
                    healthDelta = oHealthDelta,
                    staminaDelta = oStaminaDelta,
                    manaDelta = oManaDelta
                )
            )

            // Check if challenger is defeated
            if (challengerHealth <= 0) break
        }

        // Determine winner and calculate experience
        val challengerWon = opponentHealth <= 0 || (challengerHealth > opponentHealth && challengerHealth > 0)
        val (challengerExpGained, opponentExpGained) = calculateExperience(
            challengerWon,
            challenger.level,
            opponent.level
        )

        // Create match object
        val match = Match(
            id = nextMatchId++,
            challenger = MatchCharacter(
                id = challenger.id!!,
                name = challenger.name,
                characterClass = challenger.characterClass,
                level = challenger.level.toString(),
                experienceTotal = challenger.experience!!,
                experienceGained = challengerExpGained,
                isVictor = challengerWon
            ),
            opponent = MatchCharacter(
                id = opponent.id!!,
                name = opponent.name,
                characterClass = opponent.characterClass,
                level = opponent.level.toString(),
                experienceTotal = opponent.experience!!,
                experienceGained = opponentExpGained,
                isVictor = !challengerWon
            ),
            rounds = matchRounds
        )

        // Store match in memory
        matches.add(match)

        // Update character experience (this still needs to persist to database)
        val challengerExp = challenger.experience ?: 0
        val opponentExp = opponent.experience ?: 0
        
        characterService.updateCharacter(
            challenger.id,
            challenger.copy(experience = challengerExp + challengerExpGained)
        )
        characterService.updateCharacter(
            opponent.id,
            opponent.copy(experience = opponentExp + opponentExpGained)
        )

        return match
    }

    private fun calculateTurnDeltas(
        characterClass: String,
        stamina: Int,
        mana: Int
    ): Triple<Int, Int, Int> {
        return when (characterClass) {
            "WARRIOR" -> {
                if (stamina >= 10) {
                    Triple(-Random.nextInt(15, 26), -10, 0)  // Strong attack using stamina
                } else {
                    Triple(-Random.nextInt(5, 16), 5, 0)     // Weak attack, recover stamina
                }
            }
            "SORCERER" -> {
                if (mana >= 15) {
                    Triple(-Random.nextInt(20, 31), 0, -15)  // Strong spell
                } else {
                    Triple(-Random.nextInt(5, 11), 0, 10)    // Weak attack, recover mana
                }
            }
            else -> Triple(0, 0, 0)
        }
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