package jadx.core.dex.visitors;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EnumMapAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.NewArrayNode;
import jadx.core.dex.instructions.SwitchNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.DexNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.InstructionRemover;
import jadx.core.utils.exceptions.JadxException;

@JadxVisitor(
		name = "ReSugarCode",
		desc = "Simplify synthetic or verbose code",
		runAfter = CodeShrinker.class
)
public class ReSugarCode extends AbstractVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ReSugarCode.class);

	@Override
	public boolean visit(ClassNode cls) throws JadxException {
		initClsEnumMap(cls);
		return true;
	}

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		InstructionRemover remover = new InstructionRemover(mth);
		for (BlockNode block : mth.getBasicBlocks()) {
			remover.setBlock(block);
			List<InsnNode> instructions = block.getInstructions();
			int size = instructions.size();
			for (int i = 0; i < size; i++) {
				InsnNode replacedInsn = process(mth, instructions, i, remover);
				if (replacedInsn != null) {
					instructions.set(i, replacedInsn);
				}
			}
			remover.perform();
		}
	}

	private static InsnNode process(MethodNode mth, List<InsnNode> instructions, int i, InstructionRemover remover) {
		InsnNode insn = instructions.get(i);
		switch (insn.getType()) {
			case NEW_ARRAY:
				return processNewArray(mth, instructions, i, remover);

			case SWITCH:
				processEnumSwitch(mth, (SwitchNode) insn);
				return null;

			default:
				return null;
		}
	}

	/**
	 * Replace new array and sequence of array-put to new filled-array instruction.
	 */
	private static InsnNode processNewArray(MethodNode mth, List<InsnNode> instructions, int i,
	                                        InstructionRemover remover) {
		NewArrayNode newArrayInsn = (NewArrayNode) instructions.get(i);
		InsnArg arg = newArrayInsn.getArg(0);
		if (!arg.isLiteral()) {
			return null;
		}
		int len = (int) ((LiteralArg) arg).getLiteral();
		int size = instructions.size();
		if (len <= 0
				|| i + len >= size
				|| instructions.get(i + len).getType() != InsnType.APUT) {
			return null;
		}
		ArgType arrType = newArrayInsn.getArrayType();
		InsnNode filledArr = new FilledNewArrayNode(arrType.getArrayElement(), len);
		filledArr.setResult(newArrayInsn.getResult());
		for (int j = 0; j < len; j++) {
			InsnNode put = instructions.get(i + 1 + j);
			if (put.getType() != InsnType.APUT) {
				LOG.debug("Not a APUT in expected new filled array: {}, method: {}", put, mth);
				return null;
			}
			filledArr.addArg(put.getArg(2));
			remover.add(put);
		}
		return filledArr;
	}

	private static void processEnumSwitch(MethodNode mth, SwitchNode insn) {
		InsnArg arg = insn.getArg(0);
		if (!arg.isInsnWrap()) {
			return;
		}
		InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
		if (wrapInsn.getType() != InsnType.AGET) {
			return;
		}
		EnumMapInfo enumMapInfo = checkEnumMapAccess(mth.dex(), wrapInsn);
		if (enumMapInfo == null) {
			return;
		}
		FieldNode enumMapField = enumMapInfo.getMapField();
		InsnArg invArg = enumMapInfo.getArg();

		EnumMapAttr.KeyValueMap valueMap = getEnumMap(mth, enumMapField);
		if (valueMap == null) {
			return;
		}
		Object[] keys = insn.getKeys();
		for (Object key : keys) {
			Object newKey = valueMap.get(key);
			if (newKey == null) {
				return;
			}
		}
		// replace confirmed
		if (!insn.replaceArg(arg, invArg)) {
			return;
		}
		for (int i = 0; i < keys.length; i++) {
			keys[i] = valueMap.get(keys[i]);
		}
		enumMapField.add(AFlag.DONT_GENERATE);
		checkAndHideClass(enumMapField.getParentClass());
	}

	private static void initClsEnumMap(ClassNode enumCls) {
		MethodNode clsInitMth = enumCls.getClassInitMth();
		if (clsInitMth == null || clsInitMth.isNoCode()) {
			return;
		}
		EnumMapAttr mapAttr = new EnumMapAttr();
		for (BlockNode block : clsInitMth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.APUT) {
					addToEnumMap(enumCls.dex(), mapAttr, insn);
				}
			}
		}
		if (!mapAttr.isEmpty()) {
			enumCls.addAttr(mapAttr);
		}
	}

	@Nullable
	private static EnumMapAttr.KeyValueMap getEnumMap(MethodNode mth, FieldNode field) {
		ClassNode syntheticClass = field.getParentClass();
		EnumMapAttr mapAttr = syntheticClass.get(AType.ENUM_MAP);
		if (mapAttr == null) {
			return null;
		}
		return mapAttr.getMap(field);
	}

	private static void addToEnumMap(DexNode dex, EnumMapAttr mapAttr, InsnNode aputInsn) {
		InsnArg litArg = aputInsn.getArg(2);
		if (!litArg.isLiteral()) {
			return;
		}
		EnumMapInfo mapInfo = checkEnumMapAccess(dex, aputInsn);
		if (mapInfo == null) {
			return;
		}
		InsnArg enumArg = mapInfo.getArg();
		FieldNode field = mapInfo.getMapField();
		if (field == null || !enumArg.isInsnWrap()) {
			return;
		}
		InsnNode sget = ((InsnWrapArg) enumArg).getWrapInsn();
		if (!(sget instanceof IndexInsnNode)) {
			return;
		}
		Object index = ((IndexInsnNode) sget).getIndex();
		if (!(index instanceof FieldInfo)) {
			return;
		}
		FieldNode fieldNode = dex.resolveField((FieldInfo) index);
		if (fieldNode == null) {
			return;
		}
		int literal = (int) ((LiteralArg) litArg).getLiteral();
		mapAttr.add(field, literal, fieldNode);
	}

	public static EnumMapInfo checkEnumMapAccess(DexNode dex, InsnNode checkInsn) {
		InsnArg sgetArg = checkInsn.getArg(0);
		InsnArg invArg = checkInsn.getArg(1);
		if (!sgetArg.isInsnWrap() || !invArg.isInsnWrap()) {
			return null;
		}
		InsnNode invInsn = ((InsnWrapArg) invArg).getWrapInsn();
		InsnNode sgetInsn = ((InsnWrapArg) sgetArg).getWrapInsn();
		if (invInsn.getType() != InsnType.INVOKE || sgetInsn.getType() != InsnType.SGET) {
			return null;
		}
		InvokeNode inv = (InvokeNode) invInsn;
		if (!inv.getCallMth().getShortId().equals("ordinal()I")) {
			return null;
		}
		ClassNode enumCls = dex.resolveClass(inv.getCallMth().getDeclClass());
		if (enumCls == null || !enumCls.isEnum()) {
			return null;
		}
		Object index = ((IndexInsnNode) sgetInsn).getIndex();
		if (!(index instanceof FieldInfo)) {
			return null;
		}
		FieldNode enumMapField = dex.resolveField((FieldInfo) index);
		if (enumMapField == null || !enumMapField.getAccessFlags().isSynthetic()) {
			return null;
		}
		return new EnumMapInfo(inv.getArg(0), enumMapField);
	}

	/**
	 * If all static final synthetic fields have DONT_GENERATE => hide whole class
	 */
	private static void checkAndHideClass(ClassNode cls) {
		for (FieldNode field : cls.getFields()) {
			AccessInfo af = field.getAccessFlags();
			if (af.isSynthetic() && af.isStatic() && af.isFinal()
					&& !field.contains(AFlag.DONT_GENERATE)) {
				return;
			}
		}
		cls.add(AFlag.DONT_GENERATE);
	}

	private static class EnumMapInfo {
		private final InsnArg arg;
		private final FieldNode mapField;

		public EnumMapInfo(InsnArg arg, FieldNode mapField) {
			this.arg = arg;
			this.mapField = mapField;
		}

		public InsnArg getArg() {
			return arg;
		}

		public FieldNode getMapField() {
			return mapField;
		}
	}
}
