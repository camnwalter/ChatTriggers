package com.chattriggers.ctjs.engine.module

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.Reference
import com.chattriggers.ctjs.engine.ILoader
import com.chattriggers.ctjs.engine.langs.js.JSContextFactory
import com.chattriggers.ctjs.engine.langs.js.JSLoader
import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.minecraft.libs.FileLib
import com.chattriggers.ctjs.minecraft.wrappers.World
import com.chattriggers.ctjs.printTraceToConsole
import com.chattriggers.ctjs.triggers.TriggerType
import com.chattriggers.ctjs.utils.Config
import com.chattriggers.ctjs.utils.console.Console
import com.chattriggers.ctjs.utils.kotlin.toVersion
import org.apache.commons.io.FileUtils
import org.mozilla.javascript.Context
import java.io.File
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

object ModuleManager {
    private val loaders = listOf(JSLoader)
    val generalConsole = Console(null)
    val cachedModules = TreeSet<Module> { o1, o2 ->
        o1.name.compareTo(o2.name)
    }
    val modulesFolder = run {
        Config.loadData()
        File(Config.modulesFolder)
    }
    val pendingOldModules = mutableSetOf<Module>()

    private fun importPendingModules() {
        val toDownload = File(modulesFolder, ".to_download.txt")
        if (!toDownload.exists()) return

        toDownload.readText().split(",").filter(String::isBlank).forEach(::importModule)

        toDownload.delete()
    }

    fun setup() {
        modulesFolder.mkdirs()

        // Download pending modules
        importPendingModules()

        // Get existing modules
        val installedModules = getFoldersInDir(modulesFolder).map(::parseModule).distinctBy {
            it.name.lowercase()
        }

        // Check if those modules have updates
        installedModules.forEach(ModuleUpdater::updateModule)
        cachedModules.addAll(installedModules)

        // Import required modules
        installedModules.distinct().forEach { module ->
            module.metadata.requires?.forEach { getRequiredModules(it, module.name) }
        }

        // Load their assets
        loadAssets(cachedModules)

        // Normalize all metadata
        cachedModules.forEach {
            it.metadata.entry = it.metadata.entry?.let(FileLib::normalizeFilePath)
        }

        // Get all jars
        val jars = cachedModules.map { module ->
            module.folder.walk().filter {
                it.isFile && it.extension == "jar"
            }.map {
                it.toURI().toURL()
            }.toList()
        }.flatten()

        // Setup all loaders
        loaders.forEach {
            it.setup(jars)
        }
    }

    fun entryPass(modules: TreeSet<Module> = cachedModules, completionListener: (percentComplete: Float) -> Unit = {}) {
        loaders.forEach(ILoader::entrySetup)

        val total = modules.count { it.metadata.entry != null }
        var completed = 0

        // Load the modules
        loaders.forEach { loader ->
            modules.filter {
                File(it.folder, it.metadata.entry ?: return@filter false).extension == loader.getLanguage().extension
            }.forEach {
                loader.entryPass(it, File(it.folder, it.metadata.entry!!).toURI())

                completed++
                completionListener(completed.toFloat() / total)
            }
        }
    }

    private fun getFoldersInDir(dir: File): List<File> {
        if (!dir.isDirectory) return emptyList()

        return dir.listFiles()?.filter {
            it.isDirectory
        } ?: listOf()
    }

    private fun parseModule(directory: File): Module {
        val metadataFile = File(directory, "metadata.json")
        var metadata = ModuleMetadata()

        if (metadataFile.exists()) {
            try {
                metadata = CTJS.gson.fromJson(FileLib.read(metadataFile), ModuleMetadata::class.java)
            } catch (exception: Exception) {
                exception.printTraceToConsole()
            }
        }

        return Module(directory.name, metadata, directory)
    }

    data class ImportedModule(val module: Module?, val dependencies: List<Module>)

    fun importModule(moduleName: String): ImportedModule {
        val newModules = getRequiredModules(moduleName)

        // Load their assets
        loadAssets(newModules)

        // Normalize all metadata
        newModules.forEach {
            it.metadata.entry = it.metadata.entry?.let(FileLib::normalizeFilePath)
        }

        entryPass(newModules)

        return ImportedModule(newModules.first(), newModules.drop(1))
    }

    private fun getRequiredModules(moduleName: String, requiredBy: String? = null): TreeSet<Module> {
        val alreadyImported = cachedModules.any {
            if (it.name == moduleName) {
                if (requiredBy != null) {
                    it.metadata.isRequired = true
                    it.requiredBy.add(requiredBy)
                }

                true
            } else false
        }

        if (alreadyImported) return TreeSet<Module>()

        val (realName, modVersion) = downloadModule(moduleName) ?: return TreeSet<Module>()

        val moduleDir = File(modulesFolder, realName)
        val module = parseModule(moduleDir)
        module.targetModVersion = modVersion.toVersion()

        if (requiredBy != null) {
            module.metadata.isRequired = true
            module.requiredBy.add(requiredBy)
        }

        cachedModules.add(module)

        val set = TreeSet(module.metadata.requires?.flatMap {
            getRequiredModules(it, module.name)
        } ?: listOf())
        set.add(module)

        return set
    }

    data class DownloadResult(val name: String, val modVersion: String)

    fun downloadModule(name: String): DownloadResult? {
        val downloadZip = File(modulesFolder, "currDownload.zip")

        try {
            val url = "${CTJS.WEBSITE_ROOT}/api/modules/$name/scripts?modVersion=${Reference.MODVERSION}"
            val connection = CTJS.makeWebRequest(url)
            FileUtils.copyInputStreamToFile(connection.getInputStream(), downloadZip)
            FileSystems.newFileSystem(downloadZip.toPath(), null).use {
                val rootFolder = Files.newDirectoryStream(it.rootDirectories.first()).iterator()
                if (!rootFolder.hasNext()) throw Exception("Too small")
                val moduleFolder = rootFolder.next()
                if (rootFolder.hasNext()) throw Exception("Too big")

                val realName = moduleFolder.fileName.toString().trimEnd(File.separatorChar)
                File(modulesFolder, realName).apply { mkdir() }
                Files.walk(moduleFolder).forEach { path ->
                    val resolvedPath = Paths.get(modulesFolder.toString(), path.toString())
                    if (Files.isDirectory(resolvedPath)) {
                        return@forEach
                    }
                    Files.copy(path, resolvedPath, StandardCopyOption.REPLACE_EXISTING)
                }
                return DownloadResult(realName, connection.getHeaderField("CT-Version"))
            }
        } catch (exception: Exception) {
            exception.printTraceToConsole()
        } finally {
            downloadZip.delete()
        }

        return null
    }

    fun deleteModule(name: String): Boolean {
        val module = cachedModules.find { it.name.equals(name, ignoreCase = true) } ?: return false

        val file = File(modulesFolder, module.folder.name)
        if (!file.exists()) throw IllegalStateException("Expected module to have an existing folder!")

        val context = JSContextFactory.enterContext()
        try {
            val classLoader = context.applicationClassLoader as URLClassLoader

            classLoader.close()

            if (file.deleteRecursively()) {
                Reference.loadCT()
                return true
            }
        } finally {
            Context.exit()
        }

        return false
    }

    fun tryReportOldVersion(module: Module) {
        if (World.isLoaded()) {
            reportOldVersion(module)
        } else {
            pendingOldModules.add(module)
        }
    }

    fun reportOldVersion(module: Module) {
        ChatLib.chat(
            "&cWarning: the module \"${module.name}\" was made for an older version of CT, " +
                    "so it may not work correctly."
        )
    }

    private fun loadAssets(modules: Set<Module>) {
        modules.map {
            File(it.folder, "assets")
        }.filter {
            it.exists() && !it.isFile
        }.map {
            it.listFiles()?.toList() ?: emptyList()
        }.flatten().forEach {
            if (it.isFile)
                FileUtils.copyFileToDirectory(it, CTJS.assetsDir)
            else
                FileUtils.copyDirectory(it, File(CTJS.assetsDir, it.name))
        }
    }

    fun teardown() {
        cachedModules.clear()

        loaders.forEach {
            it.clearTriggers()

            if (Config.clearConsoleOnLoad) {
                it.console.clearConsole()
            }
        }

        if (Config.clearConsoleOnLoad)
            generalConsole.clearConsole()
    }

    fun trigger(type: TriggerType, arguments: Array<out Any?>) {
        loaders.forEach {
            it.exec(type, arguments)
        }
    }

    fun getConsole(language: String): Console {
        return loaders.firstOrNull {
            it.getLanguage().langName == language
        }?.console ?: generalConsole
    }
}
