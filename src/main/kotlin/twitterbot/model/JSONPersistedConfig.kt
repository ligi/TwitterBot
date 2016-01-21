package twitterbot.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File

class JSONPersistedConfig(state_file: File) : JSONPersisted(state_file)  {

    var config: Config? = null;
    var adapterInstance: JsonAdapter<Config>? = null;

    fun getAdapter(): JsonAdapter<Config> {
        if (adapterInstance == null) {
            adapterInstance = Moshi.Builder().build().adapter(Config::class.java)
        }

        return adapterInstance!!
    }

    override fun write() {
        val buffer = Okio.buffer(Okio.sink(file))
        getAdapter().toJson(buffer, config)
        buffer.close()

        perhapsThrowUnconfigured()
    }

    private fun perhapsThrowUnconfigured() {
        if (config!!.throw_not_configured) {
            throw RuntimeException("Please edit $file")
        }
    }

    override fun read() {
        config = getAdapter().fromJson(Okio.buffer(Okio.source(file)))
        perhapsThrowUnconfigured()
    }

    override fun reset() {
        config = Config()
    }

}
