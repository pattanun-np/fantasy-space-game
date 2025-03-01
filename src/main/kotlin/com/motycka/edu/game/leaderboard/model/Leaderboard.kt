package com.motycka.edu.game.leaderboard.model

import com.motycka.edu.game.characters.model.Character

data class LeaderboardEntry(
    val position: Int,
    val character: Character,
    val wins: Int,
    val losses: Int,
    val draws: Int
) 