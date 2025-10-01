package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeProject(
    @SerialName("data")
    val data: CurseForgeData
): PlatformProject {
    override fun platform(): Platform = Platform.CURSEFORGE

    override fun platformId(): String = data.id.toString()

    override fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses {
        return data.classId?.platform ?: defaultClasses
    }

    override fun platformSlug(): String = data.slug

    override fun platformIconUrl(): String? = data.logo.url

    override fun platformTitle(): String = data.name

    override fun platformSummary(): String? = data.summary

    override fun platformAuthor(): String? = data.authors[0].name

    override fun platformDownloadCount(): Long = data.downloadCount

    override fun platformUrls(defaultClasses: PlatformClasses): PlatformProject.Urls {
        val classes = data.classId?.platform ?: defaultClasses
        return PlatformProject.Urls(
            projectUrl = "https://www.curseforge.com/minecraft/${classes.curseforge.slug}/${data.slug}",
            sourceUrl = data.links.sourceUrl,
            issuesUrl = data.links.issuesUrl,
            wikiUrl = data.links.wikiUrl
        )
    }

    override fun platformScreenshots(): List<PlatformProject.Screenshot> {
        return data.screenshots.map { asset ->
            PlatformProject.Screenshot(
                imageUrl = asset.url,
                title = asset.title,
                description = asset.description
            )
        }
    }
}