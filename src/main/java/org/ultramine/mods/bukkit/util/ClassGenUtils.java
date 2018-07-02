package org.ultramine.mods.bukkit.util;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.ultramine.server.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

public class ClassGenUtils
{
	private static final Unsafe U = UnsafeUtil.getUnsafe();

	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> makeOneArgMethodDelegate(Class<?> base, String name, Method method, Class<T> iface)
	{
		return (Class<T>) U.defineAnonymousClass(base, makeOneArgMethodDelegate(name, method, iface), null);
	}

	public static byte[] makeOneArgMethodDelegate(String name, Method method, Class<?> iface)
	{
		if(method.getParameterTypes().length != 1)
			throw new IllegalArgumentException("Method " + method + " accepts more or less than 1 parameter");
		if(!iface.isInterface())
			throw new IllegalArgumentException("iface should be an interface");
		Method[] mtds = iface.getDeclaredMethods();
		Method frontMethod = null;
		for(Method m : mtds)
		{
			if(!m.isDefault())
			{
				if(frontMethod != null)
					throw new IllegalArgumentException("iface should be a functional interface, i.e. contains only one abstract method");
				frontMethod = m;
			}
		}
		if(frontMethod == null)
			throw new IllegalArgumentException("iface should be a functional interface, i.e. contains only one abstract method");
		if(frontMethod.getParameterTypes().length != 1)
			throw new IllegalArgumentException("Method " + frontMethod + " accepts more or less than 1 parameter");


		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String desc = name.replace('.', '/');
		String instType = Type.getInternalName(method.getDeclaringClass());
		String dstType = Type.getInternalName(method.getParameterTypes()[0]);
		String srcType = Type.getInternalName(frontMethod.getParameterTypes()[0]);

		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{Type.getInternalName(iface)});

		cw.visitSource(".dynamic", null);
		{
			cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, frontMethod.getName(), Type.getMethodDescriptor(frontMethod), null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, instType);
			mv.visitVarInsn(ALOAD, 1);
			if(!srcType.equals(dstType))
				mv.visitTypeInsn(CHECKCAST, dstType);
			mv.visitMethodInsn(INVOKEVIRTUAL, instType, method.getName(), Type.getMethodDescriptor(method), false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();
		return cw.toByteArray();
	}
}
