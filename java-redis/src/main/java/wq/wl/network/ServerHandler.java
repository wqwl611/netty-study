package wq.wl.network;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import wq.wl.kvdb.KvDb;
import wq.wl.message.Message;

import static wq.wl.message.Message.ResCode.*;

/**
 * Description:
 * 线程共享的对象，注意线程安全
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
@Component
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);
    @Autowired
    private KvDb kvDb;
    private ThreadLocal<Message.Result.Builder> failResultBuilder =
            ThreadLocal.withInitial(() -> Message.Result.newBuilder().setResCode(RES_FAIL));

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOG.info("connection from: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("exception: ", cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOG.info("close one connection.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("close one connection.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Message.Command command = parseCommand(msg);
        LOG.debug("receive command: [{}]", command.toString());
        Message.Result result = processCommand(command);
        ctx.writeAndFlush(result);
    }

    private Message.Result processCommand(Message.Command command) {
        switch (command.getCommandType()) {
            case GET:
                return processGet(command);
            case DEL:
                return processDel(command);
            case SET:
                return processSet(command);
            default:
                return failResultBuilder.get().setCommandId(command.getCommandId())
                        .setResMsg("unknown command type.").build();
        }
    }

    private Message.Result processSet(Message.Command command) {
        try {
            kvDb.set(command.getKey(), command.getValue());
        } catch (Exception e) {
            return getFailResult(command, e);
        }
        Message.Result result = Message.Result.newBuilder()
                .setResCode(RES_SUCCESS)
                .setCommandId(command.getCommandId())
                .build();
        return result;
    }

    private Message.Result getFailResult(Message.Command command, Exception e) {
        return failResultBuilder.get().setResMsg(e.getMessage()).setCommandId(command.getCommandId()).build();
    }

    private Message.Result processDel(Message.Command command) {
        try {
            kvDb.del(command.getKey());
        } catch (Exception e) {
            return getFailResult(command, e);
        }
        Message.Result result = Message.Result.newBuilder()
                .setCommandId(command.getCommandId())
                .setResCode(RES_SUCCESS)
                .build();
        return result;
    }

    private Message.Result processGet(Message.Command command) {
        String result = null;
        try {
            result = kvDb.get(command.getKey());
        } catch (Exception e) {
            return getFailResult(command, e);
        }
        // 如果不存在值，返回空字符串
        if (StringUtils.isEmpty(result)) {
            result = "";
        }
        return Message.Result.newBuilder().setCommandId(command.getCommandId())
                .setResCode(RES_SUCCESS).setResult(result)
                .build();
    }

    private Message.Command parseCommand(ByteBuf msg) throws InvalidProtocolBufferException {
        try {
            return Message.Command.parseFrom(getBytes(msg));
        } catch (InvalidProtocolBufferException e) {
            // 理论上不应该出现
            LOG.error("parse command error: ", e);
            throw e;
        }
    }

    private byte[] getBytes(ByteBuf msg) {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        return bytes;
    }

}
