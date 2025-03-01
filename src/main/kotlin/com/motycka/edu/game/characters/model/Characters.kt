package com.motycka.edu.game.characters.model

import com.motycka.edu.game.account.model.AccountId
import org.hibernate.mapping.TableOwner

data class Character(
    val id: CharacterId? = null,
    val accountId : AccountId? = null,
    val name: String,
    val health: Int,
    val attack: Int,
    val stamina: Int,
    val defense: Int,
    val mana: Int? = null,
    val healing : Int? = null,
    val characterClass: String,
    var experience: Int? = null,
    var level: Int = 1,
    var isOwner: Boolean = false
)

/*


"id": "1",
  "name": "Aragorn",
  "health": 100,
  "attackPower": 50,
  "stamina": 30,
  "defensePower": 20,
  "mana": null,
  "healingPower": null,
  "characterClass": "WARRIOR",
  "level": "5",
  "experience": 2000,
  "shouldLevelUp": true,
  "isOwner": true
 */