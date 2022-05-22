package com.chattriggers.ctjs.browser

import com.chattriggers.ctjs.browser.components.TextInput
import com.chattriggers.ctjs.browser.pages.AllModulesPage
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.state
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.NumberComponent
import gg.essential.vigilance.gui.settings.SelectorComponent

class BrowserSettings(private val reloadModules: () -> Unit) : UIContainer() {
    var search by state("")
    var filteredTags by state(emptyList<String>())
    var sort by state(Sort.DateNewestToOldest)
    var filter by state(Filter.All)

    private val content by UIContainer().constrain {
        x = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = 100.percent()
    } childOf this

    private val searchSetting by Setting("Search Modules") childOf content
    private val searchInput by TextInput().constrain {
        width = 20.percentOfWindow().coerceAtLeast(100.pixels())
    } childOf searchSetting

    // TODO: Combobox for filteredTagsInput
    private val sortSetting by Setting("Module Sorting") childOf content
    private val sortInput by SelectorComponent(0, Sort.values().map { it.display }) childOf sortSetting

    private val filterSetting by Setting("Module Filter") childOf content
    private var filterInput by SelectorComponent(
        0,
        Filter.values().filter { it.ordinal < 2 }.map { it.display }
    ) childOf filterSetting

    private val pageSetting by Setting("Page") childOf content
    private var pageInput by NumberComponent(1, 1, 10000, 1) childOf pageSetting

    init {
        var stopSearchDelay: () -> Unit = {}

        searchInput.onChange {
            search = it
            stopSearchDelay.invoke()
            stopSearchDelay = searchInput.delay(1000L) {
                AllModulesPage.page = 0
                reloadModules()
            }
        }

        sortInput.onValueChange {
            sort = Sort.values()[it as Int]
            reloadModules()
        }

        filterInput.onValueChange {
            filter = Filter.values()[it as Int]
            AllModulesPage.page = 0
            reloadModules()
        }

        ModuleBrowser.isLoggedIn.onSetValue { loggedIn ->
            filter = Filter.All
            reloadModules()

            filterSetting.removeChild(filterInput)
            filterInput = SelectorComponent(
                0,
                Filter.values().let { values ->
                    when {
                        ModuleBrowser.rank.get() != WebsiteOwner.Rank.Default -> values.toList()
                        loggedIn -> values.filter { it != Filter.Flagged }
                        else -> values.filter { it.ordinal < 2 }
                    }
                }.map { it.display }
            ) childOf filterSetting
            filterInput.onValueChange {
                filter = Filter.values()[it as Int]
                AllModulesPage.page = 0
                reloadModules()
            }
        }

        AllModulesPage.totalModules.onSetValue { total ->
            val totalPages = if (total % 10 == 0) total / 10 else total / 10 + 1

            pageSetting.removeChild(pageInput)
            pageInput = NumberComponent(1, 1, totalPages, 1) childOf pageSetting
            pageInput.onValueChange {
                AllModulesPage.page = it as Int - 1
                reloadModules()
            }
        }

        pageInput.onValueChange {
            AllModulesPage.page = it as Int - 1
            reloadModules()
        }
    }

    private class Setting(name: String) : UIContainer() {
        init {
            constrain {
                x = NearestSiblingConstraint(20f)
                width = ChildBasedMaxSizeConstraint()
                height = 100.percent()
            }
        }

        private val title by UIText(name, shadow = false).constrain {
            color = VigilancePalette.getMidText().toConstraint()
        } childOf this

        override fun addChild(component: UIComponent) = apply {
            if (component !is UIText) {
                component.constrain {
                    x = 1.pixel()
                    y = NearestSiblingConstraint(5f)
                }
            }

            super.addChild(component)
        }
    }

    enum class Sort(val display: String, val apiValue: String) {
        DateNewestToOldest("Date (Newest to Oldest)", "DATE_CREATED_DESC"),
        DateOldestToNewest("Date (Oldest to Newest)", "DATE_CREATED_ASC"),
        DownloadsHighToLow("Downloads (High to Low)", "DOWNLOADS_DESC"),
        DownloadsLowToHigh("Downloads (Low to High)", "DOWNLOADS_ASC"),
    }

    enum class Filter(val display: String) {
        All("All Modules"),
        Trusted("Trusted Modules"),
        User("My Modules"),
        Flagged("Flagged Modules"),
    }

    enum class ModuleTag(val display: String) {
        Utility("Utility"),
        Library("Library"),
        Singleplayer("Singleplayer"),
        Multiplayer("Multiplayer"),
        GUI("GUI"),
        HUD("HUD"),
        Audio("Audio"),
        API("API"),
        OnePointEight("1.8"),
        OnePointTwelve("1.12"),
        ProofOfConcept("Proof of Concept"),
        Hypixel("Hypixel"),
        Mineplex("Mineplex"),
        Hive("Hive"),
        ASM("ASM"),
        Skyblock("Skyblock"),
    }
}
