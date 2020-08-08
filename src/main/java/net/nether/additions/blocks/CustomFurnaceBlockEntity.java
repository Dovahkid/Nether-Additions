package net.nether.additions.blocks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.*;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.nether.additions.NetherAdditions;

import java.util.List;

//import javax.annotation.Nullable;


//TODO look at fast furnaces and other furnace examples to optimize furnace code
// vanilla furnace code is wasteful
public class CustomFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, Tickable {
    private static final int[] TOP_SLOTS = new int[]{0};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    private static final int[] SIDE_SLOTS = new int[]{1};
    protected DefaultedList<ItemStack> inventory;
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int cookTimeTotal;
    protected final PropertyDelegate propertyDelegate;
    private final Object2IntOpenHashMap<Identifier> recipesUsed;
    protected final RecipeType<? extends AbstractCookingRecipe> recipeType;

    public CustomFurnaceBlockEntity() {
        super(NetherAdditions.CUSTOM_FURNACE_ENTITY);
        this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return CustomFurnaceBlockEntity.this.burnTime;
                    case 1:
                        return CustomFurnaceBlockEntity.this.fuelTime;
                    case 2:
                        return CustomFurnaceBlockEntity.this.cookTime;
                    case 3:
                        return CustomFurnaceBlockEntity.this.cookTimeTotal;
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        CustomFurnaceBlockEntity.this.burnTime = value;
                        break;
                    case 1:
                        CustomFurnaceBlockEntity.this.fuelTime = value;
                        break;
                    case 2:
                        CustomFurnaceBlockEntity.this.cookTime = value;
                        break;
                    case 3:
                        CustomFurnaceBlockEntity.this.cookTimeTotal = value;
                }

            }

            public int size() {
                return 4;
            }
        };
        this.recipesUsed = new Object2IntOpenHashMap<>();
        this.recipeType = RecipeType.SMELTING;
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.fromTag(tag, this.inventory);
        this.burnTime = tag.getShort("BurnTime");
        this.cookTime = tag.getShort("CookTime");
        this.cookTimeTotal = tag.getShort("CookTimeTotal");
        this.fuelTime = this.getFuelTime((ItemStack)this.inventory.get(1));
        CompoundTag compoundTag = tag.getCompound("RecipesUsed");
        for (String usedRecipe : compoundTag.getKeys()) {
            this.recipesUsed.put(new Identifier(usedRecipe), compoundTag.getInt(usedRecipe));
        }
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putShort("BurnTime", (short)this.burnTime);
        tag.putShort("CookTime", (short)this.cookTime);
        tag.putShort("CookTimeTotal", (short)this.cookTimeTotal);
        Inventories.toTag(tag, this.inventory);
        CompoundTag compoundTag = new CompoundTag();
        this.recipesUsed.forEach((identifier, integer) -> {
            compoundTag.putInt(identifier.toString(), integer);
        });
        tag.put("RecipesUsed", compoundTag);
        return tag;
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.netheradditions.furnace");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    public void tick() {
        boolean wasBurning = this.isBurning();
        boolean updated = false;
        if (this.isBurning()) {
            --this.burnTime;
        }

        if (!this.world.isClient) {
            ItemStack itemStack = this.inventory.get(1);
            if (!this.isBurning() && (itemStack.isEmpty() || (this.inventory.get(0)).isEmpty())) {
                if (!this.isBurning() && this.cookTime > 0) {
                    this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.cookTimeTotal);
                }
            } else {
                Recipe<?> recipe = this.world.getRecipeManager().getFirstMatch(this.recipeType, this, this.world).orElse(null);
                if (!this.isBurning() && this.canAcceptRecipeOutput(recipe)) {
                    this.burnTime = this.getFuelTime(itemStack);
                    this.fuelTime = this.burnTime;
                    if (this.isBurning()) {
                        updated = true;
                        if (!itemStack.isEmpty()) {
                            Item item = itemStack.getItem();
                            itemStack.decrement(1);
                            if (itemStack.isEmpty()) {
                                Item item2 = item.getRecipeRemainder();
                                this.inventory.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canAcceptRecipeOutput(recipe)) {
                    ++this.cookTime;
                    if (this.cookTime == this.cookTimeTotal) {
                        this.cookTime = 0;
                        this.cookTimeTotal = this.getCookTime();
                        this.craftRecipe(recipe);
                        updated = true;
                    }
                } else {
                    this.cookTime = 0;
                }
            }

            if (wasBurning != this.isBurning()) {
                updated = true;
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
            }
        }

        if (updated) {
            this.markDirty();
        }

    }

    protected boolean canAcceptRecipeOutput(Recipe<?> recipe) {
        if (!(this.inventory.get(0)).isEmpty() && recipe != null) {
            ItemStack itemStack = recipe.getOutput();
            if (itemStack.isEmpty()) {
                return false;
            } else {
                ItemStack itemStack2 = this.inventory.get(2);
                if (itemStack2.isEmpty()) {
                    return true;
                } else if (!itemStack2.isItemEqualIgnoreDamage(itemStack)) {
                    return false;
                } else if (itemStack2.getCount() < this.getMaxCountPerStack() && itemStack2.getCount() < itemStack2.getMaxCount()) {
                    return true;
                } else {
                    return itemStack2.getCount() < itemStack.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    private void craftRecipe(Recipe<?> recipe) {
        if (recipe != null && this.canAcceptRecipeOutput(recipe)) {
            ItemStack itemStack = this.inventory.get(0);
            ItemStack itemStack2 = recipe.getOutput();
            ItemStack itemStack3 = this.inventory.get(2);
            if (itemStack3.isEmpty()) {
                this.inventory.set(2, itemStack2.copy());
            } else if (itemStack3.getItem() == itemStack2.getItem()) {
                itemStack3.increment(1);
            }

            if (!this.world.isClient) {
                this.setLastRecipe(recipe);
            }

            if (itemStack.getItem() == Blocks.WET_SPONGE.asItem() && !((ItemStack)this.inventory.get(1)).isEmpty() && ((ItemStack)this.inventory.get(1)).getItem() == Items.BUCKET) {
                this.inventory.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemStack.decrement(1);
        }
    }

    protected int getFuelTime(ItemStack fuel) {
        if (!fuel.isEmpty()) {
            return FuelRegistry.INSTANCE.get(fuel.getItem());
        }
        return 0;
    }

    protected int getCookTime() {
        return this.world.getRecipeManager().getFirstMatch(this.recipeType, this, this.world).map(AbstractCookingRecipe::getCookTime).orElse(200);
    }

    public static boolean canUseAsFuel(ItemStack stack) {
        Integer i= FuelRegistry.INSTANCE.get(stack.getItem());
        return (i != null && i > 0);
    }

    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return this.isValid(slot, stack);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            Item item = stack.getItem();
            if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
                return false;
            }
        }

        return true;
    }

    public int size() {
        return this.inventory.size();
    }

    public boolean isEmpty() {
        for(ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && stack.isItemEqualIgnoreDamage(itemStack) && ItemStack.areTagsEqual(stack, itemStack);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

        if (slot == 0 && !bl) {
            this.cookTimeTotal = this.getCookTime();
            this.cookTime = 0;
            this.markDirty();
        }

    }

    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 2) {
            return false;
        } else if (slot != 1) {
            return true;
        } else {
            ItemStack itemStack = this.inventory.get(1);
            return canUseAsFuel(stack) || stack.getItem() == Items.BUCKET && itemStack.getItem() != Items.BUCKET;
        }
    }

    public void clear() {
        this.inventory.clear();
    }

    public void setLastRecipe(Recipe<?> recipe) {
        if (recipe != null) {
            Identifier identifier = recipe.getId();
            this.recipesUsed.addTo(identifier, 1);
        }

    }

    public Recipe<?> getLastRecipe() {
        return null;
    }

    public void unlockLastRecipe(PlayerEntity player) {
    }

    public void dropExperience(PlayerEntity player) {
        List<Recipe<?>> list = this.method_27354(player.world, player.getPos());
        player.unlockRecipes(list);
        this.recipesUsed.clear();
    }

    public List<Recipe<?>> method_27354(World world, Vec3d vec3d) {
        List<Recipe<?>> list = Lists.newArrayList();

        for (Entry<Identifier> entry : this.recipesUsed.object2IntEntrySet()) {
            world.getRecipeManager().get(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                dropExperience(world, vec3d, entry.getIntValue(), ((AbstractCookingRecipe) recipe).getExperience());
            });
        }

        return list;
    }

    private static void dropExperience(World world, Vec3d vec3d, int i, float f) {
        int j = MathHelper.floor((float)i * f);
        float g = MathHelper.fractionalPart((float)i * f);
        if (g != 0.0F && Math.random() < (double)g) {
            ++j;
        }

        while(j > 0) {
            int k = ExperienceOrbEntity.roundToOrbSize(j);
            j -= k;
            world.spawnEntity(new ExperienceOrbEntity(world, vec3d.x, vec3d.y, vec3d.z, k));
        }

    }

    public void provideRecipeInputs(RecipeFinder finder) {
        for (ItemStack itemStack : this.inventory) {
            finder.addItem(itemStack);
        }
    }
}
