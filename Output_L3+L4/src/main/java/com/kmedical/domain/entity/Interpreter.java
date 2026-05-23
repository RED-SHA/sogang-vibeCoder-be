package com.kmedical.domain.entity;

import com.kmedical.domain.enums.Language;

import java.util.List;

/** C06 — Interpreter «entity» extends Staff */
public class Interpreter extends Staff {

    private List<Language> languages;

    public Interpreter() { super(); }

    public List<Language> getLanguages() { return languages; }
    public void setLanguages(List<Language> languages) { this.languages = languages; }
}
