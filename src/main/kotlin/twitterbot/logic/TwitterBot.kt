package twitterbot.logic

import twitter4j.Twitter
import twitter4j.TwitterException
import twitterbot.model.Config
import twitterbot.model.State
import java.util.*

object TwitterBot {

    val MAX_TWEET_LENGTH = 140

    fun process(twitter: Twitter, config: Config, persistentState: JSONPersisted<State>) {
        try {
            val retweeterSet = TreeSet<String>()

            val state = persistentState.get()

            for (status in twitter.getUserTimeline(config.target_account)) {

                state.maybeUpdateLastSeenTweetDate(status.createdAt)

                if (!state.seen_tweets_to_retweet_count.containsKey(status.id.toString())) {
                    println("new >" + status.text)
                    if (status.text.toUpperCase().contains("JOB")) {
                        val payload_max_length = MAX_TWEET_LENGTH - config.target_account.length + 1
                        val payload = config.getMaybeRandomSignatureWithMaxLength(payload_max_length)
                        val entropy = SymbolPool.getRandomString(length = Math.min(5, payload_max_length))
                        val newStatus = "@${config.target_account} $payload $entropy"
                        println("tweeting> $newStatus")
                        twitter.updateStatus(newStatus)
                        Sleeper.sleep(config)
                    }
                    state.seen_tweets_to_retweet_count = state.seen_tweets_to_retweet_count.plus(Pair(status.id.toString(), status.retweetCount))
                } else if (status.retweetCount > state.seen_tweets_to_retweet_count[status.id.toString()]!!) {
                    for (status1 in twitter.getRetweets(status.id)) {
                        retweeterSet.add(status1.user.screenName)
                    }
                }
            }

            config.retweet_accounts.forEach {
                for (status in twitter.getUserTimeline(it)) {
                    if (state.maybeUpdateLastSeenTweetDate(status.createdAt)) {
                        if (!status.isRetweet && !status.text.contains("@")) {
                            twitter.retweetStatus(status.id)
                            println("retweeting> " + status.text)
                            Sleeper.sleep(config)
                        } else {
                            println("not retweeting retweet> " + status.text)
                        }
                    }
                }
            }

            val newRetweeters = retweeterSet.minus(state.processed_retweeters);

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
            println("got TwitterException - will try again later " + exception.message)
        }
    }

}
