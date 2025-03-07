package com.motycka.edu.game.matches.model

data class Match(
    val id: Long? = null,
    val challenger: MatchCharacter,
    val opponent: MatchCharacter,
    val rounds: List<MatchRound> = emptyList(),
    val matchOutcome: MatchOutcome = MatchOutcome.UNKNOWN
)

data class MatchCharacter(
    val id: Long,
    val name: String,
    val characterClass: String,
    val level: String,
    val experienceTotal: Int,
    val experienceGained: Int,
    val isVictor: Boolean,
    val status: String = if (isVictor) "VICTOR" else "DEFEATED"
)

data class MatchRound(
    val round: Int,
    val characterId: Long,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int,
    val flight: Flight? = null,
    val actionType: String = flight?.flightType?.getDisplayName() ?: "Standard",
    val actionResult: String = flight?.let { if (it.success) "SUCCESS" else "FAILED" } ?: "STANDARD"
)

data class Flight(
    val flightType: FlightType,
    val distance: Int,
    val duration: Int,
    val success: Boolean,
    val description: String = generateFlightDescription(flightType, success)
)

enum class FlightType {
    ATTACK_FLIGHT,
    DEFENSIVE_FLIGHT,
    EVASIVE_FLIGHT,
    HEALING_FLIGHT,
    POWER_FLIGHT;
    
    fun getDisplayName(): String {
        return when (this) {
            ATTACK_FLIGHT -> "Attack"
            DEFENSIVE_FLIGHT -> "Defense"
            EVASIVE_FLIGHT -> "Evasion"
            HEALING_FLIGHT -> "Healing"
            POWER_FLIGHT -> "Power"
        }
    }
}

fun generateFlightDescription(flightType: FlightType, success: Boolean): String {
    val action = when (flightType) {
        FlightType.ATTACK_FLIGHT -> "attacks"
        FlightType.DEFENSIVE_FLIGHT -> "defends"
        FlightType.EVASIVE_FLIGHT -> "evades"
        FlightType.HEALING_FLIGHT -> "heals"
        FlightType.POWER_FLIGHT -> "unleashes a powerful attack"
    }
    
    val outcome = if (success) {
        when (flightType) {
            FlightType.ATTACK_FLIGHT -> "landing a solid hit"
            FlightType.DEFENSIVE_FLIGHT -> "successfully blocking the attack"
            FlightType.EVASIVE_FLIGHT -> "successfully dodging the attack"
            FlightType.HEALING_FLIGHT -> "restoring health"
            FlightType.POWER_FLIGHT -> "dealing massive damage"
        }
    } else {
        when (flightType) {
            FlightType.ATTACK_FLIGHT -> "but misses"
            FlightType.DEFENSIVE_FLIGHT -> "but fails to block"
            FlightType.EVASIVE_FLIGHT -> "but fails to dodge"
            FlightType.HEALING_FLIGHT -> "but the spell fizzles"
            FlightType.POWER_FLIGHT -> "but the attack goes wild"
        }
    }
    
    return "Character $action, $outcome"
}

enum class MatchOutcome {
    CHALLENGER_WON,
    OPPONENT_WON,
    DRAW,
    UNKNOWN
} 