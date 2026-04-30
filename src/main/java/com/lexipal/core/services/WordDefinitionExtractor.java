package com.lexipal.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.lexipal.core.entities.Word;

public final class WordDefinitionExtractor
{
    private WordDefinitionExtractor()
    {

    }

    public static String extractFirstDefinition(Word word)
    {
        if (word == null)
        {
            return null;
        }

        return extractFirstDefinition(word.getDefinitions());
    }

    public static String extractFirstDefinition(JsonNode definitions)
    {
        if (definitions == null)
        {
            return null;
        }

        if (definitions.isArray() && !definitions.isEmpty())
        {
            JsonNode first = definitions.get(0);
            String definition = extractDefinitionFromNode(first);

            if (hasText(definition))
            {
                return definition;
            }
        }

        if (definitions.isTextual() && hasText(definitions.asText()))
        {
            return definitions.asText();
        }

        return null;
    }

    public static String extractPreview(JsonNode definitions)
    {
        String definition = extractFirstDefinition(definitions);

        if (hasText(definition))
        {
            return definition;
        }

        if (definitions == null)
        {
            return null;
        }

        return definitions.toString();
    }

    private static String extractDefinitionFromNode(JsonNode node)
    {
        if (node == null)
        {
            return null;
        }

        JsonNode sensesNode = node.get("senses");
        if (sensesNode != null && sensesNode.isArray() && !sensesNode.isEmpty())
        {
            JsonNode firstSense = sensesNode.get(0);
            if (firstSense != null)
            {
                JsonNode definitionNode = firstSense.get("definition");
                if (definitionNode != null && definitionNode.isTextual())
                {
                    return definitionNode.asText();
                }
            }
        }

        if (node.isTextual())
        {
            return node.asText();
        }

        JsonNode definitionNode = node.get("definition");
        if (definitionNode != null && definitionNode.isTextual())
        {
            return definitionNode.asText();
        }

        return null;
    }

    private static boolean hasText(String value)
    {
        return value != null && !value.isBlank();
    }
}
