package io.paperdb;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@DefaultSerializer(CompatibleFieldSerializer.class)
public class Person implements Serializable {
    public String mName;
    public int mAge;
    public List<String> mPhoneNumbers;
    public String[] mBikes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Person.class != o.getClass()) return false;

        Person person = (Person) o;

        if (mAge != person.mAge) return false;
        if (!Arrays.equals(mBikes, person.mBikes)) return false;
        if (mName != null ? !mName.equals(person.mName) : person.mName != null) return false;
        //noinspection RedundantIfStatement
        if (mPhoneNumbers != null ? !mPhoneNumbers.equals(person.mPhoneNumbers) : person.mPhoneNumbers != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + mAge;
        result = 31 * result + (mPhoneNumbers != null ? mPhoneNumbers.hashCode() : 0);
        result = 31 * result + (mBikes != null ? Arrays.hashCode(mBikes) : 0);
        return result;
    }
}
