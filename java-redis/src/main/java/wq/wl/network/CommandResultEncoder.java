package wq.wl.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import wq.wl.message.Message;
import wq.wl.util.BytebufUtils;

import java.util.List;

/**
 * Description:
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
public class CommandResultEncoder extends MessageToMessageEncoder<Message.Result> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message.Result result, List<Object> out) throws Exception {
        ByteBuf byteBuf = BytebufUtils.serialize(result);
        out.add(byteBuf);
    }
}
