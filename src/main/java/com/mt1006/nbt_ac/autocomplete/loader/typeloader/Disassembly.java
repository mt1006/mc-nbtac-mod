package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.loader.Loader;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Disassembly
{
	private static final int MAX_DISASSEMBLY_DEPTH = 16;
	private static final String METHOD_LOAD_SIGNATURE = "(L" + CompoundTag.class.getName().replace('.', '/') + ";)V";
	private static final String METHOD_LOAD_BLOCK_SIGNATURE = "(L" + CompoundTag.class.getName().replace('.', '/') +
			";L" + HolderLookup.Provider.class.getName().replace('.', '/') + ";)V";
	private static final String COMPOUND_TAG_SIGNATURE = CompoundTag.class.getName().replace('.', '/');
	private static final String LIST_TAG_SIGNATURE = ListTag.class.getName().replace('.', '/');
	private static final String COMPOUND_TAG_ARG_SIGNATURE = CompoundTag.class.getName();
	private static final String LIST_TAG_ARG_SIGNATURE = ListTag.class.getName();
	private static final String STRING_ARG_SIGNATURE = String.class.getName();
	private static final Map<String, ClassNode> classMap = new HashMap<>();
	private static final Stack<String> disassemblingStack = new Stack<>();
	private static final Map<String, Template> fullTemplateMap = new HashMap<>();
	private static final Map<String, Template> partialTemplateMap = new HashMap<>();
	private static String blockEntityLoadMethod = null;

	public static void init()
	{
		try
		{
			MethodNode methodNode = loadMethod(loadClass(BlockEntity.class.getName(), null), null,
					METHOD_LOAD_BLOCK_SIGNATURE, Opcodes.ACC_PROTECTED, false);
			if (methodNode == null) { throw new Exception("Failed to find \"load\" method"); }
			blockEntityLoadMethod = methodNode.name;
		}
		catch (Exception exception)
		{
			NBTac.LOGGER.error("Failed to initialize disassembler: {}", exception.toString());
			Loader.printStackTrace(exception);
		}
	}

	public static void clear()
	{
		classMap.clear();
		fullTemplateMap.clear();
		partialTemplateMap.clear();
	}

	public static ClassNode loadClass(String className, @Nullable Class<?> clazz) throws IOException
	{
		className = className.replace('/', '.');

		ClassNode existingNode = classMap.get(className);
		if (existingNode != null) { return existingNode; }

		String classPath = className.replace('.', '/') + ".class";
		ClassReader reader;

		try
		{
			if (clazz == null) { clazz = Class.forName(className); }
			InputStream classStream = clazz.getClassLoader().getResourceAsStream(classPath);
			if (classStream == null) { throw new ClassNotFoundException(); }
			reader = new ClassReader(classStream);
			classStream.close();
		}
		catch (ClassNotFoundException exception)
		{
			throw new IOException("Class not found! - " + classPath);
		}

		ClassNode node = new ClassNode(Opcodes.ASM9);
		reader.accept(node, 0);

		classMap.put(className, node);
		return node;
	}

	private static @Nullable MethodNode loadMethod(ClassNode classNode, @Nullable String methodName, @Nullable String methodSignature,
												   int accessFlags, boolean checkSuperclasses)
	{
		for (MethodNode methodNode : classNode.methods)
		{
			if ((methodName == null || methodName.equals(methodNode.name))
					&& (methodSignature == null || methodSignature.equals(methodNode.desc))
					&& (accessFlags == 0 || (methodNode.access & accessFlags) != 0))
			{
				return methodNode;
			}
		}

		if (checkSuperclasses)
		{
			if (classNode.superName == null || classNode.superName.equals("java/lang/Object")) { return null; }

			try
			{
				MethodNode node = loadMethod(loadClass(classNode.superName, null), methodName, methodSignature, accessFlags, true);
				if (node != null) { return node; }
			}
			catch (Exception ignore) {}

			try
			{
				for (String interfaceName : classNode.interfaces)
				{
					MethodNode node = loadMethod(loadClass(interfaceName, null), methodName, methodSignature, accessFlags, true);
					if (node != null) { return node; }
				}
			}
			catch (Exception ignore) {}
		}
		return null;
	}

	public static void disassemblyEntity(Class<?> clazz, NbtSuggestions arg) throws Exception
	{
		disassemblyLoadMethod(Entity.class, null, METHOD_LOAD_SIGNATURE, Opcodes.ACC_PUBLIC, clazz, arg);
	}

	public static void disassemblyBlockEntity(Class<?> clazz, NbtSuggestions arg) throws Exception
	{
		disassemblyLoadMethod(clazz, blockEntityLoadMethod, METHOD_LOAD_BLOCK_SIGNATURE,
				Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC, clazz, arg);
	}

	public static void disassemblyLoadMethod(Class<?> clazz, @Nullable String methodName, String methodSignature,
											 int accessFlags, Class<?> objectClass, NbtSuggestions arg) throws Exception
	{
		Template templates = new Template();
		disassembly(clazz.getName(), methodName, methodSignature, accessFlags,
				objectClass.getName(), new MethodArgs(templates, null), 0, false, clazz);
		templates.applyTemplate(arg);
	}

	//TODO: clean up
	public static void disassembly(String className, @Nullable String methodName, String methodSignature,
								   int accessFlags, @Nullable String objectClass, MethodArgs args, int depth,
								   boolean uncertain, @Nullable Class<?> clazz) throws Exception
	{
		String methodID = String.format("%s %s %s", className, methodName, methodSignature);
		String methodFullID = String.format("%s %s", methodID, objectClass);

		if (disassemblingStack.contains(methodFullID))
		{
			//TODO: handle nbt recursion
			if (ModConfig.debugMode.val) { NBTac.LOGGER.warn("Already disassembled! - {}", methodFullID); }
			return;
		}

		if (depth >= MAX_DISASSEMBLY_DEPTH)
		{
			if (ModConfig.debugMode.val) { NBTac.LOGGER.warn("Too deep! - {}", methodFullID); }
			return;
		}

		ClassNode classNode = loadClass(className, clazz);
		MethodNode method = loadMethod(classNode, methodName, methodSignature, accessFlags, true);

		if (method == null)
		{
			String methodInfo = methodName != null ? methodName : "with signature ";
			throw new Exception("Unable to find superclass of " + className + " containing method " + methodInfo + methodSignature);
		}

		disassemblingStack.push(methodFullID);
		readMethod(classNode, method, args, objectClass, depth, uncertain, methodID, methodFullID);
		disassemblingStack.pop();
	}

	private static void readMethod(ClassNode classNode, MethodNode methodNode, MethodArgs args, @Nullable String objectClass,
								   int depth, boolean uncertain, String methodID, String methodFullID) throws Exception
	{
		Template fullTemplate = fullTemplateMap.get(methodFullID);
		if (fullTemplate != null)
		{
			args.compound.merge(fullTemplate, args.string);
			return;
		}

		Template partialTemplate = partialTemplateMap.get(methodID);
		Template template;
		List<InvokeInfo> invokes;

		if (partialTemplate != null)
		{
			template = partialTemplate.copy(null, false, null);
			invokes = template.invokes != null ? template.invokes : List.of();
		}
		else
		{
			template = new Template();
			ValueTracker valueTracker = new ValueTracker(template, uncertain, false);
			Analyzer<TrackedValue> analyzer = new Analyzer<>(valueTracker);

			analyzer.analyze(classNode.name, methodNode);
			invokes = valueTracker.invokes;

			// Invokes that don't depend on objectClass value
			for (InvokeInfo invoke : invokes)
			{
				if (invoke.insn.getOpcode() != Opcodes.INVOKEVIRTUAL && !invoke.calledOnThis)
				{
					disassembly(invoke.insn.owner, invoke.insn.name, invoke.insn.desc, 0, null, invoke.args, depth + 1, uncertain, null);
				}
			}

			partialTemplateMap.put(methodID, template.copy(null, false, null));
		}

		// Invokes that depend on objectClass value
		for (InvokeInfo invoke : invokes)
		{
			if (invoke.insn.getOpcode() == Opcodes.INVOKEVIRTUAL)
			{
				if (invoke.calledOnThis && objectClass != null)
				{
					disassembly(objectClass, invoke.insn.name, invoke.insn.desc, 0, objectClass, invoke.args, depth + 1, uncertain, null);
				}
				else
				{
					disassembly(invoke.insn.owner, invoke.insn.name, invoke.insn.desc, 0, null, invoke.args, depth + 1, true, null);
				}
			}
			else if (invoke.calledOnThis)
			{
				disassembly(invoke.insn.owner, invoke.insn.name, invoke.insn.desc, 0, objectClass, invoke.args, depth + 1, uncertain, null);
			}
		}

		fullTemplateMap.put(methodFullID, template);
		args.compound.merge(template, args.string);
	}

	private static boolean isHiddenTag(String tag)
	{
		return (ModConfig.hideForgeTags.val
				&& (tag.equals("ForgeCaps") || tag.equals("ForgeData") || tag.startsWith("forge:")
					|| tag.startsWith("neoforge:") || tag.equals("NeoForgeData")));
	}

	// Credits to: https://stackoverflow.com/a/48806265/18214530
	public static class ValueTracker extends Interpreter<TrackedValue>
	{
		private final @Nullable Template arg;
		private final boolean uncertain;
		private final boolean keepAllInvokes;
		private final BasicInterpreter basicInterpreter = new BasicInterpreter();
		private final Set<AbstractInsnNode> insnSet = new HashSet<>();
		public final List<InvokeInfo> invokes = new ArrayList<>();

		public ValueTracker(@Nullable Template arg, boolean uncertain, boolean keepAllInvokes)
		{
			super(Opcodes.ASM9);
			this.arg = arg;
			this.uncertain = uncertain;
			this.keepAllInvokes = keepAllInvokes;

			if (arg != null) { arg.invokes = invokes; }
		}

		@Override public TrackedValue newValue(Type type)
		{
			return TrackedValue.unknown(basicInterpreter.newValue(type));
		}

		@Override public TrackedValue newParameterValue(final boolean isInstanceMethod, final int local, final Type type)
		{
			BasicValue basicValue = basicInterpreter.newParameterValue(isInstanceMethod, local, type);

			if (isInstanceMethod && local == 0)
			{
				return TrackedValue.create(TrackedValue.Type.THIS, null, basicValue);
			}
			else if (type.getClassName().equals(COMPOUND_TAG_ARG_SIGNATURE))
			{
				if (arg != null) { return TrackedValue.create(TrackedValue.Type.COMPOUND, arg, basicValue); }
			}
			else if (type.getClassName().equals(LIST_TAG_ARG_SIGNATURE))
			{
				if (arg != null) { return TrackedValue.create(TrackedValue.Type.LIST_TAG, arg, basicValue);}
			}
			else if (type.getClassName().equals(STRING_ARG_SIGNATURE))
			{
				return TrackedValue.create(TrackedValue.Type.STRING, "*", basicValue);
			}

			return TrackedValue.unknown(basicValue);
		}

		@Override public TrackedValue newOperation(AbstractInsnNode insn) throws AnalyzerException
		{
			BasicValue basicValue = basicInterpreter.newOperation(insn);

			switch (insn.getOpcode())
			{
				case Opcodes.ICONST_M1:
				case Opcodes.ICONST_0:
				case Opcodes.ICONST_1:
				case Opcodes.ICONST_2:
				case Opcodes.ICONST_3:
				case Opcodes.ICONST_4:
				case Opcodes.ICONST_5:
					return TrackedValue.create(TrackedValue.Type.INTEGER, insn.getOpcode() - Opcodes.ICONST_0, basicValue);

				case Opcodes.BIPUSH:
				case Opcodes.SIPUSH:
					return TrackedValue.create(TrackedValue.Type.INTEGER, ((IntInsnNode)insn).operand, basicValue);

				case Opcodes.LDC:
					Object ldcVal = ((LdcInsnNode)insn).cst;
					if (ldcVal instanceof Integer) { return TrackedValue.create(TrackedValue.Type.INTEGER, ldcVal, basicValue); }
					else if (ldcVal instanceof String) { return TrackedValue.create(TrackedValue.Type.STRING, ldcVal, basicValue); }
					break;
			}

			return TrackedValue.unknown(basicValue);
		}

		@Override public TrackedValue copyOperation(AbstractInsnNode insn, TrackedValue value)
		{
			return value;
		}

		@Override public TrackedValue unaryOperation(AbstractInsnNode insn, TrackedValue value) throws AnalyzerException
		{
			BasicValue basicValue = basicInterpreter.unaryOperation(insn, value.basicValue);
			if (insn.getOpcode() == Opcodes.CHECKCAST) { return TrackedValue.copy(value, basicValue); }
			else { return TrackedValue.unknown(basicValue); }
		}

		@Override public TrackedValue binaryOperation(AbstractInsnNode insn, TrackedValue value1, TrackedValue value2) throws AnalyzerException
		{
			BasicValue typeValue = basicInterpreter.binaryOperation(insn, value1.basicValue, value2.basicValue);
			return TrackedValue.unknown(typeValue);
		}

		@Override public TrackedValue ternaryOperation(AbstractInsnNode insn, TrackedValue value1, TrackedValue value2, TrackedValue value3)
		{
			return null;
		}

		@Override public TrackedValue naryOperation(AbstractInsnNode insn, List<? extends TrackedValue> values) throws AnalyzerException
		{
			BasicValue basicValue = basicInterpreter.naryOperation(insn, null);

			// Ignores INVOKEINTERFACE, INVOKEDYNAMIC, MULTIANEWARRAY and other unexpected opcodes
			if (insnSet.add(insn)
					&& (insn.getOpcode() == Opcodes.INVOKEVIRTUAL
					|| insn.getOpcode() == Opcodes.INVOKESTATIC
					|| insn.getOpcode() == Opcodes.INVOKESPECIAL))
			{
				MethodInsnNode methodInsn = (MethodInsnNode)insn;

				if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && !keepAllInvokes)
				{
					if (methodInsn.owner.equals(COMPOUND_TAG_SIGNATURE)) { return onCompoundTagInvoke(methodInsn, values, basicValue); }
					else if (methodInsn.owner.equals(LIST_TAG_SIGNATURE)) { return onListTagInvoke(methodInsn, values, basicValue); }
				}

				InvokeInfo.add(invokes, methodInsn, values, keepAllInvokes);
			}

			return TrackedValue.unknown(basicValue);
		}

		@Override public void returnOperation(AbstractInsnNode insn, TrackedValue value, TrackedValue expected) {}

		@Override public TrackedValue merge(TrackedValue value1, TrackedValue value2)
		{
			if (value1.equals(value2)) { return value1; }
			BasicValue basicValue = basicInterpreter.merge(value1.basicValue, value2.basicValue);

			if (basicValue.equals(value1.basicValue) && (value1.basicValue.equals(value2.basicValue))) { return value1; }
			else if (basicValue.equals(value2.basicValue) && (value2.basicValue.equals(value1.basicValue))) { return value2; }
			else { return TrackedValue.unknown(basicValue); }
		}

		private TrackedValue onCompoundTagInvoke(MethodInsnNode methodInsn, List<? extends TrackedValue> values, BasicValue basicValue)
		{
			if (values.size() < 2 || values.get(0).type != TrackedValue.Type.COMPOUND || values.get(1).type != TrackedValue.Type.STRING)
			{
				return TrackedValue.unknown(basicValue);
			}

			NbtSuggestion.Type type = NbtSuggestion.Type.fromMethodName(methodInsn.name);
			NbtSuggestion.Type listType = NbtSuggestion.Type.UNKNOWN;

			if (type == NbtSuggestion.Type.LIST && values.size() == 3)
			{
				if (values.get(2).type == TrackedValue.Type.INTEGER)
				{
					listType = NbtSuggestion.Type.fromID(((Integer)values.get(2).object).byteValue());
				}
			}
			else if (values.size() != 2)
			{
				return TrackedValue.unknown(basicValue);
			}

			Template suggestions = (Template)values.get(0).object;
			String tagName = (String)values.get(1).object;

			if (isHiddenTag(tagName)) { return TrackedValue.unknown(basicValue); }

			SuggestionTemplate newSuggestion;
			if (uncertain) { newSuggestion = new SuggestionTemplate(tagName, type, NbtSuggestion.Source.UNCERTAIN); }
			else { newSuggestion = new SuggestionTemplate(tagName, type); }

			suggestions.addSuggestion(newSuggestion);

			if (type == NbtSuggestion.Type.COMPOUND)
			{
				return TrackedValue.create(TrackedValue.Type.COMPOUND, newSuggestion.addSubcompound(), basicValue);
			}
			else if (type == NbtSuggestion.Type.LIST)
			{
				newSuggestion.listType = listType;
				if (listType == NbtSuggestion.Type.COMPOUND)
				{
					return TrackedValue.create(TrackedValue.Type.LIST_TAG, newSuggestion.addSubcompound(), basicValue);
				}
			}

			return TrackedValue.unknown(basicValue);
		}

		private TrackedValue onListTagInvoke(MethodInsnNode methodInsn, List<? extends TrackedValue> values, BasicValue basicValue)
		{
			if (values.size() != 2 || values.get(0).type != TrackedValue.Type.LIST_TAG ||
					!methodInsn.desc.equals("(I)L" + CompoundTag.class.getName().replace('.', '/') + ";")) // getCompound
			{
				return TrackedValue.unknown(basicValue);
			}

			return TrackedValue.create(TrackedValue.Type.COMPOUND, values.get(0).object, basicValue);
		}
	}

	public static class TrackedValue implements Value
	{
		public final TrackedValue.Type type;
		public final Object object;
		public final BasicValue basicValue;

		private TrackedValue(TrackedValue.Type type, Object object, BasicValue basicValue)
		{
			this.type = type;
			this.object = object;
			this.basicValue = basicValue;
		}

		public static @Nullable TrackedValue create(TrackedValue.Type type, Object object, @Nullable BasicValue basicValue)
		{
			return basicValue != null ? new TrackedValue(type, object, basicValue) : null;
		}

		public static @Nullable TrackedValue copy(TrackedValue trackedValue, @Nullable BasicValue basicValue)
		{
			return basicValue != null ? new TrackedValue(trackedValue.type, trackedValue.object, basicValue) : null;
		}

		public static @Nullable TrackedValue unknown(@Nullable BasicValue basicValue)
		{
			return basicValue != null ? new TrackedValue(Type.UNKNOWN, null, basicValue) : null;
		}

		@Override public boolean equals(Object obj)
		{
			if (this == obj) { return true; }
			if (!(obj instanceof TrackedValue)) { return false; }

			TrackedValue valueToCompare = (TrackedValue)obj;
			if (type != valueToCompare.type) { return false; }
			if ((object == null) != (valueToCompare.object == null)) { return false; }
			if (object != null && !object.equals(valueToCompare.object)) { return false; }
			return basicValue.equals(valueToCompare.basicValue);
		}

		@Override public int getSize()
		{
			return basicValue.getSize();
		}

		public enum Type
		{
			STRING,     // String
			INTEGER,    // Integer
			COMPOUND,   // CompoundTemplate
			LIST_TAG,   // CompoundTemplate
			THIS,       // null
			UNKNOWN     // null
		}
	}

	public static class InvokeInfo
	{
		public final MethodInsnNode insn;
		public final MethodArgs args;
		public final boolean calledOnThis;

		public InvokeInfo(MethodInsnNode insn, MethodArgs args, boolean calledOnThis)
		{
			this.insn = insn;
			this.args = args;
			this.calledOnThis = calledOnThis;
		}

		public InvokeInfo copy(Template newCompound)
		{
			return new InvokeInfo(insn, new MethodArgs(newCompound, args.string), calledOnThis);
		}

		public static void add(List<InvokeInfo> invokes, MethodInsnNode insn, List<? extends TrackedValue> values, boolean keepAllInvokes)
		{
			MethodArgs args = MethodArgs.getArgs(values, insn.getOpcode() == Opcodes.INVOKESTATIC);
			boolean calledOnThis = insn.getOpcode() != Opcodes.INVOKESTATIC && isCalledOnThis(values);
			if (args != null || keepAllInvokes) { invokes.add(new InvokeInfo(insn, args, calledOnThis)); }
		}

		private static boolean isCalledOnThis(List<? extends TrackedValue> values)
		{
			return !values.isEmpty() && values.get(0).type == TrackedValue.Type.THIS;
		}
	}

	public static class SuggestionTemplate
	{
		public String tag;
		public NbtSuggestion.Type type;
		public NbtSuggestion.Type listType = NbtSuggestion.Type.UNKNOWN;
		public Template subcompound = null;
		public NbtSuggestion.Source source = NbtSuggestion.Source.DEFAULT;

		public SuggestionTemplate(String tag, NbtSuggestion.Type type)
		{
			this.tag = tag;
			this.type = type;
		}

		public SuggestionTemplate(String tag, NbtSuggestion.Type type, NbtSuggestion.Source source)
		{
			this(tag, type);
			this.source = source;
		}

		public Template addSubcompound()
		{
			subcompound = new Template();
			return subcompound;
		}

		public NbtSuggestion applyTemplate()
		{
			NbtSuggestion nbtSuggestion = new NbtSuggestion(tag, type, source, listType);
			if (subcompound != null && !subcompound.suggestions.isEmpty())
			{
				subcompound.applyTemplate(nbtSuggestion.getSubcompound());
			}
			return nbtSuggestion;
		}

		public @Nullable SuggestionTemplate copy(@Nullable Map<Template, Template> templateMap, boolean replace, @Nullable String toReplace)
		{
			String newTag;
			if (replace && tag.equals("*"))
			{
				if (toReplace != null && !isHiddenTag(toReplace)) { newTag = toReplace; }
				else { return null; }
			}
			else
			{
				newTag = tag;
			}

			SuggestionTemplate newTemplate = new SuggestionTemplate(newTag, type, source);
			newTemplate.listType = listType;
			if (subcompound != null) { newTemplate.subcompound = subcompound.copy(templateMap, replace, toReplace); }
			return newTemplate;
		}
	}

	public static class Template
	{
		public final List<SuggestionTemplate> suggestions = new ArrayList<>();
		public @Nullable List<InvokeInfo> invokes = null;

		public void addSuggestion(@Nullable SuggestionTemplate suggestion)
		{
			if (suggestion == null) { return; }
			suggestions.removeIf((listElement) -> listElement.tag.equals(suggestion.tag));
			suggestions.add(suggestion);
		}

		public void merge(Template templates, @Nullable String toReplace)
		{
			templates.suggestions.forEach((template) -> addSuggestion(template.copy(null, true, toReplace)));
		}

		public void applyTemplate(NbtSuggestions nbtSuggestions)
		{
			suggestions.forEach((template) -> nbtSuggestions.add(template.applyTemplate()));
		}

		public Template copy(@Nullable Map<Template, Template> templateMap, boolean replace, @Nullable String toReplace)
		{
			Template template = new Template();

			if (templateMap == null && invokes != null) { templateMap = new IdentityHashMap<>(); }
			if (templateMap != null) { templateMap.put(this, template); }

			for (SuggestionTemplate suggestionTemplate : suggestions)
			{
				template.addSuggestion(suggestionTemplate.copy(templateMap, replace, toReplace));
			}

			if (templateMap != null && invokes != null)
			{
				template.invokes = new ArrayList<>();
				for (InvokeInfo invoke : invokes)
				{
					Template newCompound = templateMap.get(invoke.args.compound);
					if (newCompound != null) { template.invokes.add(invoke.copy(newCompound)); }
				}
			}
			return template;
		}
	}

	public static class MethodArgs
	{
		public final Template compound;
		public final @Nullable String string;

		public MethodArgs(Template compound, @Nullable String string)
		{
			this.compound = compound;
			this.string = string;
		}

		public static @Nullable MethodArgs getArgs(List<? extends TrackedValue> values, boolean isStatic)
		{
			Template compound = null;
			String string = null;

			for (int i = (isStatic ? 0 : 1); i < values.size(); i++)
			{
				TrackedValue trackedValue = values.get(i);
				if (trackedValue.type == TrackedValue.Type.COMPOUND || trackedValue.type == TrackedValue.Type.LIST_TAG)
				{
					if (compound == null) { compound = (Template)trackedValue.object; }
				}
				else if (trackedValue.type == TrackedValue.Type.STRING)
				{
					if (string == null) { string = (String)trackedValue.object; }
				}
			}

			return compound != null ? new MethodArgs(compound, string) : null;
		}
	}
}