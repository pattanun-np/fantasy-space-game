package com.pattnun.game.matches.model

import java.time.LocalDateTime

data class Round(
    val id: Long? = null,
    val matchId: Long,
    val roundNumber: Int,
    val characterId: Long,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int,
    val isWinner: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now()
) 