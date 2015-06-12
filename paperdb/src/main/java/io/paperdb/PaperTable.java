package io.paperdb;

class PaperTable<T> {

    @SuppressWarnings("UnusedDeclaration") PaperTable() {
    }

    PaperTable(T content) {
        mContent = content;
    }

    // Serialized content
    T mContent;
}
