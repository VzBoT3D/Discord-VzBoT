import vzbot.VzBot
import java.io.File
import java.net.URLDecoder

fun main() {
    VzBot(LocationGetter().getLocation().absolutePath)
}


class LocationGetter() {
    fun getLocation(): File {
        val url = javaClass.protectionDomain.codeSource.location;
        val jarFileLocation = File(url.path).parentFile;
        val path = URLDecoder.decode(jarFileLocation.absolutePath, "UTF-8");
        return File(path);
    }
}

