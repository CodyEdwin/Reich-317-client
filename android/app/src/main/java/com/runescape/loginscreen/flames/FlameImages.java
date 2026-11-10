package com.runescape.loginscreen.flames;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

/**
 * Android port of the shared {@code FlameImages} enum.
 *
 * <p>The desktop source is compiled with Kotlin 1.3 (see the root
 * {@code build.gradle.kts}), which allowed enum-entry initialisers to call
 * functions declared in the enum's own {@code companion object}. Kotlin 1.9 —
 * required by the Android Gradle plugin — rejects that with
 * "companion object of enum class 'FlameImages' is uninitialized here",
 * because the entries are initialised before the companion.
 *
 * <p>Written in Java, the holiday predicates are plain static methods and the
 * nested {@link Companion} class keeps the exact public API the desktop class
 * exposed — in particular {@code FlameImages.Companion.getRandomImage()},
 * which {@code FlameManager} calls. Behaviour is identical.
 */
public enum FlameImages {

    // Halloween icons
    BAT(34, FlameImages::holidayIsHalloween),
    SKULL(36, FlameImages::holidayIsHalloween),
    MOON(37, FlameImages::holidayIsHalloween),
    PUMPKIN(35, FlameImages::holidayIsHalloween),

    // Christmas icons
    SNOWFLAKE(38, FlameImages::holidayIsChristmas),
    HOLLY(39, FlameImages::holidayIsChristmas),
    SNOWMAN(40, FlameImages::holidayIsChristmas),
    ANGEL(41, FlameImages::holidayIsChristmas),

    // Normal icons
    FIRE_RUNE(42, FlameImages::holidayIsNotHoliday),
    WATER_RUNE(43, FlameImages::holidayIsNotHoliday),
    EARTH_RUNE(44, FlameImages::holidayIsNotHoliday),
    AIR_RUNE(45, FlameImages::holidayIsNotHoliday),
    BODY_RUNE(46, FlameImages::holidayIsNotHoliday),
    WRATH_RUNE(47, FlameImages::holidayIsNotHoliday),
    CHAOS_RUNE(48, FlameImages::holidayIsNotHoliday),
    COSMIC_RUNE(49, FlameImages::holidayIsNotHoliday),
    NATURE_RUNE(50, FlameImages::holidayIsNotHoliday),
    LAW_RUNE(51, FlameImages::holidayIsNotHoliday),
    DEATH_RUNE(52, FlameImages::holidayIsNotHoliday),
    SOUL_RUNE(53, FlameImages::holidayIsNotHoliday);

    private static final int HOLIDAY_CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    private static final Random RANDOM = new Random();

    private static List<FlameImages> flameImages = new ArrayList<>();

    private final int id;

    private final BooleanSupplier active;

    FlameImages(int id, BooleanSupplier active) {
        this.id = id;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return active != null && active.getAsBoolean();
    }

    public static boolean isNotHoliday() {
        return !isChristmas() && !isHalloween();
    }

    public static boolean isHalloween() {
        return holidayIsHalloween();
    }

    public static boolean isChristmas() {
        return holidayIsChristmas();
    }

    public static int getRandomImage() {
        if (flameImages.isEmpty()) {
            List<FlameImages> active = new ArrayList<>();
            for (FlameImages image : values()) {
                if (image.isActive()) {
                    active.add(image);
                }
            }
            flameImages = active;
        }
        if (flameImages.isEmpty()) {
            return FIRE_RUNE.getId();
        }
        return flameImages.get(RANDOM.nextInt(flameImages.size())).getId();
    }

    /**
     * Keeps the desktop API shape: Kotlin's {@code companion object} is
     * addressed as {@code FlameImages.Companion.getRandomImage()}.
     */
    public static final class Companion {

        private Companion() {
        }

        public static boolean isNotHoliday() {
            return FlameImages.isNotHoliday();
        }

        public static boolean isHalloween() {
            return FlameImages.isHalloween();
        }

        public static boolean isChristmas() {
            return FlameImages.isChristmas();
        }

        public static int getRandomImage() {
            return FlameImages.getRandomImage();
        }
    }

    private static boolean holidayIsNotHoliday() {
        return !holidayIsChristmas() && !holidayIsHalloween();
    }

    private static boolean holidayIsHalloween() {
        Date dateStart = new GregorianCalendar(HOLIDAY_CURRENT_YEAR, Calendar.OCTOBER, 21).getTime();
        Date dateEnd = new GregorianCalendar(HOLIDAY_CURRENT_YEAR, Calendar.NOVEMBER, 11).getTime();
        return holidayIsWithinRange(dateStart, dateEnd);
    }

    private static boolean holidayIsChristmas() {
        Date dateStart = new GregorianCalendar(HOLIDAY_CURRENT_YEAR, Calendar.DECEMBER, 16).getTime();
        Date dateEnd = new GregorianCalendar(HOLIDAY_CURRENT_YEAR, Calendar.JANUARY, 5).getTime();
        return holidayIsWithinRange(dateStart, dateEnd);
    }

    private static boolean holidayIsWithinRange(Date startDate, Date endDate) {
        Date currentDate = Date.from(Instant.now());
        return !(currentDate.before(startDate) || currentDate.after(endDate));
    }
}
