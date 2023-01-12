package dic;

import static org.junit.Assert.*;

import org.junit.*;

public class MainTest {
    Container c;

    @Before
    public void init() {
        c = new Container();
    }

    @Test
    public void autoInject() throws Exception {
        B inst = c.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectImplementation() throws Exception {
        c.registerImplementation(A.class);
        B inst = c.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectInstance() throws Exception {
        A a = new A();
        c.registerInstance(a);
        B inst = c.getInstance(B.class);

        assertNotNull(inst);
        assertSame(a, inst.aField);
    }

    @Test
    public void injectNamedInstance() throws Exception {
        A a = new A();
        c.registerInstance("iname", a);
        F inst = c.getInstance(F.class);

        assertNotNull(inst);
        assertSame(a, inst.iname);
    }

    @Test
    public void injectStringProperty() throws Exception {
        String email = "name@yahoo.com";
        c.registerInstance("email", email);
        FS inst = c.getInstance(FS.class);

        assertNotNull(inst);
        assertNotNull(inst.email);
        assertSame(inst.email, email);
    }

    @Test
    public void constructorInject() throws Exception {
        E inst = c.getInstance(E.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectInterface() throws Exception {
        c.registerImplementation(AI.class, A.class);
        B inst = c.getInstance(B.class);

        assertNotNull(inst);
        assertNotNull(inst.aField);
    }

    @Test
    public void injectDefaultImplementationForInterface() throws Exception {
        DI inst = c.getInstance(DI.class);
        assertNotNull(inst);
    }

    @Test//(expected=RegistryException.class)
    public void injectMissingDefaultImplementationForInterface() throws Exception {
        AI inst = c.getInstance(AI.class);
        assertNull(inst);
    }

    @Test
    public void decorateInstance() throws Exception {
        C ci = new C();
        c.decorateInstance(ci);

        assertNotNull(ci.bField);
        assertNotNull(ci.bField.aField);
    }

    /*@Test
    public void initializer() throws Exception {
        String email = "name@yahoo.com";
        c.registerInstance("email", email);
        FSI inst = c.getInstance(FSI.class);

        assertNotNull(inst);
        assertNotNull(inst.email);
        assertEquals(inst.email, "mailto:" + email);
    }*/

}
