package com.motycka.edu.game.characters.model


enum class CharacterLevel(
    val points: Int,
    private val requireExp: Int
) {
    LEVEL_1(200, 300),
    LEVEL_2(210, 600),
    LEVEL_3(230, 900),
    LEVEL_4(260, 1050),
    LEVEL_5(300, 1550),
    LEVEL_6(350, 2100),
    LEVEL_7(410, 2700),
    LEVEL_8(480, 3300),
    LEVEL_9(560, 3950),
    LEVEL_10(650, 4650);

    fun shouldLevelUp(currentExp: Int): Boolean {
        return currentExp >= requireExp
    }


    /*
     * Updates the level of a character based on their total points.
     *
     * @param character The character whose level is to be updated. Can be null if otherPoints is provided.
     * @param otherPoints A list of points to be used for level calculation if character is null. Must contain exactly 4 points.
     * @return The updated CharacterLevel based on the total points.
     * @throws IllegalArgumentException if neither character nor otherPoints are provided, or if otherPoints does not contain exactly 4 points.
     */
    fun upLevel(character: Character?, otherPoints: List<Int>?): CharacterLevel {
        val totalPoints: Int
        if (character != null) {
            totalPoints = character.health + when (character) {
                is Warrior -> character.attack + character.stamina + character.defense
                is Sorcerer -> character.attack + character.mana + character.healing
                else -> error("Require Character")
            }
        } else if (otherPoints != null) {
            require(otherPoints.size == 4) {
                "Require 4 points"
            }
            totalPoints = otherPoints.sum()
        } else {
            error("Require Character or List<Int>")
        }

        return entries.toTypedArray().findLast { it.points <= totalPoints } ?: LEVEL_1
    }

}