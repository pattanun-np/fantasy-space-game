package com.motycka.edu.game.account.rest

import com.motycka.edu.game.account.model.AccountId

/**
 * Response object returning the account details.
 * This object is used to map the outgoing Kotlin object  to JSON response and is exposed to outside world.
 */
data class AccountResponse(
    val id: AccountId,
    val name: String,
    val username: String,
    val password: String
)
