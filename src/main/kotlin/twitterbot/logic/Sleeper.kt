package twitterbot.logic

import twitterbot.model.Config

object Sleeper {

    fun sleep(config: Config) {
        val additionalWaitTime = (Math.random() * config.max_additional_random_time_between_checks).toInt()

        println("Sleeping for ${config.min_time_between_checks}+$additionalWaitTime s")
        Thread.sleep((config.min_time_between_checks + additionalWaitTime) * 1000L);
    }

}
