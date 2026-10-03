package com.voicerooms.app.ui.navigation

/** مسارات التنقل في التطبيق */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME = "home"
    const val EXPLORE = "explore"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"

    const val CREATE_ROOM = "create_room"
    const val ROOM = "room/{roomId}"
    const val EDIT_PROFILE = "edit_profile"
    const val WALLET = "wallet"
    const val GIFTS = "gifts"
    const val AGENCY = "agency"
    const val SETTINGS = "settings"
    const val USER_PROFILE = "user/{userId}"

    fun room(roomId: String) = "room/$roomId"
    fun user(userId: String) = "user/$userId"
}
