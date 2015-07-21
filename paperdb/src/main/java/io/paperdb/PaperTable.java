package io.paperdb;

import android.util.Log;

import java.lang.reflect.*;
import java.util.*;

import static io.paperdb.Paper.TAG;

class PaperTable<T> {

    // Serialized content
    private T mContent;
    private Map<String,String> cycleReferences = new HashMap<>();

    transient private Map<Object,String> visitedEntriesToPath = new IdentityHashMap<>();
    transient private Map<String,Object> pathToVisitedEntries = new HashMap<>();

    @SuppressWarnings("UnusedDeclaration") PaperTable() {
    }

    PaperTable(T content) {
        mContent = content;
    }

    public T getContent() {
        return mContent;
    }

    public void removeReferences() {
        try {
            doRemoveReferences(mContent, "");
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to remove cycle references", e);
        }
    }

    public void restoreReferences() {
        try {
            doRestoreReferences(mContent,"");
        }
        catch (Exception e) {
            Log.e(TAG, "Failed to restore cycle references", e);
        }
    }

    private void doRemoveReferences(Object entry,String trace) throws Exception {
        trace += (trace.isEmpty() ? "" : "-") + entry.getClass().getName();
        visitedEntriesToPath.put(entry,trace);
        Field[] fields = entry.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(entry);
            if (fieldValue==null)
                continue;
            if (visitedEntriesToPath.containsKey(fieldValue)) {
                cycleReferences.put(trace + "-" + field.getName(),visitedEntriesToPath.get(entry));
                nullCycleReference(entry, field);
                continue;
            }
            if (isNotSystemReference(fieldValue)) {
                doRemoveReferences(fieldValue, trace + "-" + field.getName());
            }
        }
    }

    private void doRestoreReferences(Object entry,String trace) throws Exception {
        trace += (trace.isEmpty() ? "" : "-") + entry.getClass().getName();
        pathToVisitedEntries.put(trace,entry);
        Field[] fields = entry.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(entry);
            if (cycleReferences.containsKey(trace + "-" + field.getName())) {
                String relation = cycleReferences.get(trace + "-" + field.getName());
                restoreCyclicReference(pathToVisitedEntries.get(relation), field);
                continue;
            }
            if (fieldValue==null)
                continue;
            if (isNotSystemReference(fieldValue)) {
                doRestoreReferences(fieldValue, trace + "-" + field.getName());
            }
        }
    }

    private boolean isNotSystemReference(Object entry) throws IllegalAccessException {
        return !entry.getClass().getCanonicalName().startsWith("java");
    }

    private void nullCycleReference(Object entry, Field field) throws Exception {
        Method setterMethod = getSetterMethod(entry, field);
        setterMethod.invoke(entry, new Object[] {null});
    }

    private void restoreCyclicReference(Object entry, Field field) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method setterMethod = getSetterMethod(entry, field);
        setterMethod.invoke(entry, entry);
    }

    private Method getSetterMethod(Object entry, Field field) throws NoSuchMethodException {
        String fieldName = field.getName();
        String fieldNameWithFirstCharacterToUpperCase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return entry.getClass().getDeclaredMethod("set" + fieldNameWithFirstCharacterToUpperCase, field.getType());
    }
}
