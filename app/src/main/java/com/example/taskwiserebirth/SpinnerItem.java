package com.example.taskwiserebirth;

public class SpinnerItem {
    private String text;
    private boolean hint;

    public SpinnerItem(String text, boolean hint) {
        this.text = text;
        this.hint = hint;
    }

    public String getText() {
        return text;
    }

    public boolean isHint() {
        return hint;
    }
}


