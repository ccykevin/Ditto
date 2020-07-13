package com.kevincheng.gradle

import com.kevincheng.gradle.util.DimensGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

class DittoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, DittoExtension::class.java, project)

        project.tasks.create("generateDimens") { task ->
            task.doLast {
                DimensGenerator(project).generateDimens(extension.dimens)
            }
        }.apply { group = GROUP_NAME }

        project.tasks.create("generateSwDimens") { task ->
            task.doLast {
                DimensGenerator(project).generateSwDimens(extension.designs)
            }
        }.apply { group = GROUP_NAME }
    }

    companion object {
        private const val EXTENSION_NAME = "ditto"
        private const val GROUP_NAME = "ditto"
    }
}