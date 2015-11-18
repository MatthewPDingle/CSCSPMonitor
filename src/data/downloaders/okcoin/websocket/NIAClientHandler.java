package data.downloaders.okcoin.websocket;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import constants.Constants.BAR_SIZE;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.OKCoinDownloader;
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
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
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
	}

	public Timer getTimer() {
		return timer;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		System.out.println("NIAClientHandler handlerAdded(...) " + ctx.toString());
		if (ctx != null) {
			handshakeFuture = ctx.newPromise();
		}
	}

	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
//		System.out.println("NIAClientHandler acceptInboundMessage(...) " + msg.toString());
		return super.acceptInboundMessage(msg);
	}

	@Override
	public void channelRead(ChannelHandlerContext arg0, Object arg1) throws Exception {
//		System.out.println("NIAClientHandler channelRead(...) " + arg1.toString());
		super.channelRead(arg0, arg1);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("NIAClientHandler channelRegistered(...) " + ctx.toString());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("NIAClientHandler channelUnregistered(...) " + ctx.toString());
		super.channelUnregistered(ctx);
	}

	@Override
	public synchronized void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println("NIAClientHandler handlerRemoved(...) " + ctx.toString());
		super.handlerRemoved(ctx);
		timer.cancel();
		if (NIAStatusSingleton.getInstance().isKeepAlive()) {
			System.err.println("NIAClientHandler handlerRemoved(...) - Something is wrong, but isKeepAlive = true, so will attempt reconnect via NIAConnectionMonitoringThread...");
			NIAStatusSingleton.getInstance().setNiaClientHandlerConnected(false);
			NIAStatusSingleton.getInstance().setStartup(false);
			NIAStatusSingleton.getInstance().setOkToWaitForConnection(false);
			System.err.println("NIAClientHandler handlerRemoved(...) END");
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("NIAClientHandler channelActive(...) " + ctx.toString());
		if (ctx != null) {
			handshaker.handshake(ctx.channel());
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("NIAClientHandler channelInactive(...) " + ctx.toString());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
//			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			System.err.println("NIAClientHandler has not detected any activity on the channel and has disconnected.  Will attempt to reoconnect...");
			NIAStatusSingleton.getInstance().reinitClient();
		} 
		else if (evt instanceof ReadTimeoutException) {
//			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			System.err.println("NIAClientHandler has detected a ReadTimeoutException.  Will attempt to reoconnect...");
			NIAStatusSingleton.getInstance().reinitClient();
		}
		else if (evt instanceof WriteTimeoutException) {
//			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			System.err.println("NIAClientHandler has detected a WriteTimeoutException.  Will attempt to reoconnect...");
			NIAStatusSingleton.getInstance().reinitClient();
		}
		else if (evt instanceof SslHandshakeCompletionEvent) {
			SslHandshakeCompletionEvent sslEvent =  (SslHandshakeCompletionEvent)evt;
			if (!sslEvent.isSuccess()) {
				System.err.println("NIAClientHandler userEventTriggered(...) " + evt.toString());
//				System.err.println("Going to attempt reconnect...");
				ctx.deregister();
				NIAStatusSingleton.getInstance().setOkToWaitForConnection(false);
//				NIAStatusSingleton.getInstance().reinitClient();
			}
			else {
				System.out.println("NIAClientHandler userEventTriggered(...) SSL Handshake Successful!");
			}
		}
		else {
			System.out.println("NIAClientHandler userEventTriggered(...) " + evt.toString());
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			Channel ch = ctx.channel();
			if (!handshaker.isHandshakeComplete()) {
				handshaker.finishHandshake(ch, (FullHttpResponse) msg);
				System.out.println("NIAClientHandler channelRead0(...) " + msg.toString());
				handshakeFuture.setSuccess();
				
				// At this point we're connected so record this and start the timer.
				NIAStatusSingleton.getInstance().setNiaClientHandlerConnected(true);
				NIAStatusSingleton.getInstance().cleanHangingRequestsFromDB();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						System.out.println("{'event':'ping'}");
						NIAStatusSingleton.getInstance().getNiaClient().sendPing();
						
						// If we've been inactive for longer than USE_REST_IF_WEBSOCKET_DELAYED_MS, then use the REST API to get a bar
//						long lastActivity = NIAStatusSingleton.getInstance().getLastActivityTime().getTimeInMillis();
//						long now = Calendar.getInstance().getTimeInMillis();
//						if (now - lastActivity > NIAStatusSingleton.USE_REST_IF_WEBSOCKET_DELAYED_MS) {
//							System.out.println("NIAClientHandler(...) is delayed.  Using REST API as backup to download bar data");
//							NIAStatusSingleton.getInstance().addLatestBars(OKCoinDownloader.downloadLatestBar(OKCoinConstants.SYMBOL_BTCCNY, BAR_SIZE.BAR_1M));
//						}
					}
				}, 5000, 5000);
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
			System.err.println("NIAClientHandler exceptionCaught(...) " + cause.toString());
			System.err.println("Going to attempt reconnect via NIAConnectionMonitoringThread...");
			if (!handshakeFuture.isDone()) {
				handshakeFuture.setFailure(cause);
			}
			ctx.close();
			NIAStatusSingleton.getInstance().setOkToWaitForConnection(false);
			NIAStatusSingleton.getInstance().setNiaClientHandlerConnected(false);
			NIAStatusSingleton.getInstance().setStartup(false);
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