import com.sights_detect.core.controllers.DesktopController
import com.sights_detect.core.statistics.Statistics
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.InvalidParameterException
import java.util.*
import kotlin.system.exitProcess

class Main(args: Array<String>) {
    val properties: Properties = Properties()
    var dirsPaths: List<String> = listOf()
    init {
        initProps(args)
        dirsPaths = args.slice(0.. args.size - 2)
    }

    private fun initProps(args: Array<String>) {
        val propsPath = args.last()
        if (!File(propsPath).exists())
            throw InvalidParameterException("The given path $propsPath isn't file, it's directory")
        if (!File(propsPath).isFile)
            throw InvalidParameterException("The given properties file $propsPath doesn't exist")
        try {
            properties.load(FileInputStream(propsPath))
        } catch (e: IOException) {
            throw InvalidParameterException("Can't load properties from $propsPath: $e")
        }
    }
}

var stats: Statistics? = null

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args.size > 1) {
        try {
            val controller = DesktopController(Main(args).dirsPaths, Main(args).properties)
            runBlocking {
                controller.start()
            }
            stats = controller.getStatistics()
            return
        } catch (e: InvalidParameterException) {
            System.err.println("ERROR: $e")
        }
    }
    else
        System.err.println("ERROR: Invalid parameters number. Should be: dir1, dir2, ..., properties file path")
    exitProcess(1)
}