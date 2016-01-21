package twitterbot.logic

import twitter4j.TwitterException
import twitterbot.model.Config
import twitterbot.model.JSONPersistedState
import java.util.*


object TwitterBot {

    val MAX_TWEET_LENGTH = 140

    fun process(api: TwitterAPI, config: Config, persistentState: JSONPersistedState) {
        try {

            val twitter = api.twitter
            val retweeeterSet = TreeSet<String>()

            val state = persistentState.state!!

            for (status in twitter.getUserTimeline(config.target_account)) {

                state.maybeUpdateLastSeenTweetDate(status.createdAt)

                if (!state.seen_tweets_to_retweet_count.containsKey(status.id.toString())) {
                    println("new >" + status.text)
                    if (status.text.toUpperCase().contains("JOB")) {
                        val newStatus = "@${config.target_account}" + config.getMaybeRandomSignatureWithMaxLength(MAX_TWEET_LENGTH - config.target_account.length + 1)
                        println("tweeting> $newStatus")
                        twitter.updateStatus(newStatus)
                        Sleeper.sleep(config)
                    }
                    state.seen_tweets_to_retweet_count = state.seen_tweets_to_retweet_count.plus(Pair(status.id.toString(), status.retweetCount))
                } else if (status.retweetCount > state.seen_tweets_to_retweet_count[status.id.toString()]!!) {
                    for (status1 in twitter.getRetweets(status.id)) {
                        retweeeterSet.add(status1.user.screenName)
                    }
                }
            }

            config.retweet_accounts.forEach {
                for (status in twitter.getUserTimeline(it)) {
                    if (state.maybeUpdateLastSeenTweetDate(status.createdAt)) {
                        twitter.retweetStatus(status.id)
                        println("retweeting> " + status.text)
                        Sleeper.sleep(config)
                    }
                }
            }

            val newRetweeters = retweeeterSet.minus(state.processed_retweeters);

            if (!newRetweeters.isEmpty()) {

                newRetweeters.forEach {
                    var newStatus = "@$it " + config.getMaybeRandomRetweetReplyWithMaxLength(MAX_TWEET_LENGTH - it.length)
                    if (newStatus.length < MAX_TWEET_LENGTH) {
                        newStatus += " " + config.getMaybeRandomSignatureWithMaxLength(MAX_TWEET_LENGTH - newStatus.length)

                        newStatus = newStatus.replace("ORIGIN_ACCOUNT", config.target_account)
                        println("tweeting> $newStatus")
                        twitter.updateStatus(newStatus)
                    }

                    Sleeper.sleep(config)
                }
            }


            state.processed_retweeters = state.processed_retweeters.plus(newRetweeters)
            persistentState.write()

        } catch (exception: TwitterException) {
            println("got TwitterException - will try again later ")
        }
    }

}
