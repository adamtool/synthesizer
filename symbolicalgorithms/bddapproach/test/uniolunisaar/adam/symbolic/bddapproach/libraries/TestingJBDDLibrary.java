package uniolunisaar.adam.symbolic.bddapproach.libraries;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.exceptions.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.util.JavaBDDCallback;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingJBDDLibrary {

    private final int NODENUM = 1000;
    private final int CACHESIZE = 1000;

    private static final String inputDir = System.getProperty("examplesfolder") + "/safety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @Test
    public static void testCallings() throws ParseException, IOException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, CouldNotFindSuitableConditionException, ParameterMissingException, SolvingException {
        final String path = inputDir + "firstExamplePaper" + File.separator;
        final String name = "firstExamplePaper";
        BDDSolver<? extends Condition> solv = BDDSolverFactory.getInstance().getSolver(path + name + ".apt", true);
        solv.initialize();
    }

    @Test(enabled = false)
    public void speicher() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        BDDFactory bdd = JFactory.init("buddy", NODENUM, CACHESIZE);
        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setSilent(true);
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override
//            public void write(int arg0) throws IOException {
//                // keep empty
//            }
//        }));
//        bdd.registerGCCallback(System.out, PrintStream.class.getMethod("println", String.class));
//        bdd.registerGCCallback(new JavaBDDGCCallback(), JavaBDDGCCallback.class.getMethod("out", new Class[]{Boolean.class}));
//        JavaBDDCallback jbddgcc = new JavaBDDCallback();
//        Method m = JavaBDDGCCallback.class.getMethod("asdfasdf", BDDFactory.GCStats.class);
//        bdd.registerGCCallback(jbddgcc, jbddgcc.getClass().getMethod("asdf", new Class[]{BDDFactory.GCStats.class}));
//        bdd.registerGCCallback(jbddgcc, jbddgcc.getClass().getMethod("asdf", new Class[]{String.class}));
//        m.invoke(jbddgcc, true, bdd.getGCStats());
        Method m = JavaBDDCallback.class.getMethod("outGCStats", Integer.class, BDDFactory.GCStats.class);
        bdd.registerGCCallback(new JavaBDDCallback(bdd), m);
        m = JavaBDDCallback.class.getMethod("outReorderStats", Integer.class, BDDFactory.ReorderStats.class);
        bdd.registerReorderCallback(new JavaBDDCallback(bdd), m);
        m = JavaBDDCallback.class.getMethod("outResizeStats", Integer.class, Integer.class);
        bdd.registerResizeCallback(new JavaBDDCallback(bdd), m);

        int size = 138;
        bdd.setVarNum(size);
        int i = 0;
        BDD test = bdd.ithVar(0);
        while (i < size) {
//            System.out.println("i" + i);
            BDD b = bdd.ithVar((size - 1) - (i % size)).impWith(bdd.ithVar(i % size));
            test = test.and(b);
            b.free();
            i++;
        }

//        System.out.println("NEXT ONE");
        BDD test2 = bdd.ithVar(5);
        int j = 0;
        while (j < 10) {
            test2 = test2.orWith(bdd.ithVar(j));
            j++;
        }
    }

    @Test
    public void jbdd() {
        //BDDFactory bdd = CUDDFactory.init(NODENUM, CACHESIZE);
        BDDFactory bdd = JFactory.init(NODENUM, CACHESIZE);

        BDD top = bdd.one();
        BDD bottom = bdd.zero();
        BDD a = top.and(bottom);
        Assert.assertTrue(a.isZero());

        bdd.setVarNum(3);

        BDD var1 = bdd.ithVar(0);
        var1 = top;
//        System.out.println(var1.toString());
//        System.out.println("var1" + var1.var());

        BDD var2 = bdd.ithVar(1);
//        System.out.println(var2.toString());
//        System.out.println("var2" + var2.var());
        //BDD and = var1.and(var2);
        BDD var3 = bdd.nithVar(2);
        BDD or = var1.or(var2).or(var3);
//        System.out.println(or.toString());

//        List<byte[]> l = or.allsat();
//        for (byte[] object : l) {
//            System.out.println(Arrays.toString(object));
//        }
        BDD test = bdd.nithVar(0);
        test = test.and(bdd.ithVar(1));
        test = test.and(bdd.ithVar(2));

//        printDecisionSets(test);
        BDD res = bdd.nithVar(2);
        BDD restriction = test.restrict(res);

//        System.out.println(restriction.isZero());
        bdd.extVarNum(1);
        BDD ch = bdd.nithVar(3);
        BDD changed = test.replace(bdd.makePair(0, ch));
//        BDDTools.printDecisionSets(changed);

//        System.out.println(test.var());
        BDD asdf = bdd.buildCube(1, new int[]{0, 1, 2});

//        BDDTools.printDecisionSets(asdf);
    }

    @Test
    public void testVariableOrder() {
        BDDFactory bdd = JFactory.init(NODENUM, CACHESIZE);
        bdd.setVarNum(10);

        BDD first = bdd.ithVar(0);
        first = first.and(bdd.nithVar(1));
        first = first.and(bdd.nithVar(2));
        first = first.and(bdd.ithVar(3));
        first = first.and(bdd.ithVar(4));
        first = first.and(bdd.ithVar(5));
        first = first.and(bdd.ithVar(6));
        first = first.and(bdd.ithVar(7));
        first = first.and(bdd.nithVar(8));
        first = first.and(bdd.nithVar(9));
//        System.out.println("first:");
//        printDecisionSets(first);

        int[] neworder = new int[]{0, 5, 1, 6, 2, 7, 3, 8, 4, 9};
        bdd.setVarOrder(neworder);

//        System.out.println("first after reorder:");
//        printDecisionSets(first);
        BDD second = bdd.ithVar(0);
        second = second.and(bdd.nithVar(1));
        second = second.and(bdd.nithVar(2));
        second = second.and(bdd.ithVar(3));
        second = second.and(bdd.ithVar(4));
        second = second.and(bdd.ithVar(5));
        second = second.and(bdd.ithVar(6));
        second = second.and(bdd.ithVar(7));
        second = second.and(bdd.nithVar(8));
        second = second.and(bdd.nithVar(9));
//        System.out.println("second:");
//        printDecisionSets(second);

    }

    @Test
    public void testProjection() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(10);
        BDDFactory fac1 = JFactory.init(NODENUM, CACHESIZE);
        fac1.setVarNum(5);

        BDD test = fac.nithVar(0);
        test = test.and(fac.nithVar(6));
        test = test.and(fac.ithVar(7));
//        System.out.println("orig");
//        printDecisionSets(test);

        BDD ex = fac.ithVar(5);
        ex = ex.and(fac.ithVar(6));
        ex = ex.and(fac.ithVar(7));
        ex = ex.and(fac.ithVar(8));
        ex = ex.and(fac.ithVar(9));

        test = test.exist(ex);
//        System.out.println("exist");
//        printDecisionSets(test);

        BDD test2 = fac1.ithVar(0);

        test2 = test2.or(test);
//        System.out.println("blub");
//        printDecisionSets(test2);

        test = test.and(test2.not());
//        System.out.println("blub2");
//        printDecisionSets(test);

    }

    @Test
    public void testCompose() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(5);
        //01100
        BDD test = fac.nithVar(0);
        test = test.and(fac.ithVar(1));
        test = test.and(fac.ithVar(2));
        test = test.and(fac.nithVar(3));
        test = test.and(fac.nithVar(4));
//        printDecisionSets(test);

//        test.compose(fac.ithVar(4), 2);
//        printDecisionSets(test);
    }

    @Test
    public void testReplacement() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(5);
        //01100
        BDD test = fac.nithVar(0);
        test = test.and(fac.ithVar(1));
        test = test.and(fac.ithVar(2));
        test = test.and(fac.nithVar(3));
        test = test.and(fac.nithVar(4));
//        System.out.println("orgi:");
//        printDecisionSets(test);

        // del
        //10111
        BDD del = fac.ithVar(0);
        del = del.and(fac.nithVar(1));
        del = del.and(fac.ithVar(2));
        del = del.and(fac.ithVar(3));
        del = del.and(fac.ithVar(4));
//        System.out.println("del:");
//        printDecisionSets(test.and(del));

//        System.out.println("comp");
//        printDecisionSets(test.constrain(fac.zero()));
//        printDecisionSets(test.compose(fac.ithVar(0), 2));
        // 00110
        BDD desired = fac.nithVar(0);
        desired = desired.and(fac.nithVar(1));
        desired = desired.and(fac.ithVar(2));
        desired = desired.and(fac.ithVar(3));
        desired = desired.and(fac.nithVar(4));
//        System.out.println("desired:");
//        printDecisionSets(desired);

        BDDFactory fac1 = JFactory.init(NODENUM, CACHESIZE);
        fac1.setVarNum(10);
        //01100
        BDD first = fac.nithVar(0);
        first.andWith(fac.ithVar(1));
        first.andWith(fac.ithVar(2));
        first.andWith(fac.nithVar(3));
        first.andWith(fac.nithVar(4));
//        System.out.println("first:");
//        printDecisionSets(first);

        BDD second = fac1.ithVar(0);
        second = second.and(fac1.nithVar(1));
        second = second.and(fac1.nithVar(2));
        second = second.and(fac1.ithVar(3));
        second = second.and(fac1.ithVar(4));
        second = second.and(fac1.nithVar(5));
        second = second.and(fac1.ithVar(6));
        second = second.and(fac1.ithVar(7));
        second = second.and(fac1.nithVar(8));
        second = second.and(fac1.nithVar(9));
//        System.out.println("second:");
//        printDecisionSets(second);

//        first = first.compose(second, 0);
//        printDecisionSets(first);
//        System.out.println("first or second:");
//        printDecisionSets(first.or(second));
//        System.out.println("second or first:");
//        printDecisionSets(second.or(first));
        BDDPairing pair = fac1.makePair();
        pair.set(new int[]{5, 6, 7, 8, 9}, new int[]{0, 1, 2, 3, 4});

        BDDPairing pair1 = fac1.makePair();
        pair1.set(new int[]{0, 1, 2, 3, 4}, new int[]{5, 6, 7, 8, 9});

        BDD ex = fac1.ithVar(5);
        ex = ex.and(fac1.ithVar(6));
        ex = ex.and(fac1.ithVar(7));
        ex = ex.and(fac1.ithVar(8));
        ex = ex.and(fac1.ithVar(9));

        BDD ex1 = fac1.ithVar(0);
        ex1 = ex1.and(fac1.ithVar(1));
        ex1 = ex1.and(fac1.ithVar(2));
        ex1 = ex1.and(fac1.ithVar(3));
        ex1 = ex1.and(fac1.ithVar(4));

//        System.out.println("exists: ");
//        printDecisionSets(second.exist(ex));
//        System.out.println("second replaced:");
//        printDecisionSets(second.exist(ex).replace(pair1));
//        System.out.println("first replaced:");
//        printDecisionSets(first.replace(pair));
        BDD first_back = fac1.ithVar(0).or(fac1.nithVar(0));
//        printDecisionSets(first_back);
        first_back = first_back.and(first);
//        System.out.println("first long");
//        printDecisionSets(first_back);

//        System.out.println("test");
//        printDecisionSets(first);
//        fac.setVarNum(10);
//        printDecisionSets(first);
        //01100
//        BDD asdf = first.compose(fac.ithVar(3), 0);
//        System.out.println("blub");
//        printDecisionSets(asdf);
    }

    @Test
    public void testRestriction() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(5);
        //01100
        BDD test = fac.nithVar(0);
        test = test.and(fac.ithVar(1));
        test = test.and(fac.ithVar(2));
        test = test.and(fac.nithVar(3));
        test = test.and(fac.nithVar(4));
//        printDecisionSets(test);

        // copy first three values
        BDD res = fac.ithVar(1).and(fac.ithVar(2));
        BDD test2 = test.restrict(res);
        // set 3 and 4 to one
        // test2 = test2.and(fac.ithVar(3).and(fac.ithVar(4)));
//        System.out.println("test2");
//        printDecisionSets(test2);
//        System.out.println("test2-ready");

//        System.out.println("bla");
//        printDecisionSets(test.high());
//        printDecisionSets(test.low());
//        System.out.println("high");
//        printDecisionSets(fac.ithVar(0).high());
//        System.out.println("low");
//        printDecisionSets(fac.ithVar(0).low());
//        System.out.println("first");
//        printDecisionSets(test2);
//        System.out.println("Restricted variables:");
//        printDecisionSets(res);
//        test2 = test2.restrict(res);
//        System.out.println("restriction of first");
//        printDecisionSets(test2);
//        
//        test.
//        System.out.println("asdf"+      fac.getDomain(1));
        //Assert.assertTrue(test.biimp(test2).isOne());
    }

    @Test
    public void testDomain3() {
        final int TOKEN = 3;
        BDDDomain[][] top = new BDDDomain[2][TOKEN - 1];
        BDDDomain[][] type = new BDDDomain[2][TOKEN - 1];
        BDDDomain[][] transitions = new BDDDomain[2][TOKEN - 1];
        BDDDomain[][] places = new BDDDomain[2][TOKEN];

        int env_size = 4;
        int sys_size = 6;
        BigInteger maxTrans = BigInteger.valueOf(2);
        maxTrans = maxTrans.pow(9);
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        for (int j = 0; j < 2; j++) {
            // env place 
            places[j][0] = fac.extDomain(env_size);
            // 10 token
            for (int i = 1; i < TOKEN; i++) {
                places[j][i] = fac.extDomain(sys_size);
                // type
                type[j][i - 1] = fac.extDomain(2);
                // top
                top[j][i - 1] = fac.extDomain(2);
                // transitions 9
                transitions[j][i - 1] = fac.extDomain(maxTrans);
            }
        }
//        BDDTools.printDecisionSets(transitions[0][0].ithVar(maxTrans.intValue() - 1), true);
//        BDDTools.printDecisionSets(type[0][1].ithVar(0).and(top[0][1].ithVar(1)), true);
//        BDDTools.printDecisionSets(type[0][1].ithVar(1).and(top[0][1].ithVar(0)), true);
        BDD env = places[0][0].ithVar(0);
//        BDDTools.printDecisionSets(env.and(places[0][2].ithVar(0).and(type[0][0].ithVar(1).and(top[0][0].ithVar(1)))), true);
//        BDDTools.printDecisionSets(places[0][2].ithVar(0).and(type[0][1].ithVar(1).and(top[0][1].ithVar(1))), true);
//        System.out.println("%%%%%%%%%%%%%%%%%");
        BDD pls = env.and(places[0][1].ithVar(0)).and(places[0][2].ithVar(0));
        BDD trans = fac.ithVar(transitions[0][0].vars()[0]);
        trans.andWith(fac.ithVar(transitions[0][0].vars()[8]));
//        BDDTools.printDecisionSets(pls.and(trans), true);
    }

    @Test
    public void testDomain2() {
        int env_size = 4;
        int sys_size = 6;
        BigInteger maxTrans = BigInteger.valueOf(2);
        maxTrans = maxTrans.pow(9);
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        for (int j = 0; j < 2; j++) {
            // env place 
            fac.extDomain(env_size);
            // 10 token
            for (int i = 0; i < 10; i++) {
                fac.extDomain(sys_size);
                // type
                fac.extDomain(1);
                // top
                fac.extDomain(1);
                // transitions 9
                fac.extDomain(maxTrans);
            }
        }
//        printDecisionSets(fac.getDomain(0).ithVar(3), true);
//        printDecisionSets(fac.getDomain(1).ithVar(5), true);
        // interleaving of D and D' for D->D'
        int[] order = new int[fac.varNum()];
        int count = 0;
        for (int i = 0; i < fac.varNum() / 2; i++) {
            order[count++] = i;
            order[count++] = fac.varNum() / 2 + i;
        }
        fac.setVarOrder(order);
//        System.out.println("neu");
//        printDecisionSets(fac.getDomain(1).ithVar(5), true);
//        printDecisionSets(fac.getDomain(2).ithVar(0), true);
    }

    @Test
    public void testDomain() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(10);
        //01100 11000
        BDD test = fac.nithVar(0);
        test.andWith(fac.ithVar(1));
        test.andWith(fac.ithVar(2));
        test.andWith(fac.nithVar(3));
        test.andWith(fac.nithVar(4));
        test.andWith(fac.ithVar(5));
        test.andWith(fac.ithVar(6));
        test.andWith(fac.nithVar(7));
        test.andWith(fac.nithVar(8));
        test.andWith(fac.nithVar(9));
//        System.out.println("first");
//        printDecisionSets(test, true);

        fac.extDomain(5);
        fac.extDomain(16);
//        System.out.println("blub" + Arrays.toString(fac.getDomain(1).vars()));

        BDD asdf = fac.getDomain(0).ithVar(3);
//        System.out.println("fac.getDomain(0).ithVar(2)");
//        printDecisionSets(asdf, true);

//        System.out.println("domain");
//        printDecisionSets(fac.getDomain(0).domain(), true);
        BDD asdf1 = fac.getDomain(1).ithVar(10);
//        System.out.println("fac.getDomain(3).ithVar(2)");
//        printDecisionSets(asdf1, true);
        asdf1.andWith(fac.ithVar(9));
        asdf1.andWith(fac.ithVar(1));
//        printDecisionSets(asdf1, true);
        asdf1.andWith(fac.getDomain(0).ithVar(3));
//        printDecisionSets(asdf1, true);

    }

    @Test
    public void testSatOne() {
        BDDFactory fac = JFactory.init(NODENUM, CACHESIZE);
        fac.setVarNum(3);
        BDD test = fac.nithVar(0);
        test.andWith(fac.ithVar(1));
        test.andWith(fac.ithVar(2).or(fac.nithVar(2)));
//        printDecisionSets(test);
        BDD one = test.satOne();
//        System.out.println(test.satCount());
//        BDDTools.printDecisionSets(one);
    }
}
