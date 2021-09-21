package com;

import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.SneakyThrows;
import store.CacheLibrary;
import store.cache.index.ConfigType;
import store.cache.index.Index;
import store.cache.index.OSRSIndices;
import store.cache.index.archive.Archive;
import store.cache.index.archive.file.File;
import store.codec.osrs.NPCDefinition;
import store.codec.util.Utils;
import store.io.impl.OutputStream;
import store.plugin.Plugin;
import store.plugin.PluginType;
import store.progress.AbstractProgressListener;
import store.progress.ProgressListener;
import suite.annotation.PluginDescriptor;
import utility.XTEASManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ReverendDread on 7/8/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project ValkyrCacheSuite
 */
@PluginDescriptor(author = "ReverendDread", description = "A world map editor.", type = PluginType.WORLD_MAP, version = "184")
public class KronosDataPacker extends Plugin {

    private static final int[] BLACKLIST_ITEMS = {
            22610, 22613, 22616, 22619, 23615, //Vesta's
            22638, 22641, 22644, //Morrigan's
            22647, 22650, 22653, 22656, 23617, // Zuriel's
            22622, 22625, 22628, 22631, 23620, //Statius's
            6758, //Bonus exp scroll
            4067, //Vote ticket
    };
    private static final int[] BLACKLIST_NPCS = {
            2153, 5051, 5081
    };
    private static final int[] BLACKLIST_OBJECTS = {
            23311, 7389, 34752
    };
    private static final int[] BLACKLIST_SPOTS = {

    };
    private static final int[] BLACKLIST_SEQS = {

    };
    private static final int[] BLACKLIST_REGIONS = {
            10805, 10806, // seers home
            5535, // hydra
            9046, // mouse hole
            7992, 7991, 8247, 8248, //kronos home
            12342, // edge
            12850, // lumb
            10284, 10028 // donator zone
    };

    private static final boolean skeletons = false; // true
    private static final boolean skins = false; // true
    private static final boolean items = false; // true
    private static final boolean npcs = false; // true
    private static final boolean objects = false; // true
    private static final boolean spots = false; // true
    private static final boolean models = false; // true
    private static final boolean sequences = false; // true
    private static final boolean varbit = false; // true
    private static final boolean idk = false; // true
    private static final boolean maps = false; // true
    private static final boolean textures = false; // true
    private static final boolean sprites = false; // true
    private static final boolean floors = false;
    private static boolean interfaces = false; // true
    private static boolean enums = false; // true
    private static boolean cs2 = false; // true
    private static boolean hitsplat = false;
    private static boolean varp = false;
    private static boolean structs = true;


    private static final int ITEM_FROM = 24283;
    private static final int OBJECT_FROM = 37433;
    private static final int SKIN_FROM = 386; // 1886
    private static final int MAP_FROM = 3641;
    private static final int SPOT_FROM = 1744;
    private static final int NPC_FROM = 9298;
    private static final int MODEL_FROM = 38909;
    private static final int MODEL_TO = 42605;
    private static final int SEQUENCE_FROM = 8527;
    private static final int SPRITES_FROM = 2309;
    private static final int INTERFACES_FROM = 648;
    private static final int INTERFACES_TO = 701;
    private static final int ENUMS_FROM = 2639;
    private static final int CS2_FROM = 3140;
    
    private static final ProgressListener progressListener = new AbstractProgressListener() {
        
        @Override
        public void finish(String title, String message) {

        }

        @Override
        public void change(double progress, String message) {
            
        }
        
    };

    public static void main(String[] args) {
        //dumpModels();
        //dumpNpcsTo317(args);
        //dumpTo317(ConfigType.OBJECT,"loc3");
        //dumpMaps();
        main2(args);
    }

    private static final int MAX_REGION = 32768;

    @SneakyThrows
    public static void dumpMaps() {
        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache");
        Index runescape_maps = runescape_cache.getIndex(OSRSIndices.MAPS);

        @Cleanup
        RandomAccessFile raf = new RandomAccessFile(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\map_index").toPath().toString(), "rw");

        System.out.println("Generating map_index...");

        int total = 0;
        raf.seek(2L);

        int end;

        int mapCount = 0;
        int landCount = 0;

        for (end = 0; end < 256; end++) {
            for (int i = 0; i < 256; i++) {
                int var17 = end << 8 | i;
                int x = runescape_maps.getArchiveId("m" + end + "_" + i);
                int y = runescape_maps.getArchiveId("l" + end + "_" + i);
                //int x = cache.getFileId(5, "m" + end + "_" + i);
                //int y = cache.getFileId(5, "l" + end + "_" + i);
                if ((x != -1) && (y != -1)) {
                    raf.writeShort(var17);
                    raf.writeShort(x);
                    raf.writeShort(y);
                    total++;
                }
            }
        }

        end = (int) raf.getFilePointer();
        raf.seek(0L);
        raf.writeShort(total);
        raf.seek(end);
        raf.close();
        System.out.println("Done dumping map_index.");

        java.io.File dir = new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\maps\\");
        dir.mkdir();

        for (int region = 0; region < 32768; region++) {

            int x = (region >> 8);
            int y = (region & 0xff);

            byte[] data;
            java.io.File file;
            DataOutputStream dos;

            //Map
            int map = runescape_maps.getArchiveId("m" + x + "_" + y);
            if (map != -1) {
                data = runescape_maps.getArchive(map).getFile(0).getData();
                file = new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\maps\\", + map + ".dat");
                dos = new DataOutputStream(new FileOutputStream(file));
                dos.write(data);
                dos.close();
                System.out.println("Dumped map " + file.getName());
            }

            //Locations
            int location =  runescape_maps.getArchiveId("l" + x + "_" + y);
            if (location != -1) {
                int[] xteas = XTEASManager.lookup(region);
                data = runescape_maps.getArchive(location, xteas).getFile(0).getData();
                if (data == null)
                    continue;
                file = new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\maps\\", + location + ".dat");
                dos = new DataOutputStream(new FileOutputStream(file));
                dos.write(data);
                dos.close();
                System.out.println("Dumped landscape " + file.getName());
            }
        }

        System.gc();
    }

    public static void dumpModels() {
        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache");
        Index runescape_models = runescape_cache.getIndex(OSRSIndices.MODELS);

        int model_count = runescape_models.getLastArchive().getId();

        for (int i = 41516; i <= model_count; i++) {
            Archive model = runescape_models.getArchive(i);
            if (model == null)
                continue;
            File rs_file = model.getFile(0);
            if (rs_file == null)
                continue;
            byte[] rs_data = rs_file.getData();
            if (rs_data == null)
                continue;

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream( );
                stream.write(rs_data);
                FileOutputStream outputStream = new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\models\\" + i));
                stream.writeTo(outputStream);
                stream.close();
                stream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            /*try {
                OutputStream data = new OutputStream(items);
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\items.dat")));
                dos.write(data.flip());
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            //kronos_models.addArchive(i).addFile(0, model.getData());
            progressListener.notify(i / (double) model_count, "Dumping model {" + i + "/" + model_count + "}");
        }

        System.gc();
    }

    public static void dumpItemsTo317(String[] args) {
        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache");
        Index runescape_config = runescape_cache.getIndex(OSRSIndices.CONFIG);

        Archive items = runescape_config.getArchive(ConfigType.ITEM.getId());

        try {
            DataOutputStream dat = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\obj3.dat")));
            DataOutputStream idx = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\obj3.idx")));

            idx.writeShort(items.getFiles().length);
            dat.writeShort(items.getFiles().length);

            for (File file : items.getFiles()) {
                if (file == null) {
                    continue;
                }

                int start = dat.size();

                dat.write(file.getData());
                int end = dat.size();

                idx.writeShort(end - start);
                //System.out.println(Arrays.toString(file.getData()));
            }
            dat.close();
            dat.flush();
            idx.close();
            idx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dumpNpcsTo317(String[] args) {
        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache");
        Index runescape_config = runescape_cache.getIndex(OSRSIndices.CONFIG);

        Archive items = runescape_config.getArchive(ConfigType.NPC.getId());

        try {
            DataOutputStream dat = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\npc3.dat")));
            DataOutputStream idx = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\npc3.idx")));

            idx.writeShort(items.getFiles().length);
            dat.writeShort(items.getFiles().length);

            for (File file : items.getFiles()) {
                if (file == null) {
                    continue;
                }

                int start = dat.size();

                dat.write(file.getData());
                dat.write(0);
                int end = dat.size();

                idx.writeShort(end - start);
                System.out.println(Arrays.toString(file.getData()));
            }
            dat.close();
            dat.flush();
            idx.close();
            idx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dumpTo317(ConfigType type, String name) {
        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache");
        Index runescape_config = runescape_cache.getIndex(OSRSIndices.CONFIG);

        Archive items = runescape_config.getArchive(type.getId());

        try {
            DataOutputStream dat = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\" + name + ".dat")));
            DataOutputStream idx = new DataOutputStream(new FileOutputStream(new java.io.File("C:\\Users\\Indrek\\Desktop\\TestDump\\" + name + ".idx")));

            idx.writeShort(items.getFiles().length);
            dat.writeShort(items.getFiles().length);

            for (File file : items.getFiles()) {
                if (file == null) {
                    continue;
                }

                int start = dat.size();

                dat.write(file.getData());
                dat.write(0);
                int end = dat.size();

                idx.writeShort(end - start);
                System.out.println(Arrays.toString(file.getData()));
            }
            dat.close();
            dat.flush();
            idx.close();
            idx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) {

        progressListener.notify(0, "Initializing Kronos cache");
        CacheLibrary kronos_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache\\");
        progressListener.notify(0, "Initializing RuneScape cache");
        //CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Git\\Seers\\Cache197\\cache\\");

        //orig below
        //CacheLibrary runescape_cache = CacheLibrary.createUncached("F:\\Programmeerimine\\RSPS\\Tools\\OSDC 2.0\\2020-06-12-rev189\\cache"); // for adv options


        CacheLibrary runescape_cache = CacheLibrary.createUncached("C:\\Users\\Indrek\\Desktop\\cache 199");
        progressListener.notify(0, "Initializing XTEA manager");
        XTEASManager.get().init();

        Index kronos_config = kronos_cache.getIndex(OSRSIndices.CONFIG);
        Index runescape_config = runescape_cache.getIndex(OSRSIndices.CONFIG);

        if (skeletons) {

            Index runescape_skeletons = runescape_cache.getIndex(OSRSIndices.SKELETONS);
            Index kronos_skeletons = kronos_cache.getIndex(OSRSIndices.SKELETONS);

            int skeleton_count = runescape_skeletons.getLastArchive().getId();
            for (int i = 0; i < skeleton_count; i++) {
                kronos_skeletons.addArchive(runescape_skeletons.getArchive(i), true, true, i);
            }

            kronos_skeletons.update(progressListener);

        }

        if (skins) {

            Index runescape_skins = runescape_cache.getIndex(OSRSIndices.SKINS);
            Index kronos_skins = kronos_cache.getIndex(OSRSIndices.SKINS);

            int skin_count = runescape_skins.getLastArchive().getId();
            System.out.println("SKIN COUNT: " + skins);
            for (int i = SKIN_FROM; i < skin_count; i++) {
                if (runescape_skins.getArchive(i) == null) {
                    System.out.println("error packing skin archive: " + i);
                    continue;
                }
                kronos_skins.addArchive(runescape_skins.getArchive(i), true, true, i);
            }

            kronos_skins.update(progressListener);

        }

        if (items) {

            Archive kronos_items = kronos_config.getArchive(10);
            Archive runescape_items = runescape_config.getArchive(10);

            int item_count = runescape_config.getArchive(10).getLastFile().getId();

            for (int i = ITEM_FROM; i < item_count; i++) {
                if (blacklist(i, BLACKLIST_ITEMS))
                    continue;
                File rs_file = runescape_items.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_items.addFile(i, rs_data);
                progressListener.notify(i / (double) item_count, "Updating item {" + i + "/" + item_count + "}");
            }

        }

        if (npcs) {

            Archive kronos_npcs = kronos_config.getArchive(9);
            Archive runescape_npcs = runescape_config.getArchive(9);

            int npc_count = runescape_config.getArchive(9).getLastFile().getId();

            for (int i = NPC_FROM; i < npc_count; i++) {
                if (blacklist(i, BLACKLIST_NPCS))
                    continue;
                File rs_file = runescape_npcs.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_npcs.addFile(i, rs_data);
                progressListener.notify(i / (double) npc_count, "Updating npc {" + i + "/" + npc_count + "}");
            }

        }

        if (objects) {

            Archive kronos_objects = kronos_config.getArchive(6);
            Archive runescape_objects = runescape_config.getArchive(6);

            int object_count = runescape_config.getArchive(6).getLastFile().getId();

            for (int i = OBJECT_FROM; i < object_count; i++) {
                if (blacklist(i, BLACKLIST_OBJECTS))
                    continue;
                File rs_file = runescape_objects.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_objects.addFile(i, rs_data);
                progressListener.notify(i / (double) object_count, "Updating object {" + i + "/" + object_count + "}");
            }

        }

        if (spots) {

            Archive kronos_spots = kronos_config.getArchive(13);
            Archive runescape_spots = runescape_config.getArchive(13);

            int spots_count = runescape_config.getArchive(13).getLastFile().getId();

            for (int i = 0; i < spots_count; i++) {
                if (blacklist(i, BLACKLIST_SPOTS))
                    continue;
                File rs_file = runescape_spots.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_spots.addFile(i, rs_data);
                progressListener.notify(i / (double) spots_count, "Updating spot {" + i + "/" + spots_count + "}");
            }

        }

        if (sequences) {

            Archive kronos_seqs = kronos_config.getArchive(12);
            Archive runescape_seqs = runescape_config.getArchive(12);

            int sequence_count = runescape_config.getArchive(12).getLastFile().getId();

            for (int i = SEQUENCE_FROM; i < sequence_count; i++) {
                //if (blacklist(i, BLACKLIST_SEQS))
                    //continue;
                if (i != 15) {
                    continue;
                }
                File rs_file = runescape_seqs.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_seqs.addFile(i, rs_data);
                progressListener.notify(i / (double) sequence_count, "Updating sequence {" + i + "/" + sequence_count + "}");
            }

        }

        if (models) {

            Index kronos_models = kronos_cache.getIndex(OSRSIndices.MODELS);
            Index runescape_models = runescape_cache.getIndex(OSRSIndices.MODELS);

            int model_count = runescape_models.getLastArchive().getId();

            for (int i = MODEL_FROM; i <= MODEL_TO; i++) {
                Archive model = runescape_models.getArchive(i);
                if (model == null)
                    continue;
                File rs_file = model.getFile(0);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_models.addArchive(i).addFile(0, model.getData());
                progressListener.notify(i / (double) model_count, "Updating model {" + i + "/" + model_count + "}");
            }
            kronos_models.update(progressListener);

        }

        if (idk) {

            Archive kronos_idk = kronos_config.getArchive(3);
            Archive runescape_idk = runescape_config.getArchive(3);

            int idk_count = runescape_idk.getLastFile().getId();

            for (int i = 0; i < idk_count; i++) {
                File rs_file = runescape_idk.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_idk.addFile(i, rs_data);
                progressListener.notify(i / (double) idk_count, "Updating identity kit {" + i + "/" + idk_count + "}");
            }

        }

        /**
         * The regions to be overwritten that are below the new maps (MAP_FROM)
         */
        Set<Integer> OVERWRITE_REGIONS = new HashSet<>(Arrays.asList(
                8025, 8026, // new cons
                12078, 12587, 12588 // tempoross
        ));

        if (maps) {
            Index runescape_maps = runescape_cache.getIndex(OSRSIndices.MAPS);
            Index kronos_maps = kronos_cache.getIndex(OSRSIndices.MAPS);
            Map<Integer, int[]> update_keys = Maps.newHashMap();
            int max_region = Short.MAX_VALUE;
            for (int i = 0; i < max_region; i++) {
                int x = i >> 8;
                int y = i & 0xff;
                if (blacklist(i, BLACKLIST_REGIONS))
                    continue;
                String map_name = "m" + x + "_" + y;
                String land_name = "l" + x + "_" + y;
                int map_id = runescape_maps.getArchiveId(map_name);
                int land_id = runescape_maps.getArchiveId(land_name);

                if (!OVERWRITE_REGIONS.contains(i) && map_id < MAP_FROM && land_id < MAP_FROM) {
                    continue;
                }

                int[] xteas = XTEASManager.lookup(i);

                boolean skip = false;

                for (int xt = 0; xt < xteas.length; xt++) {
                    if (xteas[xt] == 0) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }

                if (map_id != -1) {
                    File map_file = runescape_maps.getFile(map_id, 0);
                    byte[] map_data = map_file.getData();
                    if (map_data != null) {
                        kronos_maps.addArchive(map_id, Utils.getNameHash(map_name), true).addFile(0, map_data);
                    }
                }
                if (land_id != -1) {
                    Archive land_archive = runescape_maps.getArchive(land_id, xteas);
                    File land_file = land_archive.getFile(0);
                    byte[] land_data = land_file.getData();
                    if (land_data != null) {
                        kronos_maps.addArchive(land_id, Utils.getNameHash(land_name), true).addFile(0, land_data);
                        update_keys.put(land_id, xteas);
                    }
                    System.out.println("  \"" + i + "\": [");
                    for (int xt = 0; xt < xteas.length; xt++) {
                        if (xt == xteas.length - 1) {
                            System.out.println("\t" + xteas[xt]);
                        } else {
                            System.out.println("\t" + xteas[xt] + ",");
                        }
                    }
                    System.out.println("  ],");
                }
                //System.out.println("Packed region: " + i + " with map: " + map_id + " land: " + land_id);
                //progressListener.notify(i / (double) max_region, "Updating region {" + i + "/" + max_region + "}");
            }
            kronos_maps.update(update_keys);
        }

        if (textures) {
            Index runescape_textures = runescape_cache.getIndex(OSRSIndices.TEXTURES);
            Index kronos_textures = kronos_cache.getIndex(OSRSIndices.TEXTURES);

            int skeleton_count = runescape_textures.getLastArchive().getId();
            for (int i = 0; i < skeleton_count; i++) {
                kronos_textures.addArchive(runescape_textures.getArchive(i), true);
            }

            kronos_textures.update(progressListener);
        }

        if (sprites) {
            Index runescape_sprites = runescape_cache.getIndex(OSRSIndices.SPRITES);
            Index kronos_sprites = kronos_cache.getIndex(OSRSIndices.SPRITES);

            int kronos_sprite_count = kronos_sprites.getLastArchive().getId();

            System.out.println("kronos sprites: " + kronos_sprite_count);

            /*for (int i = 3380; i < 3663; i++) {
                kronos_sprites.removeArchive(i);
            }*/


            int sprite_count = runescape_sprites.getLastArchive().getId();
            System.out.println("TOTAL RS SPRITES: " + sprite_count);
            // replace starts from 2269

            kronos_sprites.removeArchive(2309);
            kronos_sprites.addArchive(runescape_sprites.getArchive(2309),2309,true);

            /*for (int i = 2309; i < 2310; i++) {
                kronos_sprites.addArchive(runescape_sprites.getArchive(i), true);
            }*/

            kronos_sprites.update(progressListener);
        }

        if (floors) {

            Archive kronos_flo = kronos_config.getArchive(1);
            Archive runescape_flo = runescape_config.getArchive(1);

            int flo_count = runescape_config.getArchive(1).getLastFile().getId();

            for (int i = 0; i < flo_count; i++) {
                File rs_file = runescape_flo.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_flo.addFile(i, rs_data);
                progressListener.notify(i / (double) flo_count, "Updating floor underlay {" + i + "/" + flo_count + "}");
            }

            kronos_flo = kronos_config.getArchive(4);
            runescape_flo = runescape_config.getArchive(4);

            flo_count = runescape_config.getArchive(4).getLastFile().getId();

            for (int i = 0; i < flo_count; i++) {
                File rs_file = runescape_flo.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_flo.addFile(i, rs_data);
                progressListener.notify(i / (double) flo_count, "Updating floor overlay {" + i + "/" + flo_count + "}");
            }

        }

        if (interfaces) {
            Index runescape_itf = runescape_cache.getIndex(OSRSIndices.INTERFACE);
            Index kronos_itf = kronos_cache.getIndex(OSRSIndices.INTERFACE);

            int count = runescape_itf.getLastArchive().getId();
            System.out.println("TOTAL INTERFACES: " + count);
            for (int i = 0; i < count; i++) {
                /*if (i != 60) {
                    continue;
                }*/
                if (i != 303) {
                    continue;
                }
                kronos_itf.addArchive(runescape_itf.getArchive(i), true, true);
            }
            /*for (int i = INTERFACES_FROM; i < count; i++) {
                if (i != 660 && i != 667 && i != 682 && i != 683 && i != 687) {
                    continue;
                }
                kronos_itf.addArchive(runescape_itf.getArchive(i), true, true);
            }*/

            kronos_itf.update(progressListener);
        }

        if (enums) {
            Archive kronos_enum = kronos_config.getArchive(8);
            Archive runescape_enum = runescape_config.getArchive(8);

            int enum_count = runescape_config.getArchive(8).getLastFile().getId();

            System.out.println("Total ENUMS: " + enum_count);

            for (int i = 2320; i < 2322; i++) {
                File rs_file = runescape_enum.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;

                kronos_enum.addFile(i, rs_data);
                progressListener.notify(i / (double) enum_count, "Updating enum {" + i + "/" + enum_count + "}");
            }
        }

        if (structs) {
            Archive kronos_struct = kronos_config.getArchive(8);
            Archive runescape_struct = runescape_config.getArchive(8);

            int struct_count = runescape_config.getArchive(8).getLastFile().getId();

            System.out.println("Total STRUCTS: " + struct_count);

            for (int i = 588; i < 593; i++) {
                File rs_file = runescape_struct.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;

                kronos_struct.addFile(i, rs_data);
                progressListener.notify(i / (double) struct_count, "Updating struct {" + i + "/" + struct_count + "}");
            }
        }

        if (cs2) {
            Index runescape_cs2 = runescape_cache.getIndex(OSRSIndices.CLIENT_SCRIPT);
            Index kronos_cs2 = kronos_cache.getIndex(OSRSIndices.CLIENT_SCRIPT);

            int ogcount = kronos_cs2.getLastArchive().getId();
            int count = runescape_cs2.getLastArchive().getId();
            System.out.println("CS2 Diff: " + ogcount + "/" + count);
            Set<Integer> whitelist = new HashSet<>(Arrays.asList(
                    //439, 441, 531, 1962, 2917, // Adv. options
                    //2099, 2100, 2101, 2102, 2103, 62, 2888, 2887, 3427, 1414 // health hud -- working
                    //3174, 3175, 3310, 3893, 3946, 3427, 5221, 3971, 4072, 4073, 3947, 3948, 3949, 3950, 3952, 3953, 3980,
                    //4765, 4766
                    //2797, 2798, 2799, 2800, 2801, 2802, 2803, 2804, 3040, 3041
                    //907
                    //3970, 3893, 3946, 3947, 3948, 3427, 3310, 3971, 3980
                    /*2497,
                    2500,
                    2501,
                    2502,
                    2503,
                    2505,
                    2506,
                    2833,
                    3205,
                    3267,
                    3268,
                    3269,
                    3270*/
                    2833
                    /*2099, 3427, 2888, 2887,
                    2100,
                    2101,
                    2102,
                    2103,
                    1282,
                    1490,
                    2236,
                    1045,
                    1046,
                    1414,
                    62,
                    294, 295, 296, 297, 274 // bank??*/

            ));
            for (int i = 0; i < count; i++) {
                if (!whitelist.contains(i)) {
                    continue;
                }

                Archive archive = runescape_cs2.getArchive(i);
                if (archive != null) {
                    Archive kronosArchive = kronos_cs2.getArchive(i);

                    if (kronosArchive == null) {
                        kronos_cs2.addArchive(i).addFile(archive.getFile(0));
                        System.out.println("Adding missing cs2 with id: " + i);
                    } else {
                        kronosArchive.addFile(archive.getFile(0));
                        System.out.println("Replace existing cs2 with id: " + i);
                    }
                }
            }

            kronos_cs2.update(progressListener);
        }

        if (hitsplat) {
            int[] archives = {ConfigType.HITSPLAT.getId(), ConfigType.HEALTHBAR.getId()};
            for (int i = 0; i < archives.length; i++) {
                Archive kronos_enum = kronos_config.getArchive(archives[i]);
                Archive runescape_enum = runescape_config.getArchive(archives[i]);

                int og = kronos_config.getArchive(archives[i]).getLastFile().getId();

                System.out.println("OG COUNT: " + og);

                int count = runescape_config.getArchive(archives[i]).getLastFile().getId();

                System.out.println("Total in archive " + archives[i] + ": " + count);

                for (int f = 0; f <= count; f++) {
                    if (archives[i] == ConfigType.HITSPLAT.getId() && (i == 1 || i >= 6 && i <= 11 || i == 14 || i == 15))
                        continue;
                    File rs_file = runescape_enum.getFile(f);
                    if (rs_file == null) {
                        System.out.println("file null at idx: " + f);
                        continue;
                    }
                    byte[] rs_data = rs_file.getData();
                    if (rs_data == null) {
                        System.out.println("data null at idx: " + f);
                        continue;
                    }
                    kronos_enum.addFile(f, rs_data);
                    progressListener.notify(f / (double) count, "Updating archive " + archives[i] + " {" + f + "/" + count + "}");
                }
            }
        }

        if (varbit) {

            Archive kronos_varbit = kronos_config.getArchive(14);
            Archive runescape_varbit = runescape_config.getArchive(14);

            int varbit_count = runescape_varbit.getLastFile().getId();

            System.out.println("OG varbit: " + kronos_varbit.getLastFile().getId());
            System.out.println("rs varbit: " + varbit_count);

            /*for (int i = 0; i < varbit_count; i++) {
                File rs_file = runescape_varbit.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_varbit.addFile(i, rs_data);
                progressListener.notify(i / (double) varbit_count, "Updating sequence {" + i + "/" + varbit_count + "}");
            }*/

            /**
             * Adding custom varpbits.
             */
            for (int idx = 0; idx < 10; idx++) {
                setVarpBit(kronos_varbit, 15000 + idx, buildVarbit(3900 + idx, 0, 28));
            }
        }

        if (varp) {

            Archive kronos_hit = kronos_config.getArchive(16);
            Archive runescape_hit = runescape_config.getArchive(16);

            int og = kronos_hit.getLastFile().getId();

            System.out.println("OG varp COUNT: " + og);

            int varbit_count = runescape_hit.getLastFile().getId();

            System.out.println("RS varp count: " + varbit_count);

            /*for (int i = 0; i < varbit_count; i++) {
                File rs_file = runescape_hit.getFile(i);
                if (rs_file == null)
                    continue;
                byte[] rs_data = rs_file.getData();
                if (rs_data == null)
                    continue;
                kronos_hit.addFile(i, rs_data);
                progressListener.notify(i / (double) varbit_count, "Updating varp {" + i + "/" + varbit_count + "}");
            }*/

            /*OG COUNT: 2735
            RS count: 2736*/
            // custom varps are 2735 - 2741
            // custom varps = 3900 - 3904
            //addNewVarp(kronos_hit, 3900);
            for (int idx = 0; idx < 10; idx++) {
                addNewVarp(kronos_hit, 3900 + idx);
            }
        }


        //Update config index
        kronos_config.update(progressListener);
    }

    private static void setVarpBit(Archive archive, int varpBit, byte[] data) {
        archive.addFile(varpBit, data);
        System.out.println("Set varpbit: " + varpBit + " = " + Arrays.toString(data));
    }

    private static void addNewVarp(Archive archive, int varpId) {
        archive.addFile(varpId, new byte[] { 0 });
        System.out.println("Packed new varp: " + varpId);
    }

    private static byte[] buildVarbit(int varpId, int lsb, int msb) {
        OutputStream stream = new OutputStream(6);
        stream.writeByte(1);
        stream.writeShort(varpId);
        stream.writeByte(lsb);
        stream.writeByte(msb);
        return stream.getBytes();
    }

    @Override
    public boolean load() {
        try {

//            Index blob = CacheLibrary.get().getIndex(19);
//            Index geom = CacheLibrary.get().getIndex(18);
//            Index ground = CacheLibrary.get().getIndex(20);
//            WorldMap worldMap = new WorldMap();
//            worldMap.decode(blob, geom, ground);
//            WorldMapManager manager = new WorldMapManager(null, null, geom, ground);
//            manager.load(blob, worldMap.currentMapArea.internalName, true);
//
//            int width = manager.getMapArea().getRegionHighX() - manager.getMapArea().getRegionLowX() + 1;
//            int height = manager.getMapArea().getRegionHighY() - manager.getMapArea().getRegionLowY() + 1;
//
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    if (manager.getRegions()[x][y] != null) {
//                        manager.getRegions()[x][y].decode(4, geom, ground);
//                    }
//                }
//            }

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public String getFXML() {
        return null;
    }

    public static boolean blacklist(int i, int[] array) {
        for (int element : array) {
            if (element == i)
            return true;
        }
        return false;
    }

}
