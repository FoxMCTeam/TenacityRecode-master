package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SoundList {
    @Getter
    private final List<SoundEntry> soundList = Lists.newArrayList();

    /**
     * if true it will override all the sounds from the resourcepacks loaded before
     */
    @Setter
    private boolean replaceExisting;
    private SoundCategory category;

    public boolean canReplaceExisting() {
        return this.replaceExisting;
    }

    public SoundCategory getSoundCategory() {
        return this.category;
    }

    public void setSoundCategory(SoundCategory soundCat) {
        this.category = soundCat;
    }

    public static class SoundEntry {
        private String name;
        private float volume = 1.0F;
        private float pitch = 1.0F;
        private int weight = 1;
        private Type type = Type.FILE;
        @Getter
        @Setter
        private boolean streaming = false;

        public String getSoundEntryName() {
            return this.name;
        }

        public void setSoundEntryName(String nameIn) {
            this.name = nameIn;
        }

        public float getSoundEntryVolume() {
            return this.volume;
        }

        public void setSoundEntryVolume(float volumeIn) {
            this.volume = volumeIn;
        }

        public float getSoundEntryPitch() {
            return this.pitch;
        }

        public void setSoundEntryPitch(float pitchIn) {
            this.pitch = pitchIn;
        }

        public int getSoundEntryWeight() {
            return this.weight;
        }

        public void setSoundEntryWeight(int weightIn) {
            this.weight = weightIn;
        }

        public Type getSoundEntryType() {
            return this.type;
        }

        public void setSoundEntryType(Type typeIn) {
            this.type = typeIn;
        }

        public enum Type {
            FILE("file"),
            SOUND_EVENT("event");

            private final String field_148583_c;

            Type(String p_i45109_3_) {
                this.field_148583_c = p_i45109_3_;
            }

            public static Type getType(String p_148580_0_) {
                for (Type soundlist$soundentry$type : values()) {
                    if (soundlist$soundentry$type.field_148583_c.equals(p_148580_0_)) {
                        return soundlist$soundentry$type;
                    }
                }

                return null;
            }
        }
    }
}
