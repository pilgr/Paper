package io.paperdb;

class PaperTable<T> {

    @SuppressWarnings("UnusedDeclaration") PaperTable() {
    }

    PaperTable(T[] content) {
        this.content = content;
    }

    T[] content;
}
