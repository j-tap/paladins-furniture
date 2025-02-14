package com.unlikepaladin.pfm.runtime.data;

import com.mojang.bridge.game.PackType;
import com.unlikepaladin.pfm.runtime.PFMDataGenerator;
import com.unlikepaladin.pfm.runtime.PFMGenerator;
import com.unlikepaladin.pfm.runtime.PFMProvider;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import net.minecraft.SharedConstants;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PFMMCMetaProvider extends PFMProvider {

    public PFMMCMetaProvider(PFMGenerator parent) {
        super(parent);
        parent.setProgress("Generating Minecraft Metadata");
    }

    public void run(PackType type, String description) {
        try(BufferedWriter writer = IOUtils.buffer(new FileWriter(new File(PFMRuntimeResources.createDirIfNeeded(getParent().getOutput()).toFile(), "pack.mcmeta")))) {
            writer.write("{\n");
            writer.write("  \"pack\":\n   {\n");
            writer.write("          \"pack_format\": ");
            writer.write(String.valueOf(SharedConstants.getGameVersion().getPackVersion(type)));
            writer.write(",\n           \"description\" : \"" + description + "\"\n  }\n");
            writer.write("}");
        } catch (IOException e) {
            getParent().getLogger().error("Writer exception: " + e);
            e.printStackTrace();
        }
    }
}
