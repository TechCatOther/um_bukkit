package org.ultramine.mods.bukkit.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LightningBoltConstructorTransformer
{
	public static byte[] transformConstructor(byte[] targetClass)
	{
		ClassNode cNode = new ClassNode();
		new ClassReader(targetClass).accept(cNode, 0);
		for (MethodNode mNode : cNode.methods)
			if (mNode.name.equals("<init>"))
			{
				mNode.instructions = getInsertInstructions();
				break;
			}
		ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cNode.accept(cWriter);
		return cWriter.toByteArray();
	}

	private static InsnList getInsertInstructions()
	{
		InsnList newInstructions = new InsnList();
		newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		newInstructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/effect/EntityWeatherEffect", "<init>", "(Lnet/minecraft/world/World;)V", false));
		newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		newInstructions.add(new VarInsnNode(Opcodes.DLOAD, 2));
		newInstructions.add(new VarInsnNode(Opcodes.DLOAD, 4));
		newInstructions.add(new VarInsnNode(Opcodes.DLOAD, 6));
		newInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/effect/EntityLightningBolt", "constructorInject", "(Lnet/minecraft/world/World;DDD)V", false));
		newInstructions.add(new InsnNode(Opcodes.RETURN));
		return newInstructions;
	}
}
