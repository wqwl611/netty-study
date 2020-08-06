package wq.wl.util;

import com.google.protobuf.GeneratedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
public class BytebufUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BytebufUtils.class);

    public static ByteBuf serialize(GeneratedMessage message) {
        byte[] bytes = message.toByteArray();
        ByteBuf lenByteBuf = Unpooled.buffer(4);
        lenByteBuf.writeInt(bytes.length);
        ByteBuf seriRes = Unpooled.wrappedBuffer(lenByteBuf.array(), bytes);
        LOG.debug("send data: [{}]", message.toString());
        return seriRes;
    }
}
