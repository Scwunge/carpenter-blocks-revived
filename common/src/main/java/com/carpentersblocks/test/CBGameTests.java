package com.carpentersblocks.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.test.barrier.BarrierTests;
import com.carpentersblocks.test.block.CarpentersBlockTests;
import com.carpentersblocks.test.block.CoverBehaviorTests;
import com.carpentersblocks.test.redstone.RedstoneTests;
import com.carpentersblocks.test.stairs.StairsTests;

/**
 * Entry point for every Carpenter's Blocks GameTest. Each loader registers only this class (Fabric through the
 * {@code fabric-gametest} entrypoint, NeoForge through {@code RegisterGameTestsEvent}); its generator turns the
 * {@link CBGameTest} methods of the classes below into test functions with fully qualified structure names.
 *
 * <p>Each block family adds its test class (package {@code com.carpentersblocks.test.<family>}) to
 * {@link #TEST_CLASSES}. Tests are named {@code <family>.<method>} and batched per family.
 */
public final class CBGameTests {

    public static final String EMPTY_STRUCTURE = "empty";

    public static final List<Class<?>> TEST_CLASSES = List.of(
            CarpentersBlockTests.class,
            CoverBehaviorTests.class,
            StairsTests.class,
            BarrierTests.class,
            RedstoneTests.class);

    /** Fabric instantiates gametest entrypoints. */
    public CBGameTests() {}

    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        List<TestFunction> functions = new ArrayList<>();
        for (Class<?> testClass : TEST_CLASSES) {
            functions.addAll(functionsFor(testClass));
        }
        return functions;
    }

    static List<TestFunction> functionsFor(Class<?> testClass) {
        String family = familyOf(testClass);
        Object instance = instantiate(testClass);
        List<TestFunction> functions = new ArrayList<>();
        Method[] methods = testClass.getMethods();
        // Reflection order is unspecified; keep test order stable between runs
        java.util.Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method method : methods) {
            CBGameTest test = method.getAnnotation(CBGameTest.class);
            if (test == null) {
                continue;
            }
            if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != GameTestHelper.class) {
                throw new IllegalStateException("@CBGameTest method must take a GameTestHelper: " + method);
            }
            String name = family + "." + method.getName().toLowerCase(Locale.ROOT);
            functions.add(new TestFunction(
                    CarpentersBlocks.MOD_ID + "." + family,
                    name,
                    structureId(test.structure()),
                    Rotation.NONE,
                    test.timeoutTicks(),
                    test.setupTicks(),
                    test.required(),
                    invoker(Modifier.isStatic(method.getModifiers()) ? null : instance, method)));
        }
        return functions;
    }

    /** {@code com.carpentersblocks.test.slope.SlopeTests} -> {@code slope}. */
    static String familyOf(Class<?> testClass) {
        String pkg = testClass.getPackageName();
        String prefix = CBGameTests.class.getPackageName() + ".";
        if (!pkg.startsWith(prefix) || pkg.length() == prefix.length()) {
            throw new IllegalStateException("Test class must live in " + prefix + "<family>: " + testClass.getName());
        }
        return pkg.substring(prefix.length()).replace('.', '_');
    }

    static String structureId(String structure) {
        return structure.contains(":") ? structure : CarpentersBlocks.MOD_ID + ":" + structure;
    }

    private static Object instantiate(Class<?> testClass) {
        try {
            return testClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Test class needs a public no-arg constructor: " + testClass.getName(), e);
        }
    }

    private static Consumer<GameTestHelper> invoker(Object instance, Method method) {
        return helper -> {
            try {
                method.invoke(instance, helper);
            } catch (InvocationTargetException e) {
                // Let the framework see the real failure (GameTestAssertException and friends)
                if (e.getCause() instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
