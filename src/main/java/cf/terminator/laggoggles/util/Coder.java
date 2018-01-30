package cf.terminator.laggoggles.util;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

public abstract class Coder<T>{

    public static final Coder<Integer> INTEGER = new Coder<Integer>() {
        @Override
        public Integer read(ByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public void write(Integer var, ByteBuf buf) {
            buf.writeInt(var);
        }
    };

    public static final Coder<String> STRING = new Coder<String>() {
        @Override
        public String read(ByteBuf buf) {
            return ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void write(String var, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, var);
        }
    };

    public static final Coder<UUID> UUID = new Coder<UUID>() {
        @Override
        public UUID read(ByteBuf buf) {
            return new java.util.UUID(buf.readLong(), buf.readLong());
        }

        @Override
        public void write(UUID var, ByteBuf buf) {
            buf.writeLong(var.getMostSignificantBits());
            buf.writeLong(var.getLeastSignificantBits());
        }
    };

    public static final Coder<Long> LONG = new Coder<Long>() {
        @Override
        public Long read(ByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public void write(Long var, ByteBuf buf) {
            buf.writeLong(var);
        }
    };

    public static final Coder<Boolean> BOOLEAN = new Coder<Boolean>() {
        @Override
        public Boolean read(ByteBuf buf) {
            return buf.readBoolean();
        }

        @Override
        public void write(Boolean var, ByteBuf buf) {
            buf.writeBoolean(var);
        }
    };

    abstract public T read(ByteBuf buf);
    abstract public void write(T var, ByteBuf buf);
}

