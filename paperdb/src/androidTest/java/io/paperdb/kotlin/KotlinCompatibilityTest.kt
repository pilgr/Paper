package io.paperdb.io.paperdb

import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.paperdb.Paper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KotlinCompatibilityTest{

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Paper.init(getTargetContext())
        Paper.book().destroy()
    }

    @Test
    fun testNormalClasses() {
        class PersonNormalClass(val name: String = "name", val age: Int){
            override fun equals(other: Any?): Boolean{
                if (this === other) return true
                if (other?.javaClass != javaClass) return false

                other as PersonNormalClass

                if (name != other.name) return false
                if (age != other.age) return false

                return true
            }

            override fun hashCode(): Int{
                var result = name.hashCode()
                result += 31 * result + age
                return result
            }
        }

        testReadWrite(PersonNormalClass("name", age = 42))
        testReadWrite(PersonNormalClass(age = 42))
    }

    @Test
    fun testDataClasses() {
        data class PersonDataClass(val name: String = "name", val age: Int)

        testReadWrite(PersonDataClass("Julia", age = 42))
        testReadWrite(PersonDataClass(age = 42))
    }

    @Test
    fun testClassesWithLambda() {
        class PersonWithLambda(val block: (PersonWithLambda.() -> Unit)? = null) {
            init {
                block?.invoke(this)
            }

            var name: String? = null

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other?.javaClass != javaClass) return false

                other as PersonWithLambda

                if (name != other.name) return false

                return true
            }

            override fun hashCode(): Int {
                return name?.hashCode() ?: 0
            }

        }

        testReadWrite(PersonWithLambda())
        testReadWrite(PersonWithLambda().apply { name = "new-name" })
        testReadWrite(PersonWithLambda { name = "new-name" })
    }

    private fun testReadWriteWithoutClassCheck(originObj: Any): Any {
        Paper.book().write("obj", originObj)
        val readObj = Paper.book().read<Any>("obj")
        assertThat(readObj).isEqualTo(originObj)
        return readObj
    }

    private fun testReadWrite(originObj: Any) {
        val readObj = testReadWriteWithoutClassCheck(originObj)
        assertThat(readObj.javaClass).isEqualTo(originObj.javaClass)
    }

}
