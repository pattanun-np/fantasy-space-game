package com.pattnun.game.matches

import com.pattnun.game.matches.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

interface InterfaceMatchService {
    fun getAllMatches(): List<Match>

}

@Service
class MatchService : InterfaceMatchService {
    private val matches = mutableListOf<Match>()
    private var nextMatchId: Long = 1

    override fun getAllMatches(): List<Match> {
        return matches.toList()
    }


    private fun simulateFight(char1: Character, char2: Character): FightResult {
        val winner = if (Random.nextInt(100) < 50) char1 else char2
        return FightResult(
            winner = winner,
            healthDelta = -Random.nextInt(10, 25),
            staminaDelta = -Random.nextInt(5, 15),
            manaDelta = -Random.nextInt(10, 20)
        )
    }

    private fun calculateExperience(wins: Int, winnerLevel: Int, loserLevel: Int): Int {
        val baseExp = 100
        val levelDiff = loserLevel - winnerLevel
        val multiplier = when {
            levelDiff > 0 -> 1.5
            levelDiff < 0 -> 0.5
            else -> 1.0
        }
        return (baseExp * multiplier * wins).toInt()
    }


}

