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