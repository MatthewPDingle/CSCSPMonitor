package data.downloaders.okcoin.websocket;

import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.util.CharsetUtil;

public class NIAIdleStateHandlerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new IdleStateHandler(30, 30, 30, TimeUnit.SECONDS));
		pipeline.addLast(new HeartbeatHandler());
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
	}
}