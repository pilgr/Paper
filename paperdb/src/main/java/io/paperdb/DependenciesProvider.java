package io.paperdb;

import android.content.Context;

class DependenciesProvider {

    private static final DependenciesProvider instance = new DependenciesProvider();

    private Context applicationContext;

    static DependenciesProvider getInstance() {
        return instance;
    }

    void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    Context getApplicationContext() {
        return applicationContext;
    }
}
