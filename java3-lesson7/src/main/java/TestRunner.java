import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestRunner {

    public static void start (String className) {
        try {
            Class testClass = Class.forName(className);
            Object testObject = testClass.newInstance();
            Method[] methods = testClass.getDeclaredMethods();
            List<Method> testMethodsList = new ArrayList<>();
            for (Method method : methods) {
                if (method.getDeclaredAnnotation(Test.class) != null) {
                    testMethodsList.add(method);
                }
            }
            Collections.sort(testMethodsList, (Method one, Method other) -> {
                if (one.getDeclaredAnnotation(Test.class).value() < other.getDeclaredAnnotation(Test.class).value()) {
                    return 1;
                } else return -1;
            });
            for (Method method : methods) {
                if (method.getDeclaredAnnotation(BeforeSuite.class) != null) {
                    if (testMethodsList.contains(method.getDeclaredAnnotation(BeforeSuite.class))) {
                        throw new RuntimeException();
                    } else testMethodsList.add(0, method);
                }
                if (method.getDeclaredAnnotation(AfterSuite.class) != null) {
                    if (testMethodsList.contains(method.getDeclaredAnnotation(AfterSuite.class))) {
                        throw new RuntimeException();
                    } else testMethodsList.add(testMethodsList.size(), method);
                }
            }
            for (Method meth : testMethodsList) {
                meth.invoke(testObject);
            }
        } catch (RuntimeException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        start("TestHolder");
    }
}
