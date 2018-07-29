package de.weimarnetz;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

/** Injects the @Mock objects into the @InjectMock class under test. */
public abstract class MockitoTest {
    @Before
    public void injectMocks() throws IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        checkThatAllInjectedOrAutowiredFieldsAreNonNullInClassUnderTest();
    }

    private void checkThatAllInjectedOrAutowiredFieldsAreNonNullInClassUnderTest() throws IllegalAccessException {
        List<String> nullFieldsInClassUnderTest = new ArrayList<>();
        Field[] testsFields = getClass().getDeclaredFields();
        for (Field testsField : testsFields) {
            Annotation objectUnderTest = testsField.getAnnotation(InjectMocks.class);
            if (objectUnderTest != null) {
                ReflectionUtils.makeAccessible(testsField);
                Field[] objectUnderTestsFields = testsField.get(this).getClass().getDeclaredFields();
                for (Field objectUnderTestsField : objectUnderTestsFields) {
                    Autowired autowiredField = objectUnderTestsField.getAnnotation(Autowired.class);
                    // change to javax.inject as soon as it will be present in classpath
                    Inject injectedField = objectUnderTestsField.getAnnotation(Inject.class);
                    if ((autowiredField != null && autowiredField.required()) || injectedField != null) {
                        ReflectionUtils.makeAccessible(objectUnderTestsField);
                        Object injectedOrAutowiredFieldObject = ReflectionUtils.getField(objectUnderTestsField,
                                testsField.get(this));
                        if (injectedOrAutowiredFieldObject == null) {
                            nullFieldsInClassUnderTest.add(objectUnderTestsField.getName());
                        }
                    }
                }
                break; // there is only one @InjectMocks-annotated field
            }
        }
        assertThat("Found null field(s) in class under test!", nullFieldsInClassUnderTest, is(empty()));
    }
}
