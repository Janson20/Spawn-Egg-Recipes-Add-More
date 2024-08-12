package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ID_VALUE = 254;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SyncedDataHolder entity;
    private final SynchedEntityData.DataItem<?>[] itemsById;
    private boolean isDirty;

    SynchedEntityData(SyncedDataHolder p_326156_, SynchedEntityData.DataItem<?>[] p_326032_) {
        this.entity = p_326156_;
        this.itemsById = p_326032_;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> p_135354_, EntityDataSerializer<T> p_135355_) {
        if (true || LOGGER.isDebugEnabled()) { // Forge: This is very useful for mods that register keys on classes that are not their own
            try {
                Class<?> oclass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!oclass.equals(p_135354_)) {
                    // Forge: log at warn, mods should not add to classes that they don't own, and only add stacktrace when in debug is enabled as it is mostly not needed and consumes time
                    if (LOGGER.isDebugEnabled()) LOGGER.warn("defineId called for: {} from {}", p_135354_, oclass, new RuntimeException());
                    else LOGGER.warn("defineId called for: {} from {}", p_135354_, oclass);
                }
            } catch (ClassNotFoundException classnotfoundexception) {
            }
        }

        int i = ID_REGISTRY.define(p_135354_);
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        } else {
            return p_135355_.createAccessor(i);
        }
    }

    private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> p_135380_) {
        return (SynchedEntityData.DataItem<T>)this.itemsById[p_135380_.id()];
    }

    public <T> T get(EntityDataAccessor<T> p_135371_) {
        return this.getItem(p_135371_).getValue();
    }

    public <T> void set(EntityDataAccessor<T> p_135382_, T p_135383_) {
        this.set(p_135382_, p_135383_, false);
    }

    public <T> void set(EntityDataAccessor<T> p_276368_, T p_276363_, boolean p_276370_) {
        SynchedEntityData.DataItem<T> dataitem = this.getItem(p_276368_);
        if (p_276370_ || ObjectUtils.notEqual(p_276363_, dataitem.getValue())) {
            dataitem.setValue(p_276363_);
            this.entity.onSyncedDataUpdated(p_276368_);
            dataitem.setDirty(true);
            this.isDirty = true;
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        } else {
            this.isDirty = false;
            List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

            for (SynchedEntityData.DataItem<?> dataitem : this.itemsById) {
                if (dataitem.isDirty()) {
                    dataitem.setDirty(false);
                    list.add(dataitem.value());
                }
            }

            return list;
        }
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
        List<SynchedEntityData.DataValue<?>> list = null;

        for (SynchedEntityData.DataItem<?> dataitem : this.itemsById) {
            if (!dataitem.isSetToDefault()) {
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.add(dataitem.value());
            }
        }

        return list;
    }

    public void assignValues(List<SynchedEntityData.DataValue<?>> p_135357_) {
        for (SynchedEntityData.DataValue<?> datavalue : p_135357_) {
            SynchedEntityData.DataItem<?> dataitem = this.itemsById[datavalue.id];
            this.assignValue(dataitem, datavalue);
            this.entity.onSyncedDataUpdated(dataitem.getAccessor());
        }

        this.entity.onSyncedDataUpdated(p_135357_);
    }

    private <T> void assignValue(SynchedEntityData.DataItem<T> p_135376_, SynchedEntityData.DataValue<?> p_254484_) {
        if (!Objects.equals(p_254484_.serializer(), p_135376_.accessor.serializer())) {
            throw new IllegalStateException(
                String.format(
                    Locale.ROOT,
                    "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
                    p_135376_.accessor.id(),
                    this.entity,
                    p_135376_.value,
                    p_135376_.value.getClass(),
                    p_254484_.value,
                    p_254484_.value.getClass()
                )
            );
        } else {
            p_135376_.setValue((T)p_254484_.value);
        }
    }

    public static class Builder {
        private final SyncedDataHolder entity;
        private final SynchedEntityData.DataItem<?>[] itemsById;

        public Builder(SyncedDataHolder p_326500_) {
            this.entity = p_326500_;
            this.itemsById = new SynchedEntityData.DataItem[SynchedEntityData.ID_REGISTRY.getCount(p_326500_.getClass())];
        }

        public <T> SynchedEntityData.Builder define(EntityDataAccessor<T> p_326262_, T p_326000_) {
            int i = p_326262_.id();
            if (i > this.itemsById.length) {
                throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.itemsById.length + ")");
            } else if (this.itemsById[i] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + i + "!");
            } else if (EntityDataSerializers.getSerializedId(p_326262_.serializer()) < 0) {
                throw new IllegalArgumentException("Unregistered serializer " + p_326262_.serializer() + " for " + i + "!");
            } else {
                this.itemsById[p_326262_.id()] = new SynchedEntityData.DataItem<>(p_326262_, p_326000_);
                return this;
            }
        }

        public SynchedEntityData build() {
            for (int i = 0; i < this.itemsById.length; i++) {
                if (this.itemsById[i] == null) {
                    throw new IllegalStateException("Entity " + this.entity.getClass() + " has not defined synched data value " + i);
                }
            }

            return new SynchedEntityData(this.entity, this.itemsById);
        }
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> p_135394_, T p_135395_) {
            this.accessor = p_135394_;
            this.initialValue = p_135395_;
            this.value = p_135395_;
        }

        public EntityDataAccessor<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T p_135398_) {
            this.value = p_135398_;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean p_135402_) {
            this.dirty = p_135402_;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public SynchedEntityData.DataValue<T> value() {
            return SynchedEntityData.DataValue.create(this.accessor, this.value);
        }
    }

    public static record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {
        public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> p_254543_, T p_254138_) {
            EntityDataSerializer<T> entitydataserializer = p_254543_.serializer();
            return new SynchedEntityData.DataValue<>(p_254543_.id(), entitydataserializer, entitydataserializer.copy(p_254138_));
        }

        public void write(RegistryFriendlyByteBuf p_320650_) {
            int i = EntityDataSerializers.getSerializedId(this.serializer);
            if (i < 0) {
                throw new EncoderException("Unknown serializer type " + this.serializer);
            } else {
                p_320650_.writeByte(this.id);
                p_320650_.writeVarInt(i);
                this.serializer.codec().encode(p_320650_, this.value);
            }
        }

        public static SynchedEntityData.DataValue<?> read(RegistryFriendlyByteBuf p_319816_, int p_254356_) {
            int i = p_319816_.readVarInt();
            EntityDataSerializer<?> entitydataserializer = EntityDataSerializers.getSerializer(i);
            if (entitydataserializer == null) {
                throw new DecoderException("Unknown serializer type " + i);
            } else {
                return read(p_319816_, p_254356_, entitydataserializer);
            }
        }

        private static <T> SynchedEntityData.DataValue<T> read(RegistryFriendlyByteBuf p_320893_, int p_253899_, EntityDataSerializer<T> p_254222_) {
            return new SynchedEntityData.DataValue<>(p_253899_, p_254222_, p_254222_.codec().decode(p_320893_));
        }
    }
}
