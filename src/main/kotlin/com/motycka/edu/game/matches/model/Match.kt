package com.motycka.edu.game.matches.model

data class Match(
    val id: Long? = null,
    val challenger: MatchCharacter,
    val opponent: MatchCharacter,
    val rounds: List<MatchRound>
)

data class MatchCharacter(
    val id: Long,
    val name: String,
    val characterClass: String,
    val level: String,
    val experienceTotal: Int,
    val experienceGained: Int,
    val isVictor: Boolean
)

data class MatchRound(
    val round: Int,
    val characterId: Long,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int
) 