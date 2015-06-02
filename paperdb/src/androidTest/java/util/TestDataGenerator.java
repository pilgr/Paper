package util;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Person;

public class TestDataGenerator {
    public static List<Person> genPersonList(int size) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Person p = new Person();
            p.setAge(i);
            p.setBikes(new String[2]);
            p.getBikes()[0] = "Kellys";
            p.getBikes()[1] = "Trek";
            p.setPhoneNumbers(new ArrayList<String>());
            p.getPhoneNumbers().add("234092348" + i);
            p.getPhoneNumbers().add("+380-44-234234234" + i);
            list.add(p);
        }
        return list;
    }
}
