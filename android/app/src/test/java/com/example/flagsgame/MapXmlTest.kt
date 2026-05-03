package com.example.flagsgame

import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MapXmlTest {
    private fun loadCodesForContinent(continent: String): List<String> {
        val countriesFile = File("src/main/assets/countries.json")
        if (!countriesFile.exists()) fail("countries.json not found at src/main/assets/countries.json")
        val text = countriesFile.readText()
        val countries = CountriesLoader.loadFromJsonString(text)
        val codes = countries.filter { it.continent.equals(continent, ignoreCase = true) }
            .map { CountryOnMap(it) }
            .flatMap { it.getCodes() }
        if (codes.isEmpty()) fail("No $continent countries found in countries.json")
        return codes
    }

    private fun pathNamesFromXml(xmlPath: String): Set<String> {
        val xmlFile = File(xmlPath)
        if (!xmlFile.exists()) fail("$xmlPath not found")
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(xmlFile)
        doc.documentElement.normalize()
        val nodeList = doc.getElementsByTagName("path")
        val names = mutableSetOf<String>()
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node is Element) {
                val nsName = node.getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                if (!nsName.isNullOrBlank()) {
                    names.add(nsName)
                } else {
                    val alt = node.getAttribute("android:name")
                    if (!alt.isNullOrBlank()) names.add(alt)
                }
            }
        }
        return names
    }

    private fun assertXmlContainsAll(continent: String, xmlPath: String) {
        val expected = loadCodesForContinent(continent)
        val names = pathNamesFromXml(xmlPath)
        val missing = expected.filter { it !in names }
        if (missing.isNotEmpty()) {
            fail("$xmlPath is missing paths for codes: ${missing.joinToString(", ")}")
        }
    }

    @Test
    fun europeXml_containsAllEuropeanCodes() {
        assertXmlContainsAll("europe", "src/main/res/drawable/europe.xml")
    }

    @Test
    fun asiaXml_containsAllAsianCodes() {
        assertXmlContainsAll("asia", "src/main/res/drawable/asia.xml")
    }
}
