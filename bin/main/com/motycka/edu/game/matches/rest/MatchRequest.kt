package com.motycka.edu.game.matches.rest

data class MatchRequest(
    val challengerId: Long,
    val opponentId: Long,
    val rounds: Int = 5
) 