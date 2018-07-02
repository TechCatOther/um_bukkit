package org.ultramine.mods.bukkit.mixin.entity.passive;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ultramine.mods.bukkit.interfaces.entity.passive.IMixinEntityHorse;
import org.ultramine.mods.bukkit.interfaces.inventory.IMixinAnimalChest;

@Mixin(net.minecraft.entity.passive.EntityHorse.class)
public abstract class MixinEntityHorse implements IMixinEntityHorse
{
	@Shadow
	private static IAttribute horseJumpStrength;
	@Shadow
	public AnimalChest horseChest;

	@Shadow
	public abstract void func_110226_cD();

	public int maxDomestication = 100;

	@Override
	public void createChest()
	{
		func_110226_cD();
	}

	@Override
	public AnimalChest getHorseChest()
	{
		return horseChest;
	}

	@Override
	public void setHorseChest(AnimalChest horseChest)
	{
		this.horseChest = horseChest;
	}

	@Override
	public IAttribute getStaticHorseJumpStrength()
	{
		return horseJumpStrength;
	}

	@Override
	public int getMaxDomestication()
	{
		return maxDomestication;
	}

	@Override
	public void setMaxDomestication(int maxDomestication)
	{
		this.maxDomestication = maxDomestication;
	}

	@Overwrite
	public int getMaxTemper()
	{
		return this.maxDomestication;
	}

	@Inject(method = "readEntityFromNBT", at = @At("HEAD"))
	public void onReadEntityFromNBT(NBTTagCompound nbt, CallbackInfo ci)
	{
		if(nbt.hasKey("Bukkit.MaxDomestication"))
		{
			this.maxDomestication = nbt.getInteger("Bukkit.MaxDomestication");
		}
	}

	@Inject(method = "writeEntityToNBT", at = @At("HEAD"))
	public void onWriteEntityToNBT(NBTTagCompound nbt, CallbackInfo ci)
	{
		nbt.setInteger("Bukkit.MaxDomestication", this.maxDomestication);
	}

	@Inject(method = "func_110226_cD", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/AnimalChest;func_110133_a(Ljava/lang/String;)V", shift = Shift.BEFORE))
	private void func_110226_cDInject(CallbackInfo ci)
	{
		((IMixinAnimalChest) horseChest).setAnimal((EntityHorse)(Object) this);
	}
}
