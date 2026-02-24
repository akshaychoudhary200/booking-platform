import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class TestClass {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.springframework.security.authentication.dao.DaoAuthenticationProvider");
        for (Constructor<?> c : clazz.getConstructors()) {
            System.out.println(c);
        }
        for (Method m : clazz.getMethods()) {
            if (m.getName().toLowerCase().contains("user")) {
                System.out.println(m);
            }
        }
    }
}
