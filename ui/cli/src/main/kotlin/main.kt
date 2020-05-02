import com.sights_detect.core.controllers.DesktopController
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Thread.sleep
import java.security.InvalidParameterException
import java.util.*

class Main(propsPath: String) {
    var properties: Properties = Properties()
    init {
        if (!File(propsPath).exists())
            throw InvalidParameterException("The given path $propsPath isn't file, it's directory")
        if (!File(propsPath).isFile)
            throw InvalidParameterException("The given properties file $propsPath doesn't exist")
        try {
            properties.load(FileInputStream(propsPath))
        }
        catch (e: IOException) { throw InvalidParameterException("Can't load properties from $propsPath: $e") }
    }
}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        try {
            val controller = DesktopController(listOf(), Main(args.last()).properties)
            controller.start()
            sleep(10000)
            val stats = controller.getStatistics()
            println(stats.getFoundPicsNum())
        } catch (e: InvalidParameterException) {
            println("ERROR: $e")
        }
    }
    else
        println("ERROR: There are no parameters. Should be: dir1, dir2, ..., properties file path")
}