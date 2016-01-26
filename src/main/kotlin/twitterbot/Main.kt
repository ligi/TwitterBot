package twitterbot

import twitterbot.logic.Sleeper
import twitterbot.logic.TwitterAPI
import twitterbot.logic.TwitterBot
import twitterbot.model.Config
import twitterbot.logic.JSONPersisted
import twitterbot.model.State
import java.io.File

fun main(args: Array<String>) {
    println("> Bot init.")

    val persistedConfig = JSONPersisted(File("config.json"), Config())

    if (persistedConfig.get().throw_not_configured) {
        throw RuntimeException("Please edit config.json")
    }

    val persistedState = JSONPersisted(File("state.json"), State())

    val config = persistedConfig.get()

    val twitterAPI = TwitterAPI(config, persistedState)

    while (true) {
        TwitterBot.process(twitterAPI.twitter, config, persistedState)
        Sleeper.sleep(config)
    }
}

