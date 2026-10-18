package com.carpentersblocks.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a GameTest method in a class listed in {@link CBGameTests#TEST_CLASSES}. Used instead of vanilla's
 * {@code @GameTest} because Fabric and NeoForge resolve that annotation's structure names differently; this way both
 * loaders run the same tests against the same structures.
 *
 * <p>The method must be public, take a {@link net.minecraft.gametest.framework.GameTestHelper} and call
 * {@code helper.succeed()} (or one of the {@code succeedWhen...} variants) when done.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CBGameTest {

    /** Structure under {@code data/carpentersblocks/structure/}; without a namespace, {@code carpentersblocks} is used. */
    String structure() default CBGameTests.EMPTY_STRUCTURE;

    int timeoutTicks() default 100;

    long setupTicks() default 0L;

    boolean required() default true;
}
