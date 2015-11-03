package data.downloaders.okcoin.websocket;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

@Sharable
public class NIAClientHandler extends SimpleChannelInboundHandler<Object> {

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private NIAListener listener;
	private Timer timer;

	public NIAClientHandler(WebSocketClientHandshaker handshaker, NIAListener listener) {
		this.handshaker = handshaker;
		this.listener = listener;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("{'event':'ping'}");
				NIAStatusSingleton.getInstance().getNiaClient().sendPing();
			}
		}, 15000, 5000);
	}

	public Timer getTimer() {
		return timer;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		if (ctx != null) {
			handshakeFuture = ctx.newPromise();
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		if (ctx != null) {
			handshaker.handshake(ctx.channel());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.err.println("NIAClientHandler channelInactive!");
		// Reconnect
		NIAStatusSingleton.getInstance().reinitClient();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			Channel ch = ctx.channel();
			if (!handshaker.isHandshakeComplete()) {
				handshaker.finishHandshake(ch, (FullHttpResponse) msg);
				System.out.println("NIAClientHandler connected!");
				NIAStatusSingleton.getInstance().setNiaClientHandlerConnected(true);
				handshakeFuture.setSuccess();
				return;
			}
	
			if (msg instanceof FullHttpResponse) {
				FullHttpResponse response = (FullHttpResponse) msg;
				throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
			}
	
			WebSocketFrame frame = (WebSocketFrame) msg;
			if (frame instanceof TextWebSocketFrame) {
				TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
				listener.onReceive(textFrame.text());
			} 
			else if (frame instanceof BinaryWebSocketFrame) {
				BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
				listener.onReceive(decodeByteBuff(binaryFrame.content()));
			} 
			else if (frame instanceof PongWebSocketFrame) {
				System.out.println("WebSocket Client received pong");
			} 
			else if (frame instanceof CloseWebSocketFrame) {
				System.out.println("WebSocket Client received closing");
				ch.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		try {
			cause.printStackTrace();
			if (!handshakeFuture.isDone()) {
				handshakeFuture.setFailure(cause);
			}
			ctx.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String decodeByteBuff(ByteBuf buf) throws IOException, DataFormatException {
		StringBuilder sb = new StringBuilder();
		try {
			byte[] temp = new byte[buf.readableBytes()];
			ByteBufInputStream bis = new ByteBufInputStream(buf);
			bis.read(temp);
			bis.close();
			Inflater decompresser = new Inflater(true);
			decompresser.setInput(temp, 0, temp.length);
			byte[] result = new byte[1024];
			while (!decompresser.finished()) {
				int resultLength = decompresser.inflate(result);
				sb.append(new String(result, 0, resultLength, "UTF-8"));
			}
			decompresser.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}