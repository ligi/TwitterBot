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
            val stringGenerator = StringGenerator(config)
            val retweeterSet = TreeSet<String>()

            val state = persistentState.get()

            state.unprocessed_follower_pool = state.unprocessed_follower_pool.minus(state.processed_follower_pool)
            persistentState.write()

            if (!state.unprocessed_follower_pool.isEmpty()) {
                state.unprocessed_follower_pool.forEach {
                    println("processing unprocessed follower @$it")
                    val message = stringGenerator.getMaybeRandomFollowerMessage(MAX_TWEET_LENGTH - it.length)
                    val signature = stringGenerator.getMaybeRandomSignatureWithMaxLength(MAX_TWEET_LENGTH - message.length)
                    var str = "@$it $message $signature"

                    str = str.replace("ORIGIN_ACCOUNT", "@${config.target_account}")

                    tweet(str, twitter)

                    state.processed_follower_pool = state.processed_follower_pool.plus(it)
                    persistentState.write()

                    Sleeper.sleep(config)
                }
            } else {
                println("gathering followers with cursor" + state.next_follower_get_cursor)
                if (state.next_follower_get_cursor != 0L) {
                    val followersList = twitter.getFollowersList(config.target_account, state.next_follower_get_cursor)

                    followersList.forEach {
                        state.unprocessed_follower_pool = state.unprocessed_follower_pool.plus(it.screenName)
                    }

                    state.next_follower_get_cursor = followersList.nextCursor

                    println("next cursor " + followersList.nextCursor)
                    persistentState.write()
                }
            }

            for (status in twitter.getUserTimeline(config.target_account)) {

                state.maybeUpdateLastSeenTweetDate(status.createdAt)

                if (!state.seen_tweets_to_retweet_count.containsKey(status.id.toString())) {
                    println("new >" + status.text)

                    if (containsKeyword(status.text, config)) {
                        val payload_max_length = MAX_TWEET_LENGTH - config.target_account.length + 1
                        val payload = stringGenerator.getMaybeRandomSignatureWithMaxLength(payload_max_length)
                        val entropy = stringGenerator.getNoiseString(length = Math.min(5, payload_max_length))
                        val newStatus = "@${config.target_account} $payload $entropy"

                        tweet(newStatus, twitter)
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
                    var newStatus = "@$it " + stringGenerator.getMaybeRandomRetweetReplyWithMaxLength(MAX_TWEET_LENGTH - it.length)
                    if (newStatus.length < MAX_TWEET_LENGTH) {
                        newStatus += " " + stringGenerator.getMaybeRandomSignatureWithMaxLength(MAX_TWEET_LENGTH - newStatus.length)

                        newStatus = newStatus.replace("ORIGIN_ACCOUNT", config.target_account)
                        println("tweeting> $newStatus")
                        tweet(newStatus, twitter)
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

    private fun tweet(str: String, twitter: Twitter) {
        println("tweeting> $str")
        twitter.updateStatus(str)
    }

    private fun containsKeyword(text: String, config: Config): Boolean {
        config.target_keywords.forEach {
            if (text.toUpperCase().contains(it.toUpperCase())) {
                return true
            }
        }
        return false
    }

}
