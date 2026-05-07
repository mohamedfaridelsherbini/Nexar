package com.mohamedfaridelsherbini.nexar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform