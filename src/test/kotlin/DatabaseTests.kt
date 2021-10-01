import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*

internal class DatabaseTests {
    val oneEntryDB = Database(File("data/one_entry.json"), mutableMapOf(
        "key" to "value"
    ))

    private val standardOut = System.out
    private val stream = ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        System.setOut(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        System.setOut(standardOut)
    }

    @Test
    fun createDatabaseTestDBInForbiddenDirectory() {
        val osName = System.getProperty("os.name").lowercase()
        val forbiddenDirectory = when {
            "win" in osName -> "C:\\Windows\\System32\\"
            "linux" in osName -> "/sys/"
            "mac" in osName -> "/System/Library/Extensions/"
            else -> "/"
        }
        assertNull(createDatabase("${forbiddenDirectory}test.dat"))
    }

    @Test
    fun createDatabaseTestDBInNormalDirectory() {
        val fileObject = File("data/test.json")

        if (fileObject.exists()) {
            fileObject.delete()
        }

        assertEquals(Database(fileObject, mutableMapOf()), createDatabase(fileObject.path))

        if (fileObject.exists()) {
            fileObject.delete()
        }
    }

    @Test
    fun openDatabaseTestNonexistentDB() {
        assertNull(openDatabase("i/dont/exist"))
    }

    @Test
    fun openDatabaseTestEmptyDB() {
        val fileObject = File("data/empty.json")
        assertEquals(Database(fileObject, mutableMapOf()), openDatabase(fileObject.path))
    }

    @Test
    fun openDatabaseTestNotDB() {
        assertNull(openDatabase("src/main/kotlin/main.kt"))
        assertEquals("main.kt contains malformed data.", stream.toString().trim())
    }

    @Test
    fun openDatabaseTestNormalDB() {
        val fileObject = File("data/music.json")
        val musicDB = mutableMapOf(
            "[m801496]" to "C418 \u2013 Minecraft - Volume Alpha",
            "[r2427020]" to "Mass Of The Fermenting Dregs \u2013 Mass Of The Fermenting Dregs",
            "[m461722]" to "Macintosh Plus \u2013 Floral Shoppe",
            "[r13314830]" to "Aphex Twin \u2013 Selected Ambient Works 85-92"
        )
        assertEquals(Database(fileObject, musicDB), openDatabase(fileObject.path))
    }

    @Test
    fun moveToTestToExistingFile() {
        val tempFile = File("data/temp.json")
        if (!tempFile.exists()) {
            tempFile.createNewFile()
        }
        tempFile.printWriter().use { it.println("temporary information") }

        oneEntryDB.moveTo("data/temp.json")

        assertEquals(tempFile.absolutePath, oneEntryDB.fileObject.absolutePath)

        tempFile.delete()
    }

    @Test
    fun moveToTestToNonexistentFile() {
        val tempFile = File("data/temp.json")
        if (tempFile.exists()) {
            tempFile.delete()
        }

        oneEntryDB.moveTo("data/temp.json")

        assertEquals(tempFile.absolutePath, oneEntryDB.fileObject.absolutePath)
        assertTrue(tempFile.exists())

        tempFile.delete()
    }
}
