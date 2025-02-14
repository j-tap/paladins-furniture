package com.unlikepaladin.pfm.runtime;

import com.google.common.base.Stopwatch;
import com.google.common.hash.HashCode;
import com.mojang.bridge.game.PackType;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.screens.PFMGeneratingOverlay;
import com.unlikepaladin.pfm.runtime.assets.PFMBlockstateModelProvider;
import com.unlikepaladin.pfm.runtime.assets.PFMLangProvider;
import com.unlikepaladin.pfm.runtime.data.PFMLootTableProvider;
import com.unlikepaladin.pfm.runtime.data.PFMMCMetaProvider;
import com.unlikepaladin.pfm.runtime.data.PFMRecipeProvider;
import com.unlikepaladin.pfm.runtime.data.PFMTagProvider;
import com.unlikepaladin.pfm.utilities.PFMFileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.data.DataCache;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PFMDataGenerator extends PFMGenerator {
    public static boolean FROZEN = false;
    private int count;
    private String progress;

    public PFMDataGenerator(Path output, boolean logOrDebug) {
        super(output, logOrDebug, LogManager.getLogger("PFM-DataGen"));
        count = 4;
    }
    public void run() throws IOException {
        if (!FROZEN) {
            count = 0;
            setDataRunning(true);
            log("Packs:");
            for (ResourcePack pack : PFMRuntimeResources.RESOURCE_PACK_LIST) {
                log("\tPack {}", pack.getName());
                for (String namespace : pack.getNamespaces(ResourceType.SERVER_DATA)) {
                    log("\t\tNamespace {}", namespace);
                }
            }
            FROZEN = true;
            Path modListPath = output.resolve("modsList");
            Path hashPath = output.resolve("dataHash");
            if (!modListPath.toFile().isFile()) {
                Files.deleteIfExists(modListPath);
                Files.createFile(modListPath);
            }
            if (!hashPath.toFile().isFile()) {
                Files.deleteIfExists(hashPath);
                Files.createFile(hashPath);
            }
            List<String> hashToCompare = hashDirectory(output.toFile(), false);
            List<String> oldHash = Files.readAllLines(hashPath);
            List<String> modList = Files.readAllLines(modListPath);
            if (!hashToCompare.toString().equals(oldHash.toString()) || !modList.toString().replace("[", "").replace("]", "").equals(PaladinFurnitureMod.getVersionMap().toString())) {
                getLogger().info("Starting PFM Data Generation");
                //MinecraftClient.getInstance().setOverlay(new PFMGeneratingOverlay(MinecraftClient.getInstance().getOverlay(), this, MinecraftClient.getInstance(), true));
                PFMFileUtil.deleteDir(output.toFile());
                DataCache dataCache = new DataCache(output, "cache");
                dataCache.ignore(output.resolve("version.json"));
                Stopwatch stopwatch = Stopwatch.createStarted();
                Stopwatch stopwatch2 = Stopwatch.createUnstarted();

                log("Starting provider: {}", "PFM PFMTags");
                stopwatch2.start();
                new PFMTagProvider(this).run(dataCache);
                count++;
                stopwatch2.stop();
                log("{} finished after {} ms", "PFM PFMTags", stopwatch2.elapsed(TimeUnit.MILLISECONDS));
                stopwatch2.reset();

                log("Starting provider: {}", "PFM Drops");
                stopwatch2.start();
                new PFMLootTableProvider(this).run(dataCache);
                count++;
                stopwatch2.stop();
                log("{} finished after {} ms", "PFM Drops", stopwatch2.elapsed(TimeUnit.MILLISECONDS));
                stopwatch2.reset();

                log("Starting provider: {}", "PFM Recipes");
                stopwatch2.start();
                new PFMRecipeProvider(this).run(dataCache);
                count++;
                stopwatch2.stop();
                log("{} finished after {} ms", "PFM Recipes", stopwatch2.elapsed(TimeUnit.MILLISECONDS));
                stopwatch2.reset();

                log("Starting provider: {}", "PFM MC Meta");
                stopwatch2.start();
                new PFMMCMetaProvider(this).run(PackType.DATA, "PFM-Data");
                count++;
                stopwatch2.stop();
                log("{} finished after {} ms", "PFM MC Meta", stopwatch2.elapsed(TimeUnit.MILLISECONDS));

                getLogger().info("Data providers took: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

                dataCache.write();

                Files.deleteIfExists(hashPath);
                Files.createFile(hashPath);
                List<String> newDataHash = hashDirectory(output.toFile(), false);
                Files.writeString(PFMRuntimeResources.createDirIfNeeded(hashPath), newDataHash.toString().replace("[", "").replace("]", ""), StandardOpenOption.APPEND);

                Files.deleteIfExists(modListPath);
                Files.createFile(modListPath);
                Files.writeString(PFMRuntimeResources.createDirIfNeeded(modListPath), PaladinFurnitureMod.getVersionMap().toString().replace("[", "").replace("]", ""), StandardOpenOption.APPEND);
            } else {
                log("Data Hash and Mod list matched, skipping generation");
            }
            setDataRunning(false);
        }
    }

    @Override
    public float getProgress() {
        return (float) count / 4;
    }

    @Override
    public void setProgress(String progress) {
        this.progress = progress;
    }

    @Override
    public String getProgressString() {
        return progress;
    }
}