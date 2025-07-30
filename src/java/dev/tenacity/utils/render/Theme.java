package dev.tenacity.utils.render;

import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.tuples.Pair;
import lombok.Getter;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum Theme {//嘿嘿
    //Green
    LITTLE_LEAF("Little Leaf", rgb(118, 184, 82), rgb(141, 194, 111)),
    LUSH("Lush", rgb(86, 171, 47), rgb(168, 224, 99)),
    SPEARMINT("Spearmint", rgb(97, 194, 162), rgb(65, 130, 108)),
    JADE_GREEN("Jade Green", rgb(0, 168, 107), rgb(0, 105, 66)),
    GREEN_SPIRIT("Green Spirit", rgb(0, 135, 62), rgb(159, 226, 191)),
    MINT_BLUE("Mint Blue", rgb(66, 158, 157), rgb(40, 94, 93)),
    PACIFIC_BLUE("Pacific Blue", rgb(5, 169, 199), rgb(4, 115, 135)),
    TROPICAL_ICE("Tropical Ice", rgb(102, 255, 209), rgb(6, 149, 255)),
    OH_HAPPINESS("Oh Happiness", rgb(0, 176, 155), rgb(150, 201, 61)),
    //Pink
    ROSY_PINK("Rosy Pink", rgb(255, 102, 204), rgb(191, 77, 153)),
    MAGENTA("Magenta", rgb(213, 63, 119), rgb(157, 68, 110)),
    HOT_PINK("Hot Pink", rgb(231, 84, 128), rgb(172, 79, 198)),
    BLUSH("Blush", rgb(178, 69, 146), rgb(241, 95, 121)),
    PIGLET("Piglet", rgb(238, 156, 167), rgb(255, 221, 225)),
    //Purple
    LAVENDER("Lavender", rgb(219, 166, 247), rgb(152, 115, 172)),
    AMETHYST("Amethyst", rgb(144, 99, 205), rgb(98, 67, 140)),
    PURPLE_FIRE("Purple Fire", rgb(104, 71, 141), rgb(177, 162, 202)),
    MAUVE("Mauve", rgb(66, 39, 90), rgb(115, 75, 109)),
    SLIGHT_OCEAN_VIEW("Slight Ocean View", rgb(168, 192, 255), rgb(63, 43, 150)),
    MOON_PURPLE("Moon Purple", rgb(78, 84, 200), rgb(143, 148, 251)),
    PURPLIN("Purplin", rgb(106, 48, 147), rgb(160, 68, 255)),
    DEEP_PURPLE("Deep Purple", rgb(103, 58, 183), rgb(81, 45, 168)),
    TWITCH("Twitch", rgb(100, 65, 165), rgb(42, 8, 69)),
    //Orange
    GRAPEFRUIT_SUNSET("Grapefruit Sunset", rgb(233, 100, 67), rgb(144, 78, 149)),
    SUNSET_PINK("Sunset Pink", rgb(255, 145, 20), rgb(245, 105, 231)),
    BLAZE_ORANGE("Blaze Orange", rgb(255, 169, 77), rgb(255, 130, 0)),
    PASTEL("Pastel", rgb(255, 109, 106), rgb(191, 82, 80)),
    ROSEANNA("Roseanna", rgb(255, 175, 189), rgb(255, 195, 160)),
    HAIKUS("Haikus", rgb(253, 116, 108), rgb(255, 144, 104)),
    LIGHT_ORANGE("Light Orange", rgb(255, 183, 94), rgb(237, 143, 3)),
    MASTER_CARD("Master Card", rgb(244, 107, 69), rgb(238, 168, 73)),
    BACK_TO_THE_FUTURE("Back to the Future", rgb(192, 36, 37), rgb(240, 203, 53)),
    //Red
    PINK_BLOOD("Pink Blood", rgb(228, 0, 70), rgb(255, 166, 201)),
    NEON_RED("Neon Red", rgb(210, 39, 48), rgb(184, 25, 42)),
    RED_COFFEE("Red Coffee", Color.BLACK, rgb(225, 34, 59)),
    BIGHEAD("Bighead", rgb(201, 75, 75), rgb(75, 19, 79)),
    RED_WITH_WHITE("Red With White", Color.RED, Color.WHITE),
    NETFLIX("Netflix", rgb(142, 14, 0), rgb(31, 28, 24)),
    //Blue
    FROST("Frost", rgb(0, 4, 40), rgb(0, 78, 146)),
    CHAMBRAY_BLUE("Chambray Blue", rgb(33, 46, 182), rgb(60, 82, 145)),
    SOLID_VAULT("Solid Vault", rgb(58, 123, 213), rgb(58, 96, 115)),
    DEEP_SEA_SPACE("Deep Sea Space", rgb(44, 62, 80), rgb(76, 161, 175)),
    DARK_SKIES("Dark Skies", rgb(75, 121, 161), rgb(40, 62, 81)),
    JOOMLA("Joomla", rgb(30, 60, 114), rgb(42, 82, 152)),
    NIGHTHAWK("Nighthawk", rgb(41, 128, 185), rgb(44, 62, 80)),
    CLEAR_SKY("Clear Sky", rgb(0, 92, 151), rgb(54, 55, 149)),
    BETWEEN_NIGHT_AND_DAY("Between Night and Day", rgb(44, 62, 80), rgb(52, 152, 219)),
    //Yellow
    SAGE_PERSUASION("Sage Persuasion", rgb(204, 204, 178), rgb(117, 117, 25)),
    SELENIUM("Selenium", rgb(60, 59, 63), rgb(96, 92, 60)),
    KOKO_CARAMEL("Koko Caramel", rgb(209, 145, 60), rgb(255, 209, 148)),
    //Light
    COLOR_OF_SKY("Color Of Sky", rgb(224, 234, 252), rgb(207, 222, 243)),
    MARGO("Margo", rgb(255, 239, 186), rgb(255, 255, 255)),
    LITHIUM("Lithium", rgb(109, 96, 39), rgb(211, 203, 184)),
    PALE_WOOD("Pale Wood", rgb(234, 205, 163), rgb(214, 174, 123)),
    PORTRAIT("Portral", rgb(142, 158, 171), rgb(238, 242, 243)),
    //Dark
    DEEP_SPACE("Deep Space", rgb(0, 0, 0), rgb(67, 67, 67)),
    LAWRENCIUM("Lawrencium", rgb(15, 12, 41), rgb(36, 36, 62)),
    DEEP_OCEAN("Deep Ocean", rgb(60, 82, 145), rgb(0, 20, 64)),
    //Mixer
    EXPRESSO("eXpresso", rgb(173, 83, 137), rgb(60, 16, 83)),
    TRANQUIL("Tranquil", rgb(238, 205, 163), rgb(239, 98, 159)),
    CHRISTMAS("Christmas", rgb(47, 115, 54), rgb(170, 58, 56)),
    SUPERMAN("Superman", rgb(0, 153, 247), rgb(241, 23, 18)),
    MINNESOTA_VIKINGS("Minnesota Vikings", rgb(86, 20, 176), rgb(219, 214, 92)),
    MIAMI_DOLPHINS("Miami Dolphins", rgb(77, 160, 176), rgb(211, 157, 56)),
    PIZELEX("Pizelex", rgb(17, 67, 87), rgb(242, 148, 146)),
    GREEN_TO_DARK("Green to dark", rgb(106, 145, 19), rgb(20, 21, 23)),
    LIZARD("Lizard", rgb(48, 67, 82), rgb(215, 210, 204)),
    ATLAS_A("Atlas A", rgb(254, 172, 94), rgb(75, 192, 200)),
    ATLAS_B("Atlas B", rgb(254, 172, 94), rgb(199, 121, 208)),
    ATLAS_C("Atlas C", rgb(199, 121, 208), rgb(75, 192, 200)),
    //Custom
    CUSTOM_THEME("Custom Theme", HUDMod.color1.get(), HUDMod.color2.get());

    @Getter
    private static final Map<String, Theme> themeMap = new HashMap<>();

    private final String name;
    private final Pair<Color, Color> colors;
    private final boolean gradient;

    Theme(String name, Color color, Color colorAlt) {
        this(name, color, colorAlt, true);
    }

    Theme(String name, Color color, Color colorAlt, boolean gradient) {
        this.name = name;
        colors = Pair.of(color, colorAlt);
        this.gradient = gradient;
    }

    private static Color rgb(int r, int g, int b) {
        return new Color(r, g, b);
    }

    public static void init() {
        Arrays.stream(values()).forEach(theme -> themeMap.put(theme.getName(), theme));
    }

    public static Pair<Color, Color> getThemeColors(String name) {
        return get(name).getColors();
    }

    public static ModeSetting getModeSetting(String name, String defaultValue) {
        return new ModeSetting(name, defaultValue, Arrays.stream(Theme.values()).map(Theme::getName).toArray(String[]::new));
    }

    public static Theme get(String name) {
        return themeMap.get(name);
    }

    public static Theme getCurrentTheme() {
        return Theme.get(HUDMod.theme.get());
    }

    public Pair<Color, Color> getColors() {
        if (this.equals(Theme.CUSTOM_THEME)) {
            if (HUDMod.color1.isRainbow()) {
                return Pair.of(HUDMod.color1.get(), HUDMod.color1.getAltColor());
            } else return Pair.of(HUDMod.color1.get(), HUDMod.color2.get());
        } else return colors;
    }
}
