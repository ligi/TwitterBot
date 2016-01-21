package twitterbot.model

import java.util.*

open class State(var authToken: String? = null,
                 var authSecret: String? = null,
                 var seen_tweets_to_retweet_count: Map<String,Int> = HashMap(),
                 var last_seen_tweet_date: Long = Date().time,
                 var processed_retweeters: Set<String> = TreeSet()) {

    fun maybeUpdateLastSeenTweetDate(date : Date)  : Boolean {
        if (date.after(Date(last_seen_tweet_date))) {
            last_seen_tweet_date = date.time
            return true
        }
        return false
    }
}
