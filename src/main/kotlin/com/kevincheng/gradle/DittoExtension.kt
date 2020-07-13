package com.kevincheng.gradle

import groovy.lang.Closure
import org.gradle.api.Project
import javax.inject.Inject

open class DittoExtension @Inject constructor(private val project: Project) {
    var dimens: ArrayList<Dimens> = arrayListOf()
    var designs: ArrayList<Design> = arrayListOf()

    fun dimens(closure: Closure<Design>): Dimens {
        val dimens: Dimens = project.configure(Dimens(), closure) as Dimens
        this.dimens.add(dimens)
        return dimens
    }

    fun design(closure: Closure<Design>): Design {
        val design: Design = project.configure(Design(), closure) as Design
        designs.add(design)
        return design
    }

    open class Dimens {
        var maxNegativeDp: Int = 0
        var maxDp: Int = 800
        var maxNegativeSp: Int = 0
        var maxSp: Int = 64
        var filePath: ArrayList<String> = arrayListOf()

        val minDp: Int
            get() = when {
                maxNegativeDp > 0 -> maxNegativeDp * -1
                else -> 0
            }

        val minSp: Int
            get() = when {
                maxNegativeSp > 0 -> maxNegativeSp * -1
                else -> 1
            }

        fun maxNegativeDp(value: Int) {
            maxNegativeDp = value
        }

        fun maxDp(value: Int) {
            maxDp = value
        }

        fun maxNegativeSp(value: Int) {
            maxNegativeSp = value
        }

        fun maxSp(value: Int) {
            maxSp = value
        }

        fun filePath(vararg filePath: String) {
            this.filePath.addAll(filePath.toList().toTypedArray())
        }
    }

    open class Design {
        var sw: Double = 0.0
        var dimens: ArrayList<String> = arrayListOf()
        var adaptSw: ArrayList<Double> = arrayListOf(
            320.0,
            360.0,
            384.0,
            392.7272,
            400.0,
            410.0,
            411.4285,
            432.0,
            480.0,
            533.0,
            592.0,
            600.0,
            640.0,
            662.0,
            720.0,
            768.0,
            800.0,
            811.0,
            820.0,
            960.0,
            961.0,
            1024.0,
            1080.0,
            1280.0,
            1365.0
        )

        fun sw(smallestWidth: Double) {
            sw = smallestWidth
        }

        fun dimens(vararg files: String) {
            dimens.addAll(files)
        }

        fun adaptSw(vararg smallestWidths: Double) {
            adaptSw.clear()
            adaptSw.addAll(smallestWidths.toTypedArray())
        }
    }
}