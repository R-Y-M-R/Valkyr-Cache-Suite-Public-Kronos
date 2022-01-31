import java.lang.reflect.Field;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import store.CacheLibrary;
import store.io.impl.InputStream;
import store.io.impl.OutputStream;
import store.plugin.PluginManager;
import store.plugin.PluginType;
import suite.annotation.MeshIdentifier;
import suite.annotation.OrderType;
import store.plugin.extension.ConfigExtensionBase;
import store.utilities.ReflectionUtils;

/**
 * @author ReverendDread Sep 17, 2019
 */
public class ItemConfig extends ConfigExtensionBase {

	@Override
	public void decode(int opcode, InputStream buffer) {
		if (CacheLibrary.get().is317()) {
			read317(opcode, buffer);
			return;
		}

		if (opcode == 1) {
			inventoryModel = buffer.readUnsignedShort();
		} else if (opcode == 2) {
			name = buffer.readString();
		} else if (opcode == 4) {
			zoom2d = buffer.readUnsignedShort();
		} else if (opcode == 5) {
			xan2d = buffer.readUnsignedShort();
		} else if (opcode == 6) {
			yan2d = buffer.readUnsignedShort();
		} else if (opcode == 7) {
			xOffset2d = buffer.readUnsignedShort();
			if (xOffset2d > 32767) {
				xOffset2d -= 65536;
			}
		} else if (opcode == 8) {
			yOffset2d = buffer.readUnsignedShort();
			if (yOffset2d > 32767) {
				yOffset2d -= 65536;
			}
		} else if (opcode == 9) {
			buffer.readString();
		} else if (opcode == 11) {
			stackable = 1;
		} else if (opcode == 12) {
			cost = buffer.readInt();
		} else if (opcode == 16) {
			members = true;
		} else if (opcode == 23) {
			maleModel0 = buffer.readUnsignedShort();
			maleOffset = buffer.readUnsignedByte();
		} else if (opcode == 24) {
			maleModel1 = buffer.readUnsignedShort();
		} else if (opcode == 25) {
			femaleModel0 = buffer.readUnsignedShort();
			femaleOffset = buffer.readUnsignedByte();
		} else if (opcode == 26) {
			femaleModel1 = buffer.readUnsignedShort();
		} else if (opcode >= 30 && opcode < 35) {
			options[opcode - 30] = buffer.readString();
			if (options[opcode - 30].equalsIgnoreCase("Hidden")) {
				options[opcode - 30] = null;
			}
		} else if (opcode >= 35 && opcode < 40) {
			interfaceOptions[opcode - 35] = buffer.readString();
		} else if (opcode == 40) {
			int var5 = buffer.readUnsignedByte();
			replaceColors = new int[var5];
			originalColors = new int[var5];
			for (int var4 = 0; var4 < var5; ++var4) {
				replaceColors[var4] = buffer.readUnsignedShort();
				originalColors[var4] = buffer.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int var5 = buffer.readUnsignedByte();
			textureFind = new int[var5];
			textureReplace = new int[var5];
			for (int var4 = 0; var4 < var5; ++var4) {
				textureFind[var4] = buffer.readUnsignedShort();
				textureReplace[var4] = buffer.readUnsignedShort();
			}
		} else if (opcode == 42) {
			shiftClickDropIndex = buffer.readByte();
		} else if (opcode == 65) {
			isTradeable = true;
		} else if (opcode == 78) {
			maleModel2 = buffer.readUnsignedShort();
		} else if (opcode == 79) {
			femaleModel2 = buffer.readUnsignedShort();
		} else if (opcode == 90) {
			maleHeadModel = buffer.readUnsignedShort();
		} else if (opcode == 91) {
			femaleHeadModel = buffer.readUnsignedShort();
		} else if (opcode == 92) {
			maleHeadModel2 = buffer.readUnsignedShort();
		} else if (opcode == 93) {
			femaleHeadModel2 = buffer.readUnsignedShort();
		} else if (opcode == 94) {
			buffer.readUnsignedShort();
		} else if (opcode == 95) {
			zan2d = buffer.readUnsignedShort();
		} else if (opcode == 97) {
			notedID = buffer.readUnsignedShort();
		} else if (opcode == 98) {
			notedTemplate = buffer.readUnsignedShort();
		} else if (opcode >= 100 && opcode < 110) {
			if (countObj == null) {
				countObj = new int[10];
				countCo = new int[10];
			}

			countObj[opcode - 100] = buffer.readUnsignedShort();
			countCo[opcode - 100] = buffer.readUnsignedShort();
		} else if (opcode == 110) {
			resizeX = buffer.readUnsignedShort();
		} else if (opcode == 111) {
			resizeY = buffer.readUnsignedShort();
		} else if (opcode == 112) {
			resizeZ = buffer.readUnsignedShort();
		} else if (opcode == 113) {
			ambient = buffer.readByte();
		} else if (opcode == 114) {
			contrast = buffer.readByte();
		} else if (opcode == 115) {
			team = buffer.readUnsignedByte();
		} else if (opcode == 139) {
			boughtId = buffer.readUnsignedShort();
		} else if (opcode == 140) {
			boughtTemplateId = buffer.readUnsignedShort();
		} else if (opcode == 148) {
			placeholderId = buffer.readUnsignedShort();
		} else if (opcode == 149) {
			placeholderTemplateId = buffer.readUnsignedShort();
		} else if (opcode == 249) {
			int length = buffer.readUnsignedByte();

			params = new HashMap<>(length);

			for (int i = 0; i < length; i++) {
				boolean isString = buffer.readUnsignedByte() == 1;
				int key = buffer.read24BitInt();
				Object value;

				if (isString) {
					value = buffer.readString();
				}

				else {
					value = buffer.readInt();
				}

				params.put(key, value);
			}
		} else {
			System.err.println("item : " + id + ", error decoding opcode : " + opcode + ", previous opcodes: " + Arrays.toString(previousOpcodes));
		}
		ArrayUtils.add(previousOpcodes, opcode);
	}

	private void read317(int opcode, InputStream buffer) {
		if (opcode == 1) {
			inventoryModel = buffer.readUnsignedShort();
		} else if (opcode == 2) {
			name = buffer.readString317();
		} else if (opcode == 3) {
			description = buffer.readString317();
		} else if (opcode == 4) {
			zoom2d = buffer.readUnsignedShort();
		} else if (opcode == 5) {
			xan2d = buffer.readUnsignedShort();
		} else if (opcode == 6) {
			yan2d = buffer.readUnsignedShort();
		} else if (opcode == 7) {
			xOffset2d = buffer.readUnsignedShort();
			if (xOffset2d > 32767) {
				xOffset2d -= 65536;
			}
		} else if (opcode == 8) {
			yOffset2d = buffer.readUnsignedShort();
			if (yOffset2d > 32767) {
				yOffset2d -= 65536;
			}
		} else if (opcode == 11) {
			stackable = 1;
		} else if (opcode == 12) {
			cost = buffer.readInt();
		} else if (opcode == 16) {
			members = true;
		} else if (opcode == 23) {
			maleModel0 = buffer.readUnsignedShort();
			maleOffset = buffer.readUnsignedByte();
		} else if (opcode == 24) {
			maleModel1 = buffer.readUnsignedShort();
		} else if (opcode == 25) {
			femaleModel0 = buffer.readUnsignedShort();
			femaleOffset = buffer.readByte();
		} else if (opcode == 26) {
			femaleModel1 = buffer.readUnsignedShort();
		} else if (opcode >= 30 && opcode < 35) {
			options[opcode - 30] = buffer.readString317();
			if (options[opcode - 30].equalsIgnoreCase("Hidden")) {
				options[opcode - 30] = null;
			}
		} else if (opcode >= 35 && opcode < 40) {
			interfaceOptions[opcode - 35] = buffer.readString317();
		} else if (opcode == 40) {
			int var5 = buffer.readUnsignedByte();
			replaceColors = new int[var5];
			originalColors = new int[var5];
			for (int var4 = 0; var4 < var5; ++var4) {
				replaceColors[var4] = buffer.readUnsignedShort();
				originalColors[var4] = buffer.readUnsignedShort();
			}
		} else if (opcode == 41) {
			int var5 = buffer.readUnsignedByte();
			textureFind = new int[var5];
			textureReplace = new int[var5];
			for (int var4 = 0; var4 < var5; ++var4) {
				textureFind[var4] = buffer.readUnsignedShort();
				textureReplace[var4] = buffer.readUnsignedShort();
			}
		} else if (opcode == 42) {
			shiftClickDropIndex = buffer.readByte();
		} else if (opcode == 65) {
			isTradeable = true;
		} else if (opcode == 78) {
			maleModel2 = buffer.readUnsignedShort();
		} else if (opcode == 79) {
			femaleModel2 = buffer.readUnsignedShort();
		} else if (opcode == 90) {
			maleHeadModel = buffer.readUnsignedShort();
		} else if (opcode == 91) {
			femaleHeadModel = buffer.readUnsignedShort();
		} else if (opcode == 92) {
			maleHeadModel2 = buffer.readUnsignedShort();
		} else if (opcode == 93) {
			femaleHeadModel2 = buffer.readUnsignedShort();
		} else if (opcode == 94) {
			buffer.readUnsignedShort();
		} else if (opcode == 95) {
			zan2d = buffer.readUnsignedShort();
		} else if (opcode == 97) {
			notedID = buffer.readUnsignedShort();
		} else if (opcode == 98) {
			notedTemplate = buffer.readUnsignedShort();
		} else if (opcode >= 100 && opcode < 110) {
			if (countObj == null) {
				countObj = new int[10];
				countCo = new int[10];
			}

			countObj[opcode - 100] = buffer.readUnsignedShort();
			countCo[opcode - 100] = buffer.readUnsignedShort();
		} else if (opcode == 110) {
			resizeX = buffer.readUnsignedShort();
		} else if (opcode == 111) {
			resizeY = buffer.readUnsignedShort();
		} else if (opcode == 112) {
			resizeZ = buffer.readUnsignedShort();
		} else if (opcode == 113) {
			ambient = buffer.readByte();
		} else if (opcode == 114) {
			contrast = buffer.readByte();
		} else if (opcode == 115) {
			team = buffer.readUnsignedByte();
		} else if (opcode == 139) {
			boughtId = buffer.readUnsignedShort();
		} else if (opcode == 140) {
			boughtTemplateId = buffer.readUnsignedShort();
		} else if (opcode == 148) {
			placeholderId = buffer.readUnsignedShort();
		} else if (opcode == 149) {
			placeholderTemplateId = buffer.readUnsignedShort();
		} else if (opcode == 249) {
			int length = buffer.readUnsignedByte();

			params = new HashMap<>(length);

			for (int i = 0; i < length; i++) {
				boolean isString = buffer.readUnsignedByte() == 1;
				int key = buffer.read24BitInt();
				Object value;

				if (isString) {
					value = buffer.readString317();
				} else {
					value = buffer.readInt();
				}

				params.put(key, value);
			}
		} else if (opcode == 255) {
			dataType = buffer.readUnsignedByte();
		} else {
			System.err.println("item : " + id + ", error decoding opcode : " + opcode + ", previous opcodes: " + Arrays.toString(previousOpcodes));
		}
		ArrayUtils.add(previousOpcodes, opcode);
	}

	@Override
	public void onCreate() {
		Map<Integer, ConfigExtensionBase> defs = PluginManager.get().getLoaderForType(PluginType.ITEM).getDefinitions();
		defs.put(id, this);
	}

	@Override
	public OutputStream[] encodeConfig317(String fileName) {
		Map<Integer, ConfigExtensionBase> defs = PluginManager.get().getLoaderForType(PluginType.ITEM).getDefinitions();

		OutputStream dat = new OutputStream();
		OutputStream idx = new OutputStream();

		idx.writeShort(defs.size());
		dat.writeShort(defs.size());

		for (int i = 0; i < defs.size(); i++) {
			ItemConfig def = (ItemConfig) defs.get(i);

			int start = dat.getPosition();

			if (def != null) {
				def.encode(dat);
			}

			dat.writeByte(0);

			int end = dat.getPosition();
			idx.writeShort(end - start);
		}

		return new OutputStream[] { dat, idx };
	}

	@Override
	public OutputStream encode(OutputStream buffer) {

		if (inventoryModel > -1) {
			buffer.writeByte(1);
			buffer.writeShort(inventoryModel);
		}

		if (!name.equals("null")) {
			buffer.writeByte(2);
			if (CacheLibrary.get().is317())
				buffer.writeString317(name);
			else
				buffer.writeString(name);
		}

		if (zoom2d != 2000) {
			buffer.writeByte(4);
			buffer.writeShort(zoom2d);
		}

		if (xan2d != 0) {
			buffer.writeByte(5);
			buffer.writeShort(xan2d);
		}

		if (yan2d != 0) {
			buffer.writeByte(6);
			buffer.writeShort(yan2d);
		}

		if (xOffset2d != 0) {
			buffer.writeByte(7);
			buffer.writeShort(xOffset2d);
		}

		if (yOffset2d != 0) {
			buffer.writeByte(8);
			buffer.writeShort(yOffset2d);
		}

		if (stackable != 0) {
			buffer.writeByte(11);
		}

		if (cost != 1) {
			buffer.writeByte(12);
			buffer.writeInt(cost);
		}

		if (members) {
			buffer.writeByte(16);
		}

		if (maleModel0 > -1) {
			buffer.writeByte(23);
			buffer.writeShort(maleModel0);
			buffer.writeByte(maleOffset);
		}
		
		if (maleModel1 > -1) {
			buffer.writeByte(24);
			buffer.writeShort(maleModel1);
		}
		
		if (femaleModel0 > -1) {
			buffer.writeByte(25);
			buffer.writeShort(femaleModel0);
			buffer.writeByte(femaleOffset);
		}
		
		if (femaleModel1 > -1) {
			buffer.writeByte(26);
			buffer.writeShort(femaleModel1);
		}
		
		for (int index = 0; index < 5; index++) {
			if (options[index] != null && !options[index].isEmpty() && !options[index].equals("null")) {
				buffer.writeByte(index + 30);
				if (CacheLibrary.get().is317())
					buffer.writeString317(options[index]);
				else
					buffer.writeString(options[index]);
			}
		}

		for (int index = 0; index < 5; index++) {
			if (interfaceOptions[index] != null && !interfaceOptions[index].isEmpty() && !interfaceOptions[index].equals("null")) {
				buffer.writeByte(index + 35);
				if (CacheLibrary.get().is317())
					buffer.writeString317(interfaceOptions[index]);
				else
					buffer.writeString(interfaceOptions[index]);
			}
		}

		if (replaceColors != null && originalColors != null) {
			buffer.writeByte(40);
			int length = Math.min(replaceColors.length, originalColors.length);
			buffer.writeByte(length);
			for (int index = 0; index < length; index++) {
				buffer.writeShort(replaceColors[index]);
				buffer.writeShort(originalColors[index]);
			}
		}

		if (textureFind != null && textureReplace != null) {
			buffer.writeByte(41);
			int length = Math.min(textureFind.length, textureReplace.length);
			buffer.writeByte(length);
			for (int index = 0; index < length; index++) {
				buffer.writeShort(textureFind[index]);
				buffer.writeShort(textureReplace[index]);
			}
		}

		if (shiftClickDropIndex != -2) {
			buffer.writeByte(42);
			buffer.writeByte(shiftClickDropIndex);
		}

		if (isTradeable) {
			buffer.writeByte(65);
		}

		if (maleModel2 > -1) {
			buffer.writeByte(78);
			buffer.writeShort(maleModel2);
		}
		
		if (femaleModel2 > -1) {
			buffer.writeByte(79);
			buffer.writeShort(femaleModel2);
		}
		
		if (maleHeadModel > -1) {
			buffer.writeByte(90);
			buffer.writeShort(maleHeadModel);
		}
		
		if (femaleHeadModel > -1) {
			buffer.writeByte(91);
			buffer.writeShort(femaleHeadModel);
		}
		
		if (maleHeadModel2 > -1) {
			buffer.writeByte(92);
			buffer.writeShort(maleHeadModel2);
		}
		
		if (femaleHeadModel2 > -1) {
			buffer.writeByte(93);
			buffer.writeShort(femaleHeadModel2);
		}
		
		if (zan2d != 0) {
			buffer.writeByte(95);
			buffer.writeShort(zan2d);
		}

		if (notedID > -1) {
			buffer.writeByte(97);
			buffer.writeShort(notedID);
		}
		
		if (notedTemplate > -1) {
			buffer.writeByte(98);
			buffer.writeShort(notedTemplate);
		}
		
		if (countCo != null && countObj != null) {

			int[] objHolder = new int[10];
			int[] coHolder = new int[10];

			for (int index = 0; index < 10; index++) {
				if (index < countCo.length && countCo[index] != 0) {
					coHolder[index] = countCo[index];
				}
			}

			for (int index = 0; index < 10; index++) {
				if (index < countObj.length && countObj[index] != 0) {
					objHolder[index] = countObj[index];
				}
			}

			for (int index = 0; index < 10; index++) {
				buffer.writeByte(index + 100);
				buffer.writeShort(objHolder[index]);
				buffer.writeShort(coHolder[index]);
			}

		}

		if (resizeX > -1) {
			buffer.writeByte(110);
			buffer.writeShort(resizeX);
		}

		if (resizeY > -1) {
			buffer.writeByte(111);
			buffer.writeShort(resizeY);
		}

		if (resizeZ > -1) {
			buffer.writeByte(112);
			buffer.writeShort(resizeZ);
		}

		if (ambient != 0) {
			buffer.writeByte(113);
			buffer.writeByte(ambient);
		}

		if (contrast != 0) {
			buffer.writeByte(114);
			buffer.writeByte(contrast);
		}

		if (team != 0) {
			buffer.writeByte(115);
			buffer.writeByte(team);
		}

		if (boughtId > -1) {
			buffer.writeByte(139);
			buffer.writeShort(boughtId);
		}

		if (boughtTemplateId > -1) {
			buffer.writeByte(140);
			buffer.writeShort(boughtTemplateId);
		}

		if (placeholderId > -1) {
			buffer.writeByte(148);
			buffer.writeShort(placeholderId);
		}

		if (placeholderTemplateId > -1) {
			buffer.writeByte(149);
			buffer.writeShort(placeholderTemplateId);
		}

		if (Objects.nonNull(params)) {
			buffer.writeByte(249);
			buffer.writeByte(params.size());
			for (int key : params.keySet()) {
				Object value = params.get(key);
				buffer.writeByte(value instanceof String ? 1 : 0);
				buffer.write24BitInt(key);
				if (value instanceof String) {
					if (CacheLibrary.get().is317())
						buffer.writeString317((String) value);
					else
						buffer.writeString((String) value);
				} else {
					buffer.writeInt((Integer) value);
				}
			}
		}

		if (dataType != 0) {
			buffer.writeByte(255);
			buffer.writeByte(dataType);
		}

		return buffer;
	}

	@Override
	public String toString() {
		return "[" + this.id + "] " + this.name;
	}

	@OrderType(priority = 1)
	public String name = "null";
	@OrderType(priority = 2)
	public String[] options = new String[] { "null", "null", "Take", "null", "null" };
	@OrderType(priority = 3)
	public String description;// itemExamine
	@OrderType(priority = 4)
	public String[] interfaceOptions = new String[] { "null", "null", "null", "null", "Drop" };
	@OrderType(priority = 5) @MeshIdentifier
	public int inventoryModel;
	@OrderType(priority = 6)
	public int zoom2d = 2000;
	@OrderType(priority = 7)
	public int xOffset2d = 0;
	@OrderType(priority = 8)
	public int yOffset2d = 0;
	@OrderType(priority = 9)
	public int resizeX = 128;
	@OrderType(priority = 10)
	public int resizeY = 128;
	@OrderType(priority = 11)
	public int resizeZ = 128;
	@OrderType(priority = 12) @MeshIdentifier
	public int maleModel0 = -1;
	@OrderType(priority = 13) @MeshIdentifier
	public int maleModel1 = -1;
	@OrderType(priority = 14) @MeshIdentifier
	public int maleModel2 = -1;
	@OrderType(priority = 15)
	public int maleOffset;
	@OrderType(priority = 16) @MeshIdentifier
	public int maleHeadModel = -1;
	@OrderType(priority = 17) @MeshIdentifier
	public int maleHeadModel2 = -1;
	@OrderType(priority = 18) @MeshIdentifier
	public int femaleModel0 = -1;
	@OrderType(priority = 19) @MeshIdentifier
	public int femaleModel1 = -1;
	@OrderType(priority = 20) @MeshIdentifier
	public int femaleModel2 = -1;
	@OrderType(priority = 21)
	public int femaleOffset;
	@OrderType(priority = 22) @MeshIdentifier
	public int femaleHeadModel = -1;
	@OrderType(priority = 23) @MeshIdentifier
	public int femaleHeadModel2 = -1;
	@OrderType(priority = 24)
	public int[] replaceColors;
	@OrderType(priority = 25)
	public int[] originalColors;
	@OrderType(priority = 26)
	public int[] textureFind;
	@OrderType(priority = 27)
	public int[] textureReplace;
	@OrderType(priority = 28)
	public int xan2d = 0;
	@OrderType(priority = 29)
	public int yan2d = 0;
	@OrderType(priority = 30)
	public int zan2d = 0;
	public int cost = 1;
	public boolean isTradeable;
	public int stackable = 0;
	public boolean members = false;
	public int ambient;
	public int contrast;
	public int[] countCo;
	public int[] countObj;
	public int notedID = -1;
	public int notedTemplate = -1;
	public int team;
	public int shiftClickDropIndex = -2;
	public int boughtId = -1;
	public int boughtTemplateId = -1;
	public int placeholderId = -1;
	public int placeholderTemplateId = -1;
	public HashMap<Integer, Object> params = null;
	@OrderType(priority = 31)
	public int dataType = 0;

	private static Map<Field, Integer> fieldPriorities;

	@Override
	public Map<Field, Integer> getPriority() {
		if (fieldPriorities != null)
			return fieldPriorities;
		Map<String, Pair<Field, Object>> values = ReflectionUtils.getValues(this);

		fieldPriorities = Maps.newHashMap();

		values.values().forEach(pair -> {
			Field field = pair.getKey();
			int priority = field.isAnnotationPresent(OrderType.class) ? field.getAnnotation(OrderType.class).priority() : 1000;
			fieldPriorities.put(field, priority);
		});
		return fieldPriorities;
	}

	@Override
	public List<Integer> getMeshIds() {
		List<Integer> meshes = Lists.newArrayList();
		try {
			for (Field field : this.getClass().getFields()) {
				if (field.isAnnotationPresent(MeshIdentifier.class)) {
					if (field.getType().isAssignableFrom(int.class) || field.getType().isAssignableFrom(short.class)) {
						meshes.add((int) field.get(this));
					} else if (field.getType().isAssignableFrom(int[][].class)) {
						int[][] values = (int[][]) field.get(this);
						for (int type = 0; type < values.length; type++) {
							int[] models = values[type];
							for (int model = 0; model < models.length; model++) {
								meshes.add(models[model]);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return meshes;
	}

	@Override
	public List<Pair<Integer, Integer>> getRecolors() {
		List<Pair<Integer, Integer>> pairs = Lists.newArrayList();
		try {
			if (replaceColors == null || originalColors == null) {
				return null;
			}
			int length = Math.min(replaceColors.length, originalColors.length);
			for (int index = 0; index < length; index++) {
				pairs.add(new Pair<>(replaceColors[index], originalColors[index]));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pairs;
	}

	@Override
	public List<Pair<Integer, Integer>> getRetextures() {
		List<Pair<Integer, Integer>> pairs = Lists.newArrayList();
		try {
			if (textureFind == null || textureReplace == null) {
				return null;
			}
			int length = Math.min(textureFind.length, textureReplace.length);
			for (int index = 0; index < length; index++) {
				pairs.add(new Pair<>(textureFind[index], textureReplace[index]));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pairs;
	}

	@Override
	public void copy(Object copyFrom) {
		ItemConfig from = (ItemConfig) copyFrom;
		inventoryModel = from.inventoryModel;
		name = from.name;
		System.out.println("new name: " + name);
		zoom2d = from.zoom2d;
		yan2d = from.yan2d;
		xan2d = from.xan2d;
		xOffset2d = from.xOffset2d;
		yOffset2d = from.yOffset2d;
		stackable = from.stackable;
		cost = from.cost;
		members = from.members;
		maleModel0 = from.maleModel0;
		maleOffset = from.maleOffset;
		maleModel1 = from.maleModel1;
		femaleModel0 = from.femaleModel0;
		femaleOffset = from.femaleOffset;
		femaleModel1 = from.femaleModel1;
		options = Arrays.copyOf(from.options, from.options.length);
		interfaceOptions = Arrays.copyOf(from.interfaceOptions, from.interfaceOptions.length);
		replaceColors = Arrays.copyOf(from.replaceColors, from.replaceColors.length);
		originalColors = Arrays.copyOf(from.originalColors, from.originalColors.length);
		textureFind = Arrays.copyOf(from.textureFind, from.textureFind.length);
		textureReplace = Arrays.copyOf(from.textureReplace, from.textureReplace.length);
		shiftClickDropIndex = from.shiftClickDropIndex;
		isTradeable = from.isTradeable;
		maleModel2 = from.maleModel2;
		femaleModel2 = from.femaleModel2;
		maleHeadModel = from.maleHeadModel;
		femaleHeadModel = from.femaleHeadModel;
		maleHeadModel2 = from.maleHeadModel2;
		femaleHeadModel2 = from.femaleHeadModel2;
		zan2d = from.zan2d;
		notedID = from.notedID;
		notedTemplate = from.notedTemplate;
		countObj = Arrays.copyOf(from.countObj, from.countObj.length);
		countCo = Arrays.copyOf(from.countCo, from.countCo.length);
		resizeX = from.resizeX;
		resizeY = from.resizeY;
		resizeZ = from.resizeZ;
		ambient = from.ambient;
		contrast = from.ambient;
		team = from.team;
		boughtId = from.boughtId;
		boughtTemplateId = from.boughtTemplateId;
		placeholderId = from.placeholderId;
		placeholderTemplateId = from.placeholderTemplateId;
	}
}
