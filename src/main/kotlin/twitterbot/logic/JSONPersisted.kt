package twitterbot.logic

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File

class JSONPersisted<T : Any>(val file: File, val default: T) {

    var content: T
    val adapter: JsonAdapter<T>

    init {
        content = default
        adapter = Moshi.Builder().build().adapter(default.javaClass)
        if (!file.exists()) {
            println("$file does not exist -> creating one")
            file.createNewFile()
            write()
        } else {
            println("found $file -> reading")
            read()
        }

    }

    fun read() {
        content = adapter.fromJson(Okio.buffer(Okio.source(file)))
    }

    fun write() {
        val buffer = Okio.buffer(Okio.sink(file))
        adapter.toJson(buffer, content)
        buffer.close()
    }

    fun get(): T {
        return content
    }

}
