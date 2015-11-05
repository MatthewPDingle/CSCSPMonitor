package data.downloaders.okcoin.websocket;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import data.downloaders.okcoin.OKCoinConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.CharsetUtil;

public class NIAIdleStateHandlerInitializer extends ChannelInitializer<SocketChannel> {

	private SslContext sslCtx;
	private NIAClientHandler handler;
	
	public NIAIdleStateHandlerInitializer(SslContext sslCtx, NIAClientHandler handler) {
		this.sslCtx = sslCtx;
		this.handler = handler;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		URI uri = new URI(OKCoinConstants.WEBSOCKET_URL_CHINA);

		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new IdleStateHandler(0, 0, 15, TimeUnit.SECONDS));
//		pipeline.addLast(new ReadTimeoutHandler(10));
//		pipeline.addLast(new WriteTimeoutHandler(10));
		pipeline.addLast(new HeartbeatHandler());
		if (sslCtx != null) {
			ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
		}
		ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
	}

	public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {
		private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT\r\n", CharsetUtil.UTF_8));

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (evt instanceof IdleStateEvent) {
				ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				System.err.println("NIAIdleStateHandlerInitializer has not detected any activity on the channel and has disconnected.  Will attempt to reoconnect...");
				NIAStatusSingleton.getInstance().reinitClient();
			} 
			else if (evt instanceof ReadTimeoutException) {
				ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				System.err.println("NIAIdleStateHandlerInitializer has detected a ReadTimeoutException.  Will attempt to reoconnect...");
				NIAStatusSingleton.getInstance().reinitClient();
			}
			else if (evt instanceof WriteTimeoutException) {
				ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				System.err.println("NIAIdleStateHandlerInitializer has detected a WriteTimeoutException.  Will attempt to reoconnect...");
				NIAStatusSingleton.getInstance().reinitClient();
			}
			else {
				System.err.println("NIAIdleStateHandlerInitializer has detected something else.  ");
				System.err.println(evt.toString());
				super.userEventTriggered(ctx, evt);
			}
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			System.err.println("NIAIdleStateHandlerInitializer has detected channelInactive. Will attempt to reconnect...");
			super.channelInactive(ctx);
			NIAStatusSingleton.getInstance().reinitClient();
		}
	}
}