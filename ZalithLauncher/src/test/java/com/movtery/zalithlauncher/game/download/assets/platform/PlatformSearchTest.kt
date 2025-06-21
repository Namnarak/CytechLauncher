package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModCategory
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchRequest
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ProjectTypeFacet
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.VersionFacet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.file.Paths

class PlatformSearchJsonTest {

    @Test
    fun testSearchWithCurseforge() = runBlocking {
        val request = CurseForgeSearchRequest(
            sortField = PlatformSortField.POPULARITY,
            categories = setOf(
                CurseForgeModCategory.FOOD,
                CurseForgeModCategory.MAGIC
            ),
            gameVersion = "1.21.4",
            modLoader = CurseForgeModLoader.FABRIC,
            index = 0,
            pageSize = 50
        )

        val result = PlatformSearch.searchWithCurseforge(request)
        assertNotNull(result)
        result.let { r ->
            r.data.forEach { project ->
                println("id = ${project.id}")
                println("classId = ${project.classId}")
                println("name = ${project.name}")
                println("categories = ${project.categories.joinToString(",") { it.id.toString() }}")
                println("-----------")
            }
            println("index = ${r.pagination.index}")
            println("resultCount = ${r.pagination.resultCount}")
            println("totalCount = ${r.pagination.totalCount}")
        }
    }

    @Test
    fun testSearchWithModrinth() = runBlocking(Dispatchers.IO) {
        val request = ModrinthSearchRequest(
            facets = listOf(
                ProjectTypeFacet.MOD,
                VersionFacet("1.20.1"),
                VersionFacet("1.21.4"),
                ModrinthModLoaderCategory.FABRIC,
                ModrinthModLoaderCategory.NEOFORGE,
                ModrinthModCategory.MAGIC
            ),
            offset = 0,
            limit = 20
        )
        val result = PlatformSearch.searchWithModrinth(request)
        assertNotNull(result)
        result.let { r ->
            r.hits.forEach { project ->
                println("projectID = ${project.projectId}")
                println("projectType = ${project.projectType}")
                println("title = ${project.title}")
                println("displayCategories = ${project.displayCategories?.joinToString(",")}")
                println("-----------")
            }
            println("offset = ${r.offset}")
            println("limit = ${r.limit}")
            println("totalHits = ${r.totalHits}")
        }
    }

    @Suppress("unused")
    private fun writeTestResult(fileName: String, text: String) {
        val testOutputDir = Paths.get("src", "test", "result", "com.movtery.zalithlauncher.game.download.assets.platform")
        val outputFile = testOutputDir.resolve(fileName).toFile()
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(text, Charsets.UTF_8)
    }
}
