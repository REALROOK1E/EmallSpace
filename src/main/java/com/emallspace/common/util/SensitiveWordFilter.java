package com.emallspace.common.util;

import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class SensitiveWordFilter {
    
    // Mock Trie Tree implementation
    private Set<String> sensitiveWords = new HashSet<>();

    public SensitiveWordFilter() {
        sensitiveWords.add("badword");
        sensitiveWords.add("illegal");
    }

    public boolean containsSensitiveWord(String text) {
        // Simplified check
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
