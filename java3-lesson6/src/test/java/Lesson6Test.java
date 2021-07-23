import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.geekbrains.java3.lesson6.MethodsEx6;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Lesson6Test {
    public MethodsEx6 methods;

    @BeforeEach
    public void init() {
        methods = new MethodsEx6();
    }

    public static Stream<Arguments> dataForReturnMethod() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{2, 3, 2}, new int[]{0, 5, 8, 3, 4, 2, 3, 2}));
        out.add(Arguments.arguments(new int[]{3, 9, 2, 3, 2}, new int[]{4, 3, 9, 2, 3, 2}));
        out.add(Arguments.arguments(new int[]{2, 3, 2}, new int[]{1, 1, 4, 0, 5, 8, 3, 4, 2, 3, 2}));
        out.add(Arguments.arguments(new int[]{}, new int[]{0, 5, 6, 3, 4, 2, 3, 4}));
        return out.stream();
    }

    @DisplayName("ReturnMethodTest1")
    @ParameterizedTest
    @MethodSource("dataForReturnMethod")
    public void testArrayReturnMethod(int[] result, int[] array) {
        Assertions.assertArrayEquals(result, methods.returnMethod(array));
    }

    @Test
    @DisplayName("ReturnMethodTest2")
    public void checkRuntimeException() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            methods.returnMethod(new int[]{0, 5, 8, 3, 1, 2, 3, 2});
        });
    }


    public static Stream<Arguments> dataForCheckMethod() {
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(false, new int[]{0, 5, 8, 3, 4, 2, 3, 2}));
        out.add(Arguments.arguments(true, new int[]{4, 1, 4, 4, 4, 1}));
        out.add(Arguments.arguments(false, new int[]{1, 1, 1, 1, 1}));
        out.add(Arguments.arguments(false, new int[]{4, 1, 4, 1, 4, 1, 4, 5}));
        return out.stream();
    }

    @DisplayName("CheckMethodTest1")
    @ParameterizedTest
    @MethodSource("dataForCheckMethod")
    public void testArrayCheckMethod(boolean result, int[] array) {
        Assertions.assertEquals(result, methods.checkMethod(array));
    }
}