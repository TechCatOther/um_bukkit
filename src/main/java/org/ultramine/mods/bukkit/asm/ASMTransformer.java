package org.ultramine.mods.bukkit.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.ultramine.mods.bukkit.asm.transformers.InventoryClassTransformer;
import org.ultramine.mods.bukkit.asm.transformers.LightningBoltConstructorTransformer;

public class ASMTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null)
			return null;
		if (transformedName.equals("net.minecraft.entity.effect.EntityLightningBolt"))
			return LightningBoltConstructorTransformer.transformConstructor(basicClass);
		if (doesClassImplementIInventory(basicClass))
			return InventoryClassTransformer.transformClass(basicClass);
		return basicClass;
	}

	private boolean doesClassImplementIInventory(byte[] someClass)
	{
		final boolean[] doesClassImplementIInventory = {false};
		ClassReader classReader = new ClassReader(someClass);
		final ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5)
		{
			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
			{
				for (String interfaceName : interfaces)
					if ((interfaceName.equals("rb") || interfaceName.equals("rl") || interfaceName.equals("net/minecraft/inventory/ISidedInventory") || interfaceName.equals("aph")) && (access & Opcodes.ACC_INTERFACE) == 0)
					{
						doesClassImplementIInventory[0] = true;
						break;
					}
				super.visit(version, access, name, signature, superName, interfaces);
			}
		};
		classReader.accept(classVisitor, 0);
		return doesClassImplementIInventory[0];
	}
}
