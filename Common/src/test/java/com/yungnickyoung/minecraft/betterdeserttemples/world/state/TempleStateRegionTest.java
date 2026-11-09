package com.yungnickyoung.minecraft.betterdeserttemples.world.state;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A temple region file that cannot be read must not be overwritten with a tag holding only the temple being queried:
 * that would silently drop the cleared state of every other temple in the region (issue #46).
 */
class TempleStateRegionTest {
    private static final String KEY = "r.0.0.temples";
    private static final BlockPos CLEARED_A = new BlockPos(16, 0, 32);
    private static final BlockPos CLEARED_B = new BlockPos(160, 0, 320);
    private static final BlockPos QUERIED = new BlockPos(48, 0, 64);

    /** Valid region file with two cleared temples, cut short (EOFException while reading). */
    @Test
    void truncatedRegionFileIsKept(@TempDir Path dir) throws IOException {
        byte[] valid = validRegionFile(dir);
        byte[] corrupt = Arrays.copyOf(valid, valid.length - 6);
        assertCorruptFileSurvivesQuery(dir, corrupt);
    }

    /** Region file whose first entry has an invalid tag id (NbtIo throws a ReportedNbtException). */
    @Test
    void invalidTagIdRegionFileIsKept(@TempDir Path dir) throws IOException {
        byte[] corrupt = validRegionFile(dir);
        corrupt[3] = 31; // root compound header is 3 bytes (type 10, empty name); byte 3 = type of the first entry
        assertCorruptFileSurvivesQuery(dir, corrupt);
    }

    private static byte[] validRegionFile(Path dir) throws IOException {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(CLEARED_A.toString(), true);
        tag.putBoolean(CLEARED_B.toString(), true);
        Path file = dir.resolve("valid");
        NbtIo.write(tag, file);
        byte[] bytes = Files.readAllBytes(file);
        Files.delete(file);
        return bytes;
    }

    private static void assertCorruptFileSurvivesQuery(Path dir, byte[] corrupt) throws IOException {
        Files.write(dir.resolve(KEY), corrupt);

        TempleStateRegion region = new TempleStateRegion(dir, KEY);
        assertDoesNotThrow(() -> region.isTempleCleared(QUERIED), "reading an unreadable region file must not throw");

        List<Path> files;
        try (Stream<Path> s = Files.list(dir)) {
            files = s.toList();
        }
        boolean kept = false;
        for (Path f : files) {
            if (Arrays.equals(Files.readAllBytes(f), corrupt)) kept = true;
        }
        assertTrue(kept, "the unreadable region file was overwritten; files now: " + files);

        // The region still works afterwards: a newly cleared temple is stored and read back.
        region.setTempleCleared(QUERIED, true);
        assertTrue(new TempleStateRegion(dir, KEY).isTempleCleared(QUERIED), "new state not persisted after recovery");
    }
}
