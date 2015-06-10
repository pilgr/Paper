package io.paperdb;

class PaperTable<T> {

    @SuppressWarnings("UnusedDeclaration") PaperTable() {
    }

    PaperTable(T content) {
        this.mContent = content;
    }

    // Serialized content
    T mContent;
}
