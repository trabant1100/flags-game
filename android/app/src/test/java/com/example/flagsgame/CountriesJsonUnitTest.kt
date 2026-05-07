package com.example.flagsgame

import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CountriesJsonUnitTest {

    @Test
    fun countriesJson_hasRequiredFields() {
        val file = File("src/main/assets/countries.json")
        assertTrue("countries.json not found at ${file.path}", file.exists())

        val text = file.readText(Charsets.UTF_8)
        val arr = JSONArray(text)
        assertTrue("countries.json should contain at least one country", arr.length() > 0)

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val path = "countries.json[$i]"

            // code
            assertTrue("$path: missing 'code'", obj.has("code"))
            assertTrue("$path: empty 'code'", obj.optString("code", "").trim().isNotEmpty())

            // flag
            assertTrue("$path: missing 'flag'", obj.has("flag"))
            assertTrue("$path: empty 'flag'", obj.optString("flag", "").trim().isNotEmpty())

            // names
            assertTrue("$path: missing 'names'", obj.has("names"))
            val names = obj.optJSONArray("names")
            assertNotNull("$path: 'names' should be an array", names)
            assertTrue("$path: 'names' should not be empty", names!!.length() > 0)

            // capitals or capital
            val hasCapitals = obj.has("capitals")
            assertTrue("$path: missing capitals'", hasCapitals)
            if (hasCapitals) {
                val caps = obj.optJSONArray("capitals")
                assertNotNull("$path: 'capitals' should be an array", caps)
                assertTrue("$path: 'capitals' should not be empty", caps!!.length() > 0)
            }

            // continent
            assertTrue("$path: missing 'continent'", obj.has("continent"))
            assertTrue("$path: empty 'continent'", obj.optString("continent", "").trim().isNotEmpty())
        }
    }
}
