package com.martyphee.temperature.http.auth

import io.estatico.newtype.macros.newtype

object users {

  @newtype case class AuthToken(value: String)

  case class User(token: AuthToken)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

}
