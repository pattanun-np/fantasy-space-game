package com.motycka.edu.game.account.rest

import com.motycka.edu.game.account.model.Account

/**
 * Mappers to map between account request/response and account model
 */

fun AccountRegistrationRequest.toAccount() = Account(
    id = null,
    name = name,
    username = username,
    password = password
)

fun Account.toAccountResponse() = AccountResponse(
    id = requireNotNull(id) { "Account id must not be null" },
    name = name,
    username = username,
    password = "***" // we don't want to expose the password in the response
)
