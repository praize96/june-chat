public class TestHolder {

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("BeforeSuite");
    }

    @Test(value = 1)
    public void test1() {
        System.out.println("test1, value=1");
    }

    @Test
    public void test2() {
        System.out.println("test2, value=5");
    }

    @Test(value = 10)
    public void test3() {
        System.out.println("test3, value=10");
    }

    @Test
    public void test4() {
        System.out.println("test4, value=5");
    }

    @Test(value = 2)
    public void test5() {
        System.out.println("test5, value=2");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("AfterSuite");
    }
}
