package com.kevincheng.gradle.util

import com.kevincheng.gradle.DittoExtension
import org.gradle.api.Project
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.PrintWriter
import java.math.BigDecimal
import java.math.RoundingMode
import javax.xml.parsers.DocumentBuilderFactory

class DimensGenerator(project: Project) {

    private val resources = File("${project.buildFile.parent}\\src\\main\\res\\")
    private val valuesDirRegex = "([\\w-.]+)".toRegex()
    private val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    private val dpUnit = "dp"
    private val spUnit = "sp"
    private val positiveFormat: String = "%1\$s_%2\$s"
    private val negativeFormat: String = "%1\$s_minus_%2\$s"

    fun generateDimens(dimens: ArrayList<DittoExtension.Dimens>) {
        dimens.forEach {
            if (it.filePath.isEmpty()) throw UnknownError("output file path is not configured.")

            val dimensMap: LinkedHashMap<String, String> = LinkedHashMap()
            dimensMap.createDimens(it.minDp..it.maxDp, dpUnit)
            dimensMap.createDimens(it.minSp..it.maxSp, spUnit)

            it.filePath.forEach { path ->
                val info = valuesDirRegex.findAll(path)
                    .map { result -> result.groupValues[1] }
                    .toList()
                val valuesDirName = info.first()
                val dimensFileName = info.last()
                val xml = createXmlFile(valuesDirName, dimensFileName)
                PrintWriter(xml.absoluteFile).outputNewDimens(dimensMap)
            }
        }
    }

    fun generateSwDimens(designs: ArrayList<DittoExtension.Design>) {
        designs.forEach { design ->
            if (design.sw <= 0.0) throw UnknownError("smallest width of design is incorrect/not configured.")
            if (design.dimens.isEmpty()) throw UnknownError("source of dimens is not configured.")

            design.dimens.forEach { dimensPath ->
                val dimens = File(resources, dimensPath)
                if (!dimens.exists()) throw UnknownError("$dimensPath not found.")

                val info = valuesDirRegex.findAll(dimensPath).map { it.groupValues[1] }.toList()
                val valuesDirName = info.first()
                val dimensFileName = info.last()
                val swRegex = "(sw[\\d]+dp)".toRegex()
                val dimensMap = dimens.getDimens()

                design.adaptSw.forEach { targetSW ->
                    val targetSwName = "sw${targetSW.toInt()}dp"
                    val targetSwDirName = when (valuesDirName.contains(swRegex)) {
                        true -> valuesDirName.replace(swRegex, targetSwName)
                        false -> valuesDirName.replace("values", "values-$targetSwName")
                    }
                    val targetSwXml = createXmlFile(targetSwDirName, dimensFileName)
                    PrintWriter(targetSwXml.absoluteFile).outputNewDimens(dimensMap, targetSW / design.sw)
                }
            }
        }
    }

    private fun LinkedHashMap<String, String>.createDimens(range: IntRange, unit: String) {
        for (value in range) {
            val name: String
            val format: String

            when (value >= 0) {
                true -> {
                    name = value.toString()
                    format = positiveFormat
                }
                false -> {
                    name = (-value).toString()
                    format = negativeFormat
                }
            }

            this[String.format(format, unit, name)] = "$value$unit"
        }
    }

    private fun File.getDimens(): LinkedHashMap<String, String> {
        val dimensMap: LinkedHashMap<String, String> = LinkedHashMap()
        val doc: Document = documentBuilderFactory.newDocumentBuilder().parse(this).also {
            it.documentElement.normalize()
        }

        val nodes = doc.getElementsByTagName("dimen")

        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                dimensMap[element.getAttribute("name")] = element.textContent
            }
        }
        return dimensMap
    }

    private fun createXmlFile(valuesDirName: String, dimensFileName: String): File {
        val dir = File(resources, valuesDirName)
        if (!dir.exists()) dir.mkdir()

        val xml = File(dir, dimensFileName)
        if (xml.exists()) xml.delete()
        xml.createNewFile()

        return xml
    }

    private fun PrintWriter.outputNewDimens(dimensMap: Map<String, String>, ratio: Double = 1.0) {
        println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        println("<resources>")
        dimensMap.forEach { (name, dimension) ->
            if (dimension.endsWith(dpUnit) || dimension.endsWith(spUnit)) {
                val unit = dimension.substring(dimension.length - 2, dimension.length)
                val value = BigDecimal(dimension.substring(0 until dimension.length - 2).toDouble())
                    .setScale(4, RoundingMode.DOWN).toDouble()
                val newValue = BigDecimal.valueOf(value * ratio)
                    .setScale(4, RoundingMode.DOWN)
                    .toDouble()
                println("\t<dimen name=\"$name\">$newValue$unit</dimen>")
            }
        }
        println("</resources>")
        close()
    }
}