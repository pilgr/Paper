package io.paperdb.testdata;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDataGenerator {
    public static List<Person> genPersonList(int size) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Person p = genPerson(i);
            list.add(p);
        }
        return list;
    }

    @NonNull
    public static Person genPerson(int i) {
        Person p = new Person();
        p.setAge(i);
        p.setBikes(new String[2]);
        p.getBikes()[0] = "Kellys gen#" + i;
        p.getBikes()[1] = "Trek gen#" + i;
        p.setPhoneNumbers(new ArrayList<String>());
        p.getPhoneNumbers().add("0-KEEP-CALM" + i);
        p.getPhoneNumbers().add("0-USE-PAPER" + i);
        return p;
    }

    public static Map<Integer, Person> genPersonMap(int size) {
        HashMap<Integer, Person> map = new HashMap<>();
        int i = 0;
        for (Person person : genPersonList(size)) {
            map.put(i++, person);
        }
        return map;
    }
}
