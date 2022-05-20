package com.chattriggers.ctjs.browser.components

import com.chattriggers.ctjs.browser.BrowserEntry
import com.chattriggers.ctjs.browser.NearestSiblingConstraint
import com.chattriggers.ctjs.browser.pages.BrowserModuleProvider
import com.chattriggers.ctjs.browser.pages.BrowserReleaseProvider
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CopyConstraintFloat
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.markdown.MarkdownComponent
import gg.essential.vigilance.gui.VigilancePalette

class ModuleRelease(
    val module: BrowserModuleProvider,
    private val release: BrowserReleaseProvider,
) : UIContainer() {
    private val block by HighlightedBlock(
        backgroundColor = VigilancePalette.getBackground(),
        highlightColor = VigilancePalette.getBrightHighlight(),
        backgroundHoverColor = VigilancePalette.getLightBackground(),
        highlightHoverColor = VigilancePalette.getAccent(),
    ) effect ScissorEffect() childOf this

    private val text1 by UIText("Module version ").constrain {
        x = 20.pixels()
        y = 10.pixels()
        color = VigilancePalette.getMidText().toConstraint()
    } childOf block

    private val text2 by UIText("v${release.releaseVersion}").constrain {
        x = NearestSiblingConstraint()
        y = CopyConstraintFloat() boundTo text1
        color = VigilancePalette.getBrightText().toConstraint()
    } childOf block

    private val text3 by UIText(" for ct version ").constrain {
        x = NearestSiblingConstraint()
        y = CopyConstraintFloat() boundTo text1
        color = VigilancePalette.getMidText().toConstraint()
    } childOf block

    private val text4 by UIText("v${release.modVersion}").constrain {
        x = NearestSiblingConstraint()
        y = CopyConstraintFloat() boundTo text1
        color = VigilancePalette.getBrightText().toConstraint()
    } childOf block

    private val changelogTitle by UIText("Changelog:").constrain {
        x = 20.pixels()
        y = NearestSiblingConstraint(10f)
        color = VigilancePalette.getMidText().toConstraint()
    }

    private val changelog by MarkdownComponent(release.changelog, BrowserEntry.markdownConfig).constrain {
        x = 30.pixels()
        y = NearestSiblingConstraint(10f)
        width = 100.percent() - 60.pixels()
        color = VigilancePalette.getMidText().toConstraint()
    }

    init {
        constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        }

        if (release.changelog.isNotBlank()) {
            changelogTitle childOf block
            changelog childOf block
        }

        block.constrain {
            height = basicHeightConstraint { text1.getBottom() - text1.getTop() + 20f }
        }
    }

    override fun afterInitialization() {
        super.afterInitialization()

        // We need the markdown component to lay itself out before we constrain
        // the block, otherwise the markdown component will be outside the
        // block bounds and will never be drawn
        if (release.changelog.isNotBlank()) {
            Window.enqueueRenderOperation {
                block.constrain {
                    height = basicHeightConstraint {
                        val top = text1.getTop()
                        val bottom = changelog.getBottom()
                        bottom - top + 20f
                    }
                }
            }
        }
    }
}
