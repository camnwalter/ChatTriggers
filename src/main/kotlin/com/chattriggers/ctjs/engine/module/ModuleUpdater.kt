package com.chattriggers.ctjs.engine.module

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.Reference
import com.chattriggers.ctjs.minecraft.libs.ChatLib
import com.chattriggers.ctjs.printToConsole
import com.chattriggers.ctjs.utils.Config
import com.chattriggers.ctjs.utils.console.LogType
import com.chattriggers.ctjs.utils.kotlin.toVersion
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

object ModuleUpdater {
    private val changelogs = mutableListOf<ModuleMetadata>()
    private var shouldReportChangelog = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        shouldReportChangelog = true
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Text) {
        if (!shouldReportChangelog) return
        changelogs.forEach(::reportChangelog)
        changelogs.clear()
    }

    private fun tryReportChangelog(module: ModuleMetadata) {
        if (shouldReportChangelog) {
            reportChangelog(module)
        } else {
            changelogs.add(module)
        }
    }

    private fun reportChangelog(module: ModuleMetadata) {
        ChatLib.chat("&a[ChatTriggers] ${module.name} has updated to version ${module.version}")
        ChatLib.chat("&aChangelog: &r${module.changelog}")
    }

    fun updateModule(module: Module) {
        if (!Config.autoUpdateModules) return

        val metadata = module.metadata

        try {
            if (metadata.name == null) return

            "Checking for update in ${metadata.name}".printToConsole()

            val url = "${CTJS.WEBSITE_ROOT}/api/modules/${metadata.name}/metadata?modVersion=${Reference.MODVERSION}"
            val connection = CTJS.makeWebRequest(url)

            val newMetadataText = connection.getInputStream().bufferedReader().readText()
            val newMetadata = CTJS.gson.fromJson(newMetadataText, ModuleMetadata::class.java)

            if (newMetadata.version == null) {
                ("Remote version of module ${metadata.name} has no version numbers, so it will " +
                        "not be updated!").printToConsole(logType = LogType.WARN)
                return
            } else if (metadata.version != null && metadata.version.toVersion() >= newMetadata.version.toVersion()) {
                return
            }

            ModuleManager.downloadModule(metadata.name)
            "Updated module ${metadata.name}".printToConsole()

            module.metadata = File(module.folder, "metadata.json").let {
                CTJS.gson.fromJson(it.readText(), ModuleMetadata::class.java)
            }

            if (Config.moduleChangelog && module.metadata.changelog != null) {
                tryReportChangelog(module.metadata)
            }
        } catch (e: Exception) {
            "Can't find page for ${metadata.name}".printToConsole(logType = LogType.WARN)
        }
    }
}
