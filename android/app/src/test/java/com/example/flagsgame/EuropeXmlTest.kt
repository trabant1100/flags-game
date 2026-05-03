package com.example.flagsgame

import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class EuropeXmlTest {

    @Test
    fun europeXml_containsAllEuropeanCodes() {
        val countriesFile = File("src/main/assets/countries.json")
        if (!countriesFile.exists()) {
            fail("countries.json not found at src/main/assets/countries.json")
        }

        val text = countriesFile.readText()
        val countries = CountriesLoader.loadFromJsonString(text)
        val countriesOnMapCodes = countries.filter { it.continent.equals("europe", ignoreCase = true) }
            .map { CountryOnMap(it) }
            .flatMap { it.getCodes() }

        if (countriesOnMapCodes.isEmpty()) fail("No european countries found in countries.json")

        val europeXmlFile = File("src/main/res/drawable/europe.xml")
        if (!europeXmlFile.exists()) {
            fail("europe.xml not found at src/main/res/drawable/europe.xml")
        }

        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(europeXmlFile)
        doc.documentElement.normalize()

        val nodeList = doc.getElementsByTagName("path")
        val names = mutableSetOf<String>()
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node is Element) {
                // android:name is in the Android namespace
                val nsName = node.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                if (!nsName.isNullOrBlank()) {
                    names.add(nsName)
                } else {
                    val alt = node.getAttribute("android:name")
                    if (!alt.isNullOrBlank()) names.add(alt)
                }
            }
        }

        val missing = countriesOnMapCodes.filter { it !in names }
        if (missing.isNotEmpty()) {
            fail("europe.xml is missing paths for codes: ${missing.joinToString(", ")}")
        }
    }
}
