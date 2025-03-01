package com.motycka.edu.game.account

import com.motycka.edu.game.account.model.Account

object AccountFixtures {
    val UNKNOWN = "unknown"
    val DEVELOPER = Account(
        id = 1L,
        name = "The Developer",
        username = "developer",
        password = "password"
    )
    val TESTER = Account(
        id = 2L,
        name = "The Tester",
        username = "tester",
        password = "password"
    )
}
