package twitterbot

import twitterbot.logic.Sleeper
import twitterbot.logic.TwitterAPI
import twitterbot.logic.TwitterBot
import twitterbot.model.JSONPersistedConfig
import twitterbot.model.JSONPersistedState
import java.io.File

fun main(args: Array<String>) {
    println("> Bot init.")

    val persistedConfig = JSONPersistedConfig(File("config.json"))
    val persistedState = JSONPersistedState(File("state.json"))

    val config = persistedConfig.config!!

    val twitterAPI = TwitterAPI(config, persistedState)

    while(true) {
        TwitterBot.process(twitterAPI,config, persistedState)
        Sleeper.sleep(config)
    }
}

