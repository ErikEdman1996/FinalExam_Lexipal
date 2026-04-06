package com.lexipal.core.entities;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words")
public class Word
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String lemma;

    @Column(name = "rarity_score")
    private Double rarityScore;

    @Column(length = 100)
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definitions", columnDefinition = "jsonb")
    private JsonNode definitions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode phonetics;

    public Word()
    {

    }

    public Word(Long id, String lemma, Double rarityScore, String source, JsonNode definitions, JsonNode phonetics)
    {
        this.id = id;
        this.lemma = lemma;
        this.rarityScore = rarityScore;
        this.source = source;
        this.definitions = definitions;
        this.phonetics = phonetics;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public Double getRarityScore() {
        return rarityScore;
    }

    public void setRarityScore(Double rarityScore) {
        this.rarityScore = rarityScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JsonNode getDefinitions() {
        return definitions;
    }

    public void setDefinitions(JsonNode definitions) {
        this.definitions = definitions;
    }

    public JsonNode getPhonetics() {
        return phonetics;
    }

    public void setPhonetics(JsonNode phonetics) {
        this.phonetics = phonetics;
    }
}
