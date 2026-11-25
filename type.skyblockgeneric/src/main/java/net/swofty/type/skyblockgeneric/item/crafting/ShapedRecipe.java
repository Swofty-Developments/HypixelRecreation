package net.swofty.type.skyblockgeneric.item.crafting;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ItemQuantifiable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
public class ShapedRecipe extends SkyBlockRecipe<ShapedRecipe> {
    public static final List<ShapedRecipe> CACHED_RECIPES = new ArrayList<>();

    private final List<String> pattern;
    private final char[][] patternArray;
    private final int height;
    private final int width;

    private static final int GRID_SIZE = 9;
    private static final int CHAR_SPACE = 256; // index by (char & 0xFF)
    private final ItemQuantifiable[] ingredientByChar;

    private final Function<SkyBlockItem, Boolean>[] extraReqByChar;
    private final boolean[] symbolHasExtraReq;
    private final Variant[] variants;
    private final int recipeSize;

    private record Variant(int[] airMaskByOffset, int[][] requiredGridIdxByOffset, char[][] requiredSymByOffset) {
    }

    public ShapedRecipe(RecipeType type,
                        SkyBlockItem result,
                        Map<Character, ItemQuantifiable> ingredientMap,
                        List<String> pattern,
                        Function<SkyBlockPlayer, CraftingResult> canCraft) {
        super(result, type, canCraft);

        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern must have at least one element");
        }

        this.pattern = normalizedPatternList(pattern);
        this.height = this.pattern.size();
        this.width = this.pattern.stream().mapToInt(String::length).max().orElse(0);
        this.patternArray = toCharArray(this.pattern, height, width);

        this.ingredientByChar = new ItemQuantifiable[CHAR_SPACE];
        for (Map.Entry<Character, ItemQuantifiable> e : ingredientMap.entrySet()) {
            this.ingredientByChar[e.getKey() & 0xFF] = e.getValue();
        }
        this.ingredientByChar[' ' & 0xFF] = new ItemQuantifiable(ItemType.AIR, 0);
        this.ingredientByChar['O' & 0xFF] = new ItemQuantifiable(ItemType.AIR, 0);

        @SuppressWarnings("unchecked")
        Function<SkyBlockItem, Boolean>[] tmp = (Function<SkyBlockItem, Boolean>[]) new Function[CHAR_SPACE];
        this.extraReqByChar = tmp;
        this.symbolHasExtraReq = new boolean[CHAR_SPACE];

        char[][] mirrored = mirrorHorizontally(patternArray);
        this.variants = Arrays.deepEquals(mirrored, patternArray)
                ? new Variant[]{buildVariant(patternArray)}
                : new Variant[]{buildVariant(patternArray), buildVariant(mirrored)};

        this.recipeSize = height * width;
    }

    public ShapedRecipe(RecipeType type,
                        SkyBlockItem result,
                        Map<Character, ItemQuantifiable> ingredientMap,
                        List<String> pattern) {
        this(type, result, ingredientMap, pattern, (_) -> new CraftingResult(true, new String[]{}));
    }

    private char[][] mirrorHorizontally(char[][] source) {
        char[][] mirrored = new char[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                mirrored[row][col] = source[row][width - 1 - col];
            }
        }
        return mirrored;
    }

    private Variant buildVariant(char[][] shape) {
        int offsetRows = 3 - height + 1;
        int offsetCols = 3 - width + 1;
        int offsets = offsetRows * offsetCols;

        List<Integer> requiredPatternIdx = new ArrayList<>();
        List<Character> requiredSymbols = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                char symbol = shape[row][col];
                ItemQuantifiable ingredient = ingredientByChar[symbol & 0xFF];
                if (ingredient != null && ingredient.getItem().getMaterial() != Material.AIR) {
                    requiredPatternIdx.add(row * width + col);
                    requiredSymbols.add(symbol);
                }
            }
        }

        int[] airMaskByOffset = new int[offsets];
        int[][] requiredGridIdxByOffset = new int[offsets][];
        char[][] requiredSymByOffset = new char[offsets][];

        for (int startRow = 0; startRow < offsetRows; startRow++) {
            for (int startCol = 0; startCol < offsetCols; startCol++) {
                int offsetIndex = offsetIndex(startRow, startCol, offsetCols);
                airMaskByOffset[offsetIndex] = getAirMask(shape, startRow, startCol);

                int[] reqGrid = new int[requiredPatternIdx.size()];
                char[] reqSym = new char[requiredPatternIdx.size()];
                for (int i = 0; i < requiredPatternIdx.size(); i++) {
                    int patternIndex = requiredPatternIdx.get(i);
                    int patternRow = patternIndex / width;
                    int patternCol = patternIndex - patternRow * width;
                    reqGrid[i] = (startRow + patternRow) * 3 + (startCol + patternCol);
                    reqSym[i] = requiredSymbols.get(i);
                }
                requiredGridIdxByOffset[offsetIndex] = reqGrid;
                requiredSymByOffset[offsetIndex] = reqSym;
            }
        }

        return new Variant(airMaskByOffset, requiredGridIdxByOffset, requiredSymByOffset);
    }

    private int getAirMask(char[][] shape, int startRow, int startCol) {
        int airMask = 0;
        for (int gr = 0; gr < 3; gr++) {
            for (int gc = 0; gc < 3; gc++) {
                int idx = gr * 3 + gc;
                int pr = gr - startRow;
                int pc = gc - startCol;
                if (pr < 0 || pr >= height || pc < 0 || pc >= width) {
                    airMask |= (1 << idx);
                    continue;
                }

                char sym = shape[pr][pc];
                ItemQuantifiable iq = ingredientByChar[sym & 0xFF];
                if (sym == 'O' || iq == null || iq.getItem().getMaterial() == Material.AIR) {
                    airMask |= (1 << idx);
                }
            }
        }
        return airMask;
    }

    private static int offsetIndex(int startRow, int startCol, int offsetCols) {
        return startRow * offsetCols + startCol;
    }

    private List<String> normalizedPatternList(List<String> pattern) {
        int maxLength = pattern.stream().mapToInt(String::length).max().orElse(0);
        List<String> out = new ArrayList<>(pattern.size());
        for (String row : pattern) {
            if (row.length() < maxLength) {
                out.add(String.format("%-" + maxLength + "s", row));
            } else {
                out.add(row);
            }
        }
        return out;
    }

    private char[][] toCharArray(List<String> paddedPattern, int h, int w) {
        char[][] arr = new char[h][w];
        for (int i = 0; i < h; i++) {
            String row = paddedPattern.get(i);
            for (int j = 0; j < w; j++) {
                arr[i][j] = row.charAt(j);
            }
        }
        return arr;
    }

    public void addExtraRequirement(char patternChar, Function<SkyBlockItem, Boolean> requirement) {
        extraReqByChar[patternChar & 0xFF] = requirement;
        symbolHasExtraReq[patternChar & 0xFF] = true;
    }

    @Override
    public ShapedRecipe setResult(SkyBlockItem result) {
        this.result = result;
        return this;
    }

    @Override
    public void init() {
        CACHED_RECIPES.add(this);
    }

    private static int nonAirMask(ItemStack[] stacks) {
        int mask = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack s = stacks[i];
            if (s != null && s.material() != Material.AIR) {
                mask |= (1 << i);
            }
        }
        return mask;
    }

    private boolean matchesOffset(Variant variant, ItemStack[] stacks, int offset, int nonAir) {
        if ((nonAir & variant.airMaskByOffset()[offset]) != 0) return false;

        int[] reqIdx = variant.requiredGridIdxByOffset()[offset];
        char[] reqSym = variant.requiredSymByOffset()[offset];

        for (int i = 0; i < reqIdx.length; i++) {
            ItemStack actualStack = stacks[reqIdx[i]];
            if (actualStack == null || actualStack.material() == Material.AIR) return false;

            char sym = reqSym[i];
            ItemQuantifiable expected = ingredientByChar[sym & 0xFF];
            if (expected == null) return false;

            if (actualStack.amount() < expected.getAmount()) return false;

            SkyBlockItem actualItem = new SkyBlockItem(actualStack);
            if (!expected.matchesType(actualItem)) {
                if (!ExchangeableType.isExchangeable(
                        actualItem.getAttributeHandler().getPotentialType(),
                        expected.getItem().getAttributeHandler().getPotentialType())) {
                    return false;
                }
            }

            if (symbolHasExtraReq[sym & 0xFF]) {
                Function<SkyBlockItem, Boolean> req = extraReqByChar[sym & 0xFF];
                if (req != null && !req.apply(actualItem)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public SkyBlockItem[] consume(SkyBlockItem[] stacks) {
        ItemStack[] rawStacks = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) rawStacks[i] = (stacks[i] != null) ? stacks[i].getItemStack() : null;

        int nonAir = nonAirMask(rawStacks);
        int offsets = offsetCount();

        for (Variant variant : variants) {
            for (int offset = 0; offset < offsets; offset++) {
                if (!matchesOffset(variant, rawStacks, offset, nonAir)) continue;

                SkyBlockItem[] resultStacks = Arrays.copyOf(stacks, stacks.length);
                int[] reqIdx = variant.requiredGridIdxByOffset()[offset];
                char[] reqSym = variant.requiredSymByOffset()[offset];

                for (int i = 0; i < reqIdx.length; i++) {
                    int idx = reqIdx[i];
                    ItemQuantifiable required = ingredientByChar[reqSym[i] & 0xFF];
                    if (required == null) continue;

                    SkyBlockItem slot = resultStacks[idx];
                    if (slot == null || slot.getMaterial() == Material.AIR) continue;

                    int newAmount = slot.getAmount() - required.getAmount();
                    resultStacks[idx] = (newAmount > 0) ?
                            new SkyBlockItem(slot.getItemStack().withAmount(newAmount))
                            : null;
                }

                return resultStacks;
            }
        }

        throw new IllegalStateException("Not enough materials to consume!");
    }

    @Override
    public SkyBlockItem[] getRecipeDisplay() {
        SkyBlockItem[] display = new SkyBlockItem[9];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                char symbol = patternArray[r][c];
                ItemQuantifiable itemQ = ingredientByChar[symbol & 0xFF];

                if (itemQ != null) {
                    SkyBlockItem item = itemQ.getItem();
                    if (item != null && item.getMaterial() != Material.AIR) {
                        display[r * 3 + c] = item.clone();
                    }
                }
            }
        }

        return display;
    }

    @Override
    public SkyBlockRecipe<?> clone() {
        List<String> patternCopy = new ArrayList<>(this.pattern);
        Map<Character, ItemQuantifiable> ingredientMapCopy = new HashMap<>();
        for (int i = 0; i < CHAR_SPACE; i++) {
            ItemQuantifiable iq = ingredientByChar[i];
            if (iq != null) ingredientMapCopy.put((char) i, iq.clone());
        }

        ShapedRecipe cloned = new ShapedRecipe(recipeType, result.clone(), ingredientMapCopy, patternCopy, canCraft);
        for (int i = 0; i < CHAR_SPACE; i++) {
            Function<SkyBlockItem, Boolean> req = extraReqByChar[i];
            if (req != null) cloned.addExtraRequirement((char) i, req);
        }
        cloned.amount = this.amount;
        cloned.setCraftingRequirements(getCraftingRequirements());
        return cloned;
    }

    public static ShapedRecipe parseShapedRecipe(ItemStack[] stacks) {
        ShapedRecipe best = null;
        int bestSize = -1;

        int nonAir = nonAirMask(stacks);

        for (ShapedRecipe recipe : CACHED_RECIPES) {
            if (recipe.recipeSize <= bestSize) continue;

            if (recipe.matches(stacks, nonAir)) {
                best = recipe;
                bestSize = recipe.recipeSize;
            }
        }

        return best;
    }

    public Map<Character, List<Integer>> getPositionsOfItems(ItemStack[] stacks) {
        Map<Character, List<Integer>> positions = new HashMap<>();

        int nonAir = nonAirMask(stacks);
        int offsets = offsetCount();

        for (Variant variant : variants) {
            for (int offset = 0; offset < offsets; offset++) {
                if (!matchesOffset(variant, stacks, offset, nonAir)) continue;

                int[] reqIdx = variant.requiredGridIdxByOffset()[offset];
                char[] reqSym = variant.requiredSymByOffset()[offset];
                for (int i = 0; i < reqIdx.length; i++) {
                    positions.computeIfAbsent(reqSym[i], _ -> new ArrayList<>()).add(reqIdx[i]);
                }
            }
        }

        positions.replaceAll((_, v) -> new ArrayList<>(new LinkedHashSet<>(v)));
        return positions;
    }

    private boolean matches(ItemStack[] stacks, int nonAir) {
        int offsets = offsetCount();
        for (Variant variant : variants) {
            for (int offset = 0; offset < offsets; offset++) {
                if (matchesOffset(variant, stacks, offset, nonAir)) return true;
            }
        }
        return false;
    }

    private int offsetCount() {
        return (3 - height + 1) * (3 - width + 1);
    }

    @Override
    public String toString() {
        return "ShapedRecipe{" +
                "recipeType=" + recipeType +
                ", result=" + result +
                ", pattern=" + pattern +
                ", canCraft=" + canCraft +
                ", amount=" + amount +
                '}';
    }
}
