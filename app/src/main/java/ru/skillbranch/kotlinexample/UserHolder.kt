package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {

    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also {
                when {
                    !map.containsKey(it.login) -> map[it.login] = it
                    else -> throw IllegalArgumentException("A user with this email already exists")
                }
            }
    }

    fun loginUser(
        login: String,
        password: String
    ): String? =
        (map[login.trim()] ?: map[login.simplifyPhone()])?.let {
            if (it.checkPassword(password)) it.userInfo
            else null
        }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ): User {
        if (!isValidatedPhone(rawPhone)) throw java.lang.IllegalArgumentException(
            "Enter a valid phone number starting with a + and containing 11 digits"
        )
        return User.makeUser(fullName, phone = rawPhone)
            .also {
                when {
                    !map.containsKey(it.login) -> map[it.login] = it
                    else -> throw IllegalArgumentException("A user with this phone already exists")
                }
            }
    }

    fun requestAccessCode(login: String): Unit {
        map[login.simplifyPhone()]?.run {
            this.requestAccessCode(login.simplifyPhone())
        }
    }

    fun importUsers(csvList: List<String>): List<User> {
        val result = mutableListOf<User>()
        csvList.forEach {
            // Полное имя пользователя; email; соль:хеш пароля; телефон
            val splitted = it.split(";")
            val fullName = splitted[0].trim()
            val email = splitted[1].trim()
            val saltHash = splitted[2].trim().split(":")
            val salt = saltHash[0].trim()
            val hash = saltHash[1].trim()
            val rawPhone = splitted[3].trim()
            result.add(User.loadUser(fullName, email, salt, hash, rawPhone))
        }
        return result;
    }

    private fun isValidatedPhone(rawPhone: String): Boolean =
        rawPhone.simplifyPhone().contains("""^\+\d{11}$""".toRegex())

    private fun String.simplifyPhone(): String = this.replace("""[^+\d]""".toRegex(), "")
}