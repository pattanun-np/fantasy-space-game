package com.motycka.edu.game.matches.rest

data class MatchRequest(
    val rounds: Int,
    val challengerId: Long,
    val opponentId: Long
) 