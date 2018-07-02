package org.ultramine.gradle.task;

import groovy.lang.Closure;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.ultramine.gradle.internal.UMFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

@ParallelizableTask
public class SpeicialClassTransformTask extends DefaultTask
{
	private File inputDir;
	@OutputDirectory
	private File outputDir = new File(getProject().getBuildDir(), getName());
	@Input
	private final List<ISpecialTransformer> transformers = new ArrayList<>();
	private final Map<String, List<ISpecialTransformer>> transformerMap = new HashMap<>();

	public File getInputDir()
	{
		return inputDir;
	}

	public void setInputDir(File inputDir)
	{
		this.inputDir = inputDir;
	}

	public File getOutputDir()
	{
		return outputDir;
	}

	public void setOutputDir(File outputDir)
	{
		this.outputDir = outputDir;
	}

	public List<ISpecialTransformer> getTransformers()
	{
		return transformers;
	}

	public void addTransformer(ISpecialTransformer transformer)
	{
		transformers.add(transformer);
		transformerMap.computeIfAbsent(transformer.getPath(), k -> new ArrayList<>()).add(transformer);
		getInputs().file(new Closure<File>(null, null)
		{
			@Override
			public File call()
			{
				return new File(inputDir, transformer.getPath());
			}
		});
	}

	@TaskAction
	void doAction(IncrementalTaskInputs inputs) throws IOException
	{
		if(!inputs.isIncremental())
		{
			FileUtils.cleanDirectory(outputDir);
			for(Map.Entry<String, List<ISpecialTransformer>> ent : transformerMap.entrySet())
			{
				processClass(ent.getKey(), ent.getValue());
			}
		}
		else
		{
			inputs.outOfDate((InputFileDetails detals) -> processClass(detals.getFile()));

			Set<File> dirsToCheck = new HashSet<File>();
			inputs.removed((InputFileDetails detals) -> {
				File file = new File(outputDir, getRelPath(detals.getFile()));
				file.delete();
				dirsToCheck.add(file.getParentFile());
			});

			for(File file : dirsToCheck)
				if(file.exists() && UMFileUtils.isDirEmpty(file.toPath()))
					file.delete();
		}
	}

	private String getRelPath(File file)
	{
		return UMFileUtils.getRelativePath(inputDir, file);
	}

	private void processClass(File file)
	{
		if(file.isDirectory())
			return;
		String path = getRelPath(file);
		processClass(path, transformerMap.get(file));
	}

	private void processClass(String path, List<ISpecialTransformer> transfs)
	{
		if(transfs == null || transfs.isEmpty())
			return;
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(FileUtils.readFileToByteArray(new File(inputDir, path)));
			classReader.accept(classNode, 0);

			int flags = 0;
			for(ISpecialTransformer transformer : transfs)
			{
				transformer.transform(classNode);
				flags |= transformer.getWriteFlags();
			}

			ClassWriter writer = new ClassWriter(flags);
			classNode.accept(writer);
			FileUtils.writeByteArrayToFile(new File(outputDir, path), writer.toByteArray());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void replace(Closure<Void> clsr)
	{
		ReplaceStringTransformer transformer = new ReplaceStringTransformer();
		clsr.setDelegate(transformer);
		clsr.call();
		addTransformer(transformer);
	}

	public interface ISpecialTransformer extends Serializable
	{
		/** @return path to target class file relative to input directory (with .class extension) */
		String getPath();
		void transform(ClassNode node);
		int getWriteFlags();
	}

	public static class ReplaceStringTransformer implements ISpecialTransformer
	{
		private String path;
		private Map<String, String> replaceMap = new HashMap<>();

		@Override
		public String getPath()
		{
			return path;
		}

		@Override
		public void transform(ClassNode node)
		{
			for(Object o : node.methods)
			{
				MethodNode m = (MethodNode) o;
				for(ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
				{
					AbstractInsnNode insnNode = it.next();
					if(insnNode.getOpcode() == Opcodes.LDC)
					{
						LdcInsnNode ldc = (LdcInsnNode)insnNode;
						String replacement;
						if(ldc.cst instanceof String && (replacement = replaceMap.get(ldc.cst)) != null)
							ldc.cst = replacement;
					}
				}
			}
		}

		@Override
		public int getWriteFlags()
		{
			return 0;
		}

		public void replaceIn(String path)
		{
			path = path.replace('.', '/');
			if(!path.endsWith(".class"))
				path += ".class";
			this.path = path;
		}

		public void replace(String search, String replacement)
		{
			replaceMap.put(search, replacement);
		}
	}
}
