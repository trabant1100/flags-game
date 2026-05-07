package com.example.flagsgame

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class CountriesSchemaValidationTest {

    private val mapper = ObjectMapper()

    @Test
    fun countriesJson_matchesSchema() {
        val schemaFile = File("src/main/assets/countries.schema.json")
        assert(schemaFile.exists()) { "Schema file not found: ${'$'}{schemaFile.path}" }

        val countriesFile = File("src/main/assets/countries.json")
        assert(countriesFile.exists()) { "countries.json not found: ${'$'}{countriesFile.path}" }

        val schemaNode: JsonNode = mapper.readTree(schemaFile)
        val dataNode: JsonNode = mapper.readTree(countriesFile)

        val schemaFactory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        val schema: JsonSchema = schemaFactory.getSchema(schemaNode)

        val validationMessages = schema.validate(dataNode)
        if (validationMessages.isNotEmpty()) {
            val msgs = validationMessages.joinToString("; ") { "${it.path}: ${it.message}" }

            // try to extract offending array indices from message paths like $[12]
            val indexRegex = Regex("\\$\\[(\\d+)\\]")
            val indices = validationMessages.mapNotNull {
                val p = it.path ?: ""
                indexRegex.find(p)?.groupValues?.get(1)?.toInt()
            }.distinct()

            if (indices.isNotEmpty() && dataNode.isArray) {
                val snippets = indices.joinToString("\t\n") { idx ->
                    val node = dataNode.get(idx)
                    "$idx -> ${node?.toString() ?: "null"}"
                }
                fail("countries.json does not match schema: $msgs;\noffending entries:\n$snippets")
            } else {
                fail("countries.json does not match schema: $msgs")
            }
        }
    }
}
