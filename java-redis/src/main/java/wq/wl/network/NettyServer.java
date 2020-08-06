package wq.wl.network;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wq.wl.LifeCycle;
import wq.wl.network.CommandResultEncoder;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Description:
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
@Component
public class NettyServer implements LifeCycle {


    private static final Logger LOG = LoggerFactory.getLogger(wq.wl.network.ServerHandler.class);
    @Autowired
    private wq.wl.network.ServerHandler serverHandler;
    private Map<String, String> serverConfig;

    public NettyServer() {
    }

    public NettyServer(Map<String, String> serverConfig) {
        this.serverConfig = serverConfig;
    }

    @PostConstruct
    public void init() {
        try {
            serverConfig = Maps.newHashMap();
            start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void start() throws Throwable {
        int port = Integer.parseInt(serverConfig.getOrDefault("server.port", "9876"));
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(10);
        serverBootstrap.group(bossGroup, workerGroup);


        try {
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();
                            pipeline.addLast(new CommandResultEncoder());
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(100000, 0, 4, 0, 4));
                            pipeline.addLast(serverHandler);
                        }
                    });
            serverBootstrap.bind(port).sync();
            LOG.info("Server start...");
        } finally {
            LOG.error("kdi");
        }

    }

    @Override
    public void stop() throws Throwable {

    }


}
