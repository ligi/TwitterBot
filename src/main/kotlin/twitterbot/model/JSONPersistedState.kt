package twitterbot.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File

class JSONPersistedState(state_file: File) : JSONPersisted(state_file)  {

    var state: State? = null;
    var adapterInstance: JsonAdapter<State>? = null;

    fun getAdapter(): JsonAdapter<State> {
        if (adapterInstance == null) {
            adapterInstance = Moshi.Builder().build().adapter(State::class.java)
        }

        return adapterInstance!!
    }

    override fun write() {
        val buffer = Okio.buffer(Okio.sink(file))
        getAdapter().toJson(buffer, state)
        buffer.close()
    }

    override fun read() {
        state = getAdapter().fromJson(Okio.buffer(Okio.source(file)))
    }

    override fun reset() {
        state = State()
    }

}
