package com.mt1006.nbt_ac.autocomplete.loader.typeloader;

import com.mt1006.nbt_ac.NBTac;
import com.mt1006.nbt_ac.autocomplete.NbtSuggestions;
import com.mt1006.nbt_ac.autocomplete.suggestions.NbtSuggestion;
import com.mt1006.nbt_ac.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.util.ByteSequence;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Disassembly
{
	private static final boolean DEBUG_MESSAGES = false;
	private static final int MAX_DISASSEMBLY_DEPTH = 16;
	private static final String METHOD_LOAD_ENTITY_SIGNATURE = "(L" + CompoundTag.class.getName().replace('.', '/') + ";)V";
	private static final String METHOD_LOAD_BLOCK_ENTITY_SIGNATURE = "(L" +
			BlockState.class.getName().replace('.', '/') + ";L" + CompoundTag.class.getName().replace('.', '/') + ";)V";
	private static final String COMPOUND_TAG_SIGNATURE = CompoundTag.class.getName();
	private static final String LIST_TAG_SIGNATURE = ListTag.class.getName();
	private static final String COMPOUND_TAG_ARG_SIGNATURE = "L" + CompoundTag.class.getName().replace('.', '/') + ";";
	private static final String LIST_TAG_ARG_SIGNATURE = "L" + ListTag.class.getName().replace('.', '/') + ";";
	private static final Map<String, JavaClass> classMap = new HashMap<>();
	private static final Set<String> disassembledMethods = new HashSet<>();
	private static String blockEntityLoadName = null;

	public static void init()
	{
		Method method;

		try
		{
			method = findMethod(findClass(BlockEntity.class.getName()), null, METHOD_LOAD_BLOCK_ENTITY_SIGNATURE, false, true);
			if (method == null) { throw new Exception("Failed to find \"load\" method"); }
		}
		catch (Exception exception)
		{
			NBTac.LOGGER.error("Failed to initialize disassembler: " + exception);
			exception.printStackTrace();
			return;
		}

		blockEntityLoadName = method.getName();
	}

	public static void disassemblyEntity(Class<?> clazz, NbtSuggestions arg) throws Exception
	{
		disassembly(Entity.class.getName(), null, METHOD_LOAD_ENTITY_SIGNATURE, clazz.getName(), arg, 0, true, false);
	}

	public static void disassemblyBlockEntity(Class<?> clazz, NbtSuggestions arg) throws Exception
	{
		if (blockEntityLoadName == null) { return; }
		disassembly(clazz.getName(), blockEntityLoadName, METHOD_LOAD_BLOCK_ENTITY_SIGNATURE, clazz.getName(), arg, 0, true, false);
	}

	public static void disassembly(String className, String methodName, String methodSignature, String objectClassName,
								   NbtSuggestions arg, int depth, boolean mustBePublic, boolean uncertain) throws Exception
	{
		String methodID = String.format("%s %s %s %s", className, methodName, methodSignature, objectClassName);

		if (disassembledMethods.contains(methodID))
		{
			//TODO: handle nbt recursion
			if (DEBUG_MESSAGES) { NBTac.LOGGER.warn("Already disassembled! - " + methodID); }
			return;
		}

		if (depth >= MAX_DISASSEMBLY_DEPTH)
		{
			if (DEBUG_MESSAGES) { NBTac.LOGGER.warn("Too deep! - " + methodID); }
			return;
		}

		Method method = findMethod(findClass(className), methodName, methodSignature, true, mustBePublic);

		if (method == null)
		{
			if (methodName == null) { methodName = "with signature "; }
			throw new Exception("Unable to find superclass of " + className + " containing method " + methodName + methodSignature);
		}

		disassembledMethods.add(methodID);
		readMethod(method, arg, objectClassName, depth, uncertain);
		disassembledMethods.remove(methodID);
	}

	public static JavaClass findClass(String className) throws Exception
	{
		JavaClass existingJavaClass = classMap.get(className);

		if (existingJavaClass == null)
		{
			String classPath = className.replace('.', '/') + ".class";
			InputStream classStream = Class.forName(className).getClassLoader().getResourceAsStream(classPath);
			ClassParser classParser = new ClassParser(classStream, classPath);
			JavaClass javaClass = classParser.parse();

			classMap.put(className, javaClass);
			return javaClass;
		}
		else
		{
			return existingJavaClass;
		}
	}

	private static Method findMethod(JavaClass javaClass, String methodName, String methodSignature,
									 boolean searchInSuperclasses, boolean mustBePublic) throws Exception
	{
		if (javaClass == null) { return null; }

		for (Method method : javaClass.getMethods())
		{
			if (mustBePublic && !method.isPublic()) { continue; }
			if ((methodName == null || method.getName().equals(methodName)) && method.getSignature().equals(methodSignature))
			{
				return method;
			}
		}

		if (searchInSuperclasses)
		{
			try
			{
				String superclassName = javaClass.getSuperclassName();
				if (superclassName.equals("java.lang.Object")) { return null; }

				JavaClass superclass = classMap.get(superclassName);
				if (superclass == null)
				{
					superclass = javaClass.getSuperClass();
					classMap.put(superclassName, superclass);
				}

				Method superclassMethod = findMethod(superclass, methodName,
						methodSignature, searchInSuperclasses, mustBePublic);
				if (superclassMethod != null) { return superclassMethod; }
			}
			catch (Exception ignore) {}

			for (String classInterfaceName : javaClass.getInterfaceNames())
			{
				JavaClass classInterface = classMap.get(classInterfaceName);
				if (classInterface == null)
				{
					classInterface = javaClass.getRepository().loadClass(classInterfaceName);
					classMap.put(classInterfaceName, classInterface);
				}

				Method interfaceMethod = findMethod(classInterface, methodName,
						methodSignature, searchInSuperclasses, mustBePublic);
				if (interfaceMethod != null) { return interfaceMethod; }
			}
		}

		return null;
	}

	private static void readMethod(Method method, NbtSuggestions arg, String objectClassName, int depth, boolean uncertain) throws Exception
	{
		/*
			References:
				https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
				https://docs.oracle.com/javase/specs/jvms/se12/html/jvms-6.html
		 */

		Code code = method.getCode();

		LocalVariableTable localVarTable = code.getLocalVariableTable();
		DisassembledValue[] localVars = new DisassembledValue[localVarTable.getLength()];
		ConstantPool constPool = code.getConstantPool();
		CodeStack<DisassembledValue> stack = new CodeStack<>();

		ByteSequence bytes = new ByteSequence(code.getCode());

		for (int i = 0; i < localVarTable.getTableLength(); i++)
		{
			LocalVariable argument = localVarTable.getLocalVariable(i, 0);
			if (argument == null) { continue; }

			if (argument.getName().equals("this"))
			{
				if (i == 0) { localVars[i] = new DisassembledValue(DisassembledValue.Type.THIS, null); }
				else { throw new Exception("[NBT_AC] index != 0 for \"this\"!"); }
			}
			else if (argument.getSignature().equals(COMPOUND_TAG_ARG_SIGNATURE))
			{
				localVars[i] = new DisassembledValue(DisassembledValue.Type.COMPOUND, arg);
			}
			else if (argument.getSignature().equals(LIST_TAG_ARG_SIGNATURE))
			{
				localVars[i] = new DisassembledValue(DisassembledValue.Type.LIST_TAG, arg);
			}
		}

		while (bytes.available() != 0)
		{
			int opcode = bytes.readUnsignedByte();

			switch (opcode)
			{
				case Const.AALOAD:
				case Const.BALOAD:
				case Const.CALOAD:
				case Const.DALOAD:
				case Const.FALOAD:
				case Const.IALOAD:
				case Const.LALOAD:
				case Const.SALOAD:
					stack.poppush(2, 1);
					break;

				case Const.AASTORE:
				case Const.BASTORE:
				case Const.CASTORE:
				case Const.DASTORE:
				case Const.FASTORE:
				case Const.IASTORE:
				case Const.LASTORE:
				case Const.SASTORE:
					stack.popx(3);
					break;

				case Const.ACONST_NULL:
					stack.push(null);
					break;

				case Const.ALOAD:
				case Const.DLOAD:
				case Const.FLOAD:
				case Const.ILOAD:
				case Const.LLOAD:
					stack.push(localVars[bytes.readUnsignedByte()]);
					break;

				case Const.ALOAD_0:
				case Const.ALOAD_1:
				case Const.ALOAD_2:
				case Const.ALOAD_3:
				case Const.DLOAD_0:
				case Const.DLOAD_1:
				case Const.DLOAD_2:
				case Const.DLOAD_3:
				case Const.FLOAD_0:
				case Const.FLOAD_1:
				case Const.FLOAD_2:
				case Const.FLOAD_3:
				case Const.ILOAD_0:
				case Const.ILOAD_1:
				case Const.ILOAD_2:
				case Const.ILOAD_3:
				case Const.LLOAD_0:
				case Const.LLOAD_1:
				case Const.LLOAD_2:
				case Const.LLOAD_3:
					int loadPos = (opcode - 2) % 4;
					stack.push(localVars[loadPos]);
					break;

				case Const.ASTORE:
				case Const.DSTORE:
				case Const.FSTORE:
				case Const.ISTORE:
				case Const.LSTORE:
					localVars[bytes.readUnsignedByte()] = stack.pop();
					break;

				case Const.ASTORE_0:
				case Const.ASTORE_1:
				case Const.ASTORE_2:
				case Const.ASTORE_3:
				case Const.DSTORE_0:
				case Const.DSTORE_1:
				case Const.DSTORE_2:
				case Const.DSTORE_3:
				case Const.FSTORE_0:
				case Const.FSTORE_1:
				case Const.FSTORE_2:
				case Const.FSTORE_3:
				case Const.ISTORE_0:
				case Const.ISTORE_1:
				case Const.ISTORE_2:
				case Const.ISTORE_3:
				case Const.LSTORE_0:
				case Const.LSTORE_1:
				case Const.LSTORE_2:
				case Const.LSTORE_3:
					int storePos = (opcode - 3) % 4;
					localVars[storePos] = stack.pop();
					break;

				case Const.RETURN:
				case Const.ARETURN:
				case Const.DRETURN:
				case Const.FRETURN:
				case Const.IRETURN:
				case Const.LRETURN:
					break;

				case Const.ATHROW:
					break;

				case Const.ANEWARRAY:
					stack.poppush(1, 1);
					bytes.skip(2);
					break;

				case Const.MULTIANEWARRAY:
					bytes.skip(2);
					stack.poppush(bytes.readUnsignedByte(), 1);
					break;

				case Const.ARRAYLENGTH:
					stack.poppush(1, 1);
					break;

				case Const.BIPUSH:
					stack.push(new DisassembledValue(DisassembledValue.Type.INTEGER, (int)bytes.readByte()));
					break;

				case Const.BREAKPOINT:
				case Const.IMPDEP1:
				case Const.IMPDEP2:
					break;

				case Const.CHECKCAST:
					bytes.skip(2);
					break;

				case Const.D2F:
				case Const.D2I:
				case Const.D2L:
				case Const.F2D:
				case Const.F2I:
				case Const.F2L:
				case Const.I2B:
				case Const.I2C:
				case Const.I2D:
				case Const.I2F:
				case Const.I2L:
				case Const.I2S:
				case Const.L2D:
				case Const.L2F:
				case Const.L2I:
					stack.poppush(1, 1);
					break;

				case Const.DCONST_0:
				case Const.DCONST_1:
				case Const.FCONST_0:
				case Const.FCONST_1:
				case Const.FCONST_2:
				case Const.LCONST_0:
				case Const.LCONST_1:
					stack.push(null);
					break;

				case Const.ICONST_M1:
				case Const.ICONST_0:
				case Const.ICONST_1:
				case Const.ICONST_2:
				case Const.ICONST_3:
				case Const.ICONST_4:
				case Const.ICONST_5:
					int iconst_val = 0;
					if (opcode == Const.ICONST_M1) { iconst_val = -1; }
					if (opcode == Const.ICONST_0) { iconst_val = 0; }
					if (opcode == Const.ICONST_1) { iconst_val = 1; }
					if (opcode == Const.ICONST_2) { iconst_val = 2; }
					if (opcode == Const.ICONST_3) { iconst_val = 3; }
					if (opcode == Const.ICONST_4) { iconst_val = 4; }
					if (opcode == Const.ICONST_5) { iconst_val = 5; }

					stack.push(new DisassembledValue(DisassembledValue.Type.INTEGER, iconst_val));
					break;

				case Const.DADD:
				case Const.DCMPG:
				case Const.DCMPL:
				case Const.DDIV:
				case Const.DMUL:
				case Const.DREM:
				case Const.DSUB:
				case Const.FADD:
				case Const.FCMPG:
				case Const.FCMPL:
				case Const.FDIV:
				case Const.FMUL:
				case Const.FREM:
				case Const.FSUB:
				case Const.IADD:
				case Const.IAND:
				case Const.IDIV:
				case Const.IMUL:
				case Const.IOR:
				case Const.IREM:
				case Const.ISHL:
				case Const.ISHR:
				case Const.ISUB:
				case Const.IUSHR:
				case Const.IXOR:
				case Const.LADD:
				case Const.LAND:
				case Const.LCMP:
				case Const.LDIV:
				case Const.LMUL:
				case Const.LOR:
				case Const.LREM:
				case Const.LSHL:
				case Const.LSHR:
				case Const.LSUB:
				case Const.LUSHR:
				case Const.LXOR:
					stack.poppush(2, 1);
					break;

				case Const.DNEG:
				case Const.FNEG:
				case Const.INEG:
				case Const.LNEG:
					stack.poppush(1, 1);
					break;

				case Const.DUP:
					stack.push(stack.peek());
					break;

				case Const.DUP_X1:
					DisassembledValue dupx1_val1 = stack.pop();
					DisassembledValue dupx1_val2 = stack.pop();
					stack.push(dupx1_val1);
					stack.push(dupx1_val2);
					stack.push(dupx1_val1);
					break;

				case Const.DUP_X2:
					DisassembledValue dupx2_val1 = stack.pop();
					DisassembledValue dupx2_val2 = stack.pop();
					DisassembledValue dupx2_val3 = stack.pop();
					stack.push(dupx2_val1);
					stack.push(dupx2_val3);
					stack.push(dupx2_val2);
					stack.push(dupx2_val1);
					break;

				case Const.DUP2:
					DisassembledValue dup2_val1 = stack.elementAt(stack.size() - 1);
					DisassembledValue dup2_val2 = stack.elementAt(stack.size() - 2);
					stack.push(dup2_val2);
					stack.push(dup2_val1);
					break;

				case Const.DUP2_X1:
					DisassembledValue dup2x1_val1 = stack.pop();
					DisassembledValue dup2x1_val2 = stack.pop();
					DisassembledValue dup2x1_val3 = stack.pop();
					stack.push(dup2x1_val2);
					stack.push(dup2x1_val1);
					stack.push(dup2x1_val3);
					stack.push(dup2x1_val2);
					stack.push(dup2x1_val1);
					break;

				case Const.DUP2_X2:
					DisassembledValue dup2x2_val1 = stack.pop();
					DisassembledValue dup2x2_val2 = stack.pop();
					DisassembledValue dup2x2_val3 = stack.pop();
					DisassembledValue dup2x2_val4 = stack.pop();
					stack.push(dup2x2_val2);
					stack.push(dup2x2_val1);
					stack.push(dup2x2_val4);
					stack.push(dup2x2_val3);
					stack.push(dup2x2_val2);
					stack.push(dup2x2_val1);
					break;

				case Const.SWAP:
					DisassembledValue swap_val1 = stack.pop();
					DisassembledValue swap_val2 = stack.pop();
					stack.push(swap_val1);
					stack.push(swap_val2);
					break;

				case Const.SIPUSH:
					stack.push(new DisassembledValue(DisassembledValue.Type.INTEGER, (int)bytes.readShort()));
					break;

				case Const.MONITORENTER:
				case Const.MONITOREXIT:
					stack.pop();
					break;

				case Const.NEW:
					stack.push(null);
					bytes.skip(2);
					break;

				case Const.NEWARRAY:
					stack.poppush(1, 1);
					bytes.skip(1);
					break;

				case Const.POP:
					stack.pop();
					break;

				case Const.POP2:
					stack.popx(2);
					break;

				case Const.IINC:
					bytes.skip(2);
					break;

				case Const.GETFIELD:
					stack.poppush(1, 1);
					bytes.skip(2);
					break;

				case Const.GETSTATIC:
					stack.push(null);
					bytes.skip(2);
					break;

				case Const.GOTO:
					bytes.skip(2);
					break;

				case Const.GOTO_W:
					bytes.skip(4);
					break;

				case Const.IF_ACMPEQ:
				case Const.IF_ACMPNE:
				case Const.IF_ICMPEQ:
				case Const.IF_ICMPGE:
				case Const.IF_ICMPGT:
				case Const.IF_ICMPLE:
				case Const.IF_ICMPLT:
				case Const.IF_ICMPNE:
					stack.popx(2);
					bytes.skip(2);
					break;

				case Const.IFEQ:
				case Const.IFGE:
				case Const.IFGT:
				case Const.IFLE:
				case Const.IFLT:
				case Const.IFNE:
				case Const.IFNONNULL:
				case Const.IFNULL:
					stack.pop();
					bytes.skip(2);
					break;

				case Const.JSR:
					stack.push(null);
					bytes.skip(2);
					break;

				case Const.JSR_W:
					stack.push(null);
					bytes.skip(4);
					break;

				case Const.RET:
					bytes.skip(1);
					break;

				case Const.WIDE:
					int wide_opcode = bytes.read();

					switch (wide_opcode)
					{
						case Const.ALOAD:
						case Const.DLOAD:
						case Const.FLOAD:
						case Const.ILOAD:
						case Const.LLOAD:
							stack.push(localVars[bytes.readUnsignedShort()]);
							break;

						case Const.ASTORE:
						case Const.DSTORE:
						case Const.FSTORE:
						case Const.ISTORE:
						case Const.LSTORE:
							localVars[bytes.readUnsignedShort()] = stack.pop();
							break;

						case Const.RET:
							bytes.skip(2);
							break;

						case Const.IINC:
							bytes.skip(4);
							break;

						default:
							throw new Exception(String.format("[NBT_AC] Undefined opcode for wide operation (%d)!",wide_opcode));
					}

					break;

				case Const.LOOKUPSWITCH:
					int lookupswitch_indexMod4 = bytes.getIndex() % 4;
					bytes.skip(lookupswitch_indexMod4 == 0 ? 0 : 4 - lookupswitch_indexMod4);
					bytes.skip(4);
					long lookupswitch_npairs = bytes.readInt();
					bytes.skip(lookupswitch_npairs * 8);
					break;

				case Const.TABLESWITCH:
					int tableswitch_indexMod4 = bytes.getIndex() % 4;
					bytes.skip(tableswitch_indexMod4 == 0 ? 0 : 4 - tableswitch_indexMod4);
					bytes.skip(4);
					long tableswitch_low = bytes.readInt();
					long tableswitch_high = bytes.readInt();
					bytes.skip((tableswitch_high - tableswitch_low + 1) * 4);
					break;

				case Const.LDC:
					int ldc_index = bytes.readUnsignedByte();
					Constant ldc_const = constPool.getConstant(ldc_index);
					if (ldc_const.getTag() == Const.CONSTANT_String)
					{
						stack.push(new DisassembledValue(DisassembledValue.Type.STRING,
								((ConstantString)ldc_const).getBytes(constPool)));
					}
					else
					{
						stack.push(null);
					}
					break;

				case Const.LDC_W:
					int ldcw_index = bytes.readUnsignedShort();
					Constant ldcw_const = constPool.getConstant(ldcw_index);
					if (ldcw_const.getTag() == Const.CONSTANT_String)
					{
						stack.push(new DisassembledValue(DisassembledValue.Type.STRING, ((ConstantString)ldcw_const).getBytes(constPool)));
					}
					else
					{
						stack.push(null);
					}
					break;

				case Const.LDC2_W:
					stack.push(null);
					bytes.skip(2);
					break;

				case Const.PUTFIELD:
					stack.popx(2);
					bytes.skip(2);
					break;

				case Const.PUTSTATIC:
					stack.pop();
					bytes.skip(2);
					break;

				case Const.INVOKEDYNAMIC:
				case Const.INVOKEINTERFACE:
				case Const.INVOKESPECIAL:
				case Const.INVOKESTATIC:
				case Const.INVOKEVIRTUAL:
					handleInvoke(opcode, bytes, constPool, stack, objectClassName, depth, uncertain);
					break;

				case Const.NOP:
				case Const.INSTANCEOF:
					break;

			}
		}
	}

	private static void handleInvoke(int opcode, ByteSequence bytes, ConstantPool constPool,
									 CodeStack<DisassembledValue> stack, String objectClassName,
									 int depth, boolean uncertain) throws Exception
	{
		boolean valueAlreadyReturned = false;
		int index = bytes.readUnsignedShort();

		ConstantCP constant = constPool.getConstant(index);
		ConstantNameAndType nameAndType = constPool.getConstant(constant.getNameAndTypeIndex(), Const.CONSTANT_NameAndType);

		MethodSignature signature = new MethodSignature(nameAndType.getSignature(constPool));

		/*
			INVOKEVIRTUAL - If method belongs to CompoundTag or ListTag and is known, then add appropriate suggestion.
			                If object on which the method was called is "this", then disassemble the method.
			                If object on which the method was called is unknown, then disassemble the method of
			                   type class and mark suggestions as "uncertain".
			INVOKESPECIAL/INVOKESTATIC - Disassemble method.
			INVOKEDYNAMIC/INVOKEINTERFACE - Ignore.
		 */

		switch (opcode)
		{
			case Const.INVOKEVIRTUAL:
				valueAlreadyReturned =
						handleInvokeVirtual(constPool, stack, objectClassName, depth, constant, signature, nameAndType, uncertain);
				break;

			case Const.INVOKESPECIAL:
			case Const.INVOKESTATIC:
				handleInvokeStatic(opcode, constPool, stack, objectClassName, depth, constant, signature, nameAndType, uncertain);
				break;

			case Const.INVOKEDYNAMIC:
			case Const.INVOKEINTERFACE:
				handleInvokeDynamic(opcode, bytes, stack, signature);
				break;

			default:
				throw new Exception("[NBT_AC] Unexpected invoke opcode!");
		}

		if (signature.returnsValue() && !valueAlreadyReturned) { stack.push(null); }
	}

	private static boolean handleInvokeVirtual(ConstantPool constPool, CodeStack<DisassembledValue> stack, String objectClassName,
											   int depth, ConstantCP constant, MethodSignature signature,
											   ConstantNameAndType nameAndType, boolean uncertain) throws Exception
	{
		ConstantClass constClass = constPool.getConstant(constant.getClassIndex(), Const.CONSTANT_Class);
		String classSignature = ((String)constClass.getConstantValue(constPool)).replace('/', '.');

		if (classSignature.equals(COMPOUND_TAG_SIGNATURE))
		{
			return handleCompoundTagMethodInvoke(constPool, stack, signature, nameAndType, uncertain);
		}
		else if (classSignature.equals(LIST_TAG_SIGNATURE))
		{
			return handleListTagMethodInvoke(stack, signature);
		}
		else
		{
			return handleUnknownMethodInvoke(classSignature, constPool, stack, objectClassName, depth, signature, nameAndType, uncertain);
		}
	}

	private static boolean handleCompoundTagMethodInvoke(ConstantPool constPool, CodeStack<DisassembledValue> stack,
														 MethodSignature signature, ConstantNameAndType nameAndType, boolean uncertain)
	{
		String name = nameAndType.getName(constPool);
		NbtSuggestion.Type type = NbtSuggestion.Type.fromMethodName(name);
		NbtSuggestion.Type listType = NbtSuggestion.Type.UNKNOWN;

		if (type == NbtSuggestion.Type.LIST && signature.argumentCount() == 2)
		{
			DisassembledValue listTypeArg = stack.pop();
			if (listTypeArg != null && listTypeArg.type == DisassembledValue.Type.INTEGER)
			{
				listType = NbtSuggestion.Type.fromID(((Integer)listTypeArg.object).byteValue());
			}
		}
		else if (signature.argumentCount() != 1)
		{
			stack.popx(signature.argumentCount() + 1);
			return false;
		}

		DisassembledValue arg = stack.pop();
		DisassembledValue compound = stack.pop();

		if (type == NbtSuggestion.Type.NOT_FOUND || arg == null || arg.type != DisassembledValue.Type.STRING ||
				compound == null || compound.type != DisassembledValue.Type.COMPOUND)
		{
			return false;
		}

		NbtSuggestions suggestions = (NbtSuggestions)compound.object;
		String tagName = (String)arg.object;

		if (ModConfig.hideForgeTags.getValue() && (tagName.equals("ForgeCaps") || tagName.equals("ForgeData")))
		{
			return false;
		}

		NbtSuggestion newSuggestion;
		if (uncertain) { newSuggestion = new NbtSuggestion(tagName, type, NbtSuggestion.SuggestionType.UNCERTAIN); }
		else { newSuggestion = new NbtSuggestion(tagName, type); }

		suggestions.addSuggestion(newSuggestion);

		if (type == NbtSuggestion.Type.COMPOUND)
		{
			stack.push(new DisassembledValue(DisassembledValue.Type.COMPOUND, newSuggestion.addSubcompound()));
			return true; // valueAlreadyReturned = true
		}
		else if (type == NbtSuggestion.Type.LIST)
		{
			newSuggestion.listType = listType;

			if (listType == NbtSuggestion.Type.COMPOUND)
			{
				stack.push(new DisassembledValue(DisassembledValue.Type.LIST_TAG, newSuggestion.addSubcompound()));
				return true; // valueAlreadyReturned = true
			}
		}

		return false;
	}

	private static boolean handleListTagMethodInvoke(CodeStack<DisassembledValue> stack, MethodSignature signature)
	{
		if (!signature.signature.equals("(I)L" + CompoundTag.class.getName().replace('.', '/') + ";"))
		{
			stack.popx(signature.argumentCount() + 1);
			return false;
		}

		stack.pop(); // index of element - ignore
		DisassembledValue listTag = stack.pop();

		if (listTag == null || listTag.type != DisassembledValue.Type.LIST_TAG) { return false; }

		stack.push(new DisassembledValue(DisassembledValue.Type.COMPOUND, listTag.object));
		return true;
	}

	private static boolean handleUnknownMethodInvoke(String classSignature, ConstantPool constPool, CodeStack<DisassembledValue> stack,
													 String objectClassName, int depth, MethodSignature signature,
													 ConstantNameAndType nameAndType, boolean uncertain) throws Exception
	{
		DisassembledValue object = stack.get(stack.size() - signature.argumentCount() - 1);
		NbtSuggestions suggestions = getCompoundTagArgument(stack, signature);

		if (suggestions != null)
		{
			if (object != null && object.type == DisassembledValue.Type.THIS && objectClassName != null)
			{
				disassembly(objectClassName, nameAndType.getName(constPool),
						nameAndType.getSignature(constPool), objectClassName, suggestions, depth + 1, false, uncertain);
			}
			else
			{
				disassembly(classSignature, nameAndType.getName(constPool),
						nameAndType.getSignature(constPool), null, suggestions, depth + 1, false, true);
			}
		}

		stack.pop();
		return false;
	}

	private static void handleInvokeStatic(int opcode, ConstantPool constPool, CodeStack<DisassembledValue> stack,
										   String objectClassName, int depth, ConstantCP constant, MethodSignature signature,
										   ConstantNameAndType nameAndType, boolean uncertain) throws Exception
	{
		NbtSuggestions suggestions = getCompoundTagArgument(stack, signature);

		String newObjectClassName = null;
		if (opcode == Const.INVOKESPECIAL)
		{
			DisassembledValue value = stack.pop();
			if (value != null && value.type == DisassembledValue.Type.THIS) { newObjectClassName = objectClassName; }
		}

		if (suggestions != null)
		{
			ConstantClass constClass = constPool.getConstant(constant.getClassIndex(), Const.CONSTANT_Class);
			String className = ((String)constClass.getConstantValue(constPool)).replace('/', '.');

			disassembly(className, nameAndType.getName(constPool),
					nameAndType.getSignature(constPool), newObjectClassName, suggestions, depth + 1, false, uncertain);
		}
	}


	private static void handleInvokeDynamic(int opcode, ByteSequence bytes, CodeStack<DisassembledValue> stack,
											MethodSignature signature) throws Exception
	{
		stack.popx(signature.argumentCount());
		if (opcode == Const.INVOKEINTERFACE) { stack.pop(); }
		bytes.skip(2);
	}

	private static NbtSuggestions getCompoundTagArgument(CodeStack<DisassembledValue> stack, MethodSignature signature)
	{
		NbtSuggestions suggestions = null;

		for (int i = 0; i < signature.argumentCount(); i++)
		{
			DisassembledValue stackValue = stack.pop();
			if (stackValue == null) { continue; }
			if (stackValue.type == DisassembledValue.Type.COMPOUND || stackValue.type == DisassembledValue.Type.LIST_TAG)
			{
				suggestions = (NbtSuggestions)stackValue.object;
			}
		}

		return suggestions;
	}
}