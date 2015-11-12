package data.downloaders.okcoin.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import data.downloaders.okcoin.OKCoinConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import utils.MD5Util;

@Sharable
public class NIAClient {

	private Channel channel = null;
	private ChannelFuture channelFuture = null;
	private EventLoopGroup nioEventLoopGroup = null;
//	private EventExecutorGroup eeg = null;
	private NIAClientHandler handler = null;
	private Set<String> subscriptionChannels = new HashSet<String>();
	
	public NIAClient() {
//		eeg = new DefaultEventExecutorGroup(4);
	}
	
	public boolean connect() throws Exception  {
		nioEventLoopGroup = new NioEventLoopGroup();
		try {
			URI uri = new URI(OKCoinConstants.WEBSOCKET_URL_CHINA);
			// Seems like I need to use this SSL thing to connect.  Otherwise I get an "Invalid handshake response getStatus: 400 Bad Request"
			SslContext sslCtx = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			handler = new NIAClientHandler(WebSocketClientHandshakerFactory
					.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), Integer.MAX_VALUE), new NIAListener());

			Bootstrap bootstrap = new Bootstrap(); 
			bootstrap.group(nioEventLoopGroup)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.channel(NioSocketChannel.class)
				.remoteAddress(new InetSocketAddress(uri.getHost(), uri.getPort()))
				//.handler(new NIAIdleStateHandlerInitializer(sslCtx, handler));// {
				.handler(new ChannelInitializer<SocketChannel>() {
					public void initChannel(SocketChannel ch) throws Exception {
						try {
							if (sslCtx != null) {
								ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
							}
							ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
						}
						catch (Exception e) {
							System.err.println("NIAClient ChannelInitializer initChannel(...) threw an error");
							e.printStackTrace();
							
							nioEventLoopGroup.shutdownGracefully().sync();
							initChannel(ch);
						}
					}
				});
			channelFuture = bootstrap.connect();
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					if (!channelFuture.isSuccess()) {
						System.err.println("NIAClient channelFuture detected unsuccessful connect.  Going to throw exception.");
						throw new Exception("NIAClient connect(...) did not complete successfully.");
					}
					else {
						System.out.println("NIAClient channelFuture detected operationComplete(...) " + arg0.toString());
					}
				}
			});
			channelFuture.sync();
			channel = channelFuture.channel();
			return true;
		}
		catch (Exception e) {
			System.err.println("NIAClient connect(...) in exception block - " + e.getMessage());
			nioEventLoopGroup.shutdownGracefully().sync();
			return false;
		}
	}
	
	public void disconnect() {
		try {
			if (nioEventLoopGroup != null) {
				nioEventLoopGroup.shutdownGracefully();
			}
			if (handler != null) {
				handler.getTimer().cancel();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cancleFutureOrder(String symbol, long orderId, String contractType) {
		Map<String, String> preMap = new HashMap<String, String>();
		preMap.put("api_key", OKCoinConstants.APIKEY);
		preMap.put("symbol", symbol);
		preMap.put("order_id", String.valueOf(orderId));
		preMap.put("contract_type", contractType);
		String preStr = MD5Util.createLinkString(preMap);
		preStr = preStr + "&secret_key=" + OKCoinConstants.SECRETKEY;
		String signStr = MD5Util.getMD5String(preStr);
		preMap.put("sign", signStr);
		String params = MD5Util.getParams(preMap);
		StringBuilder tradeStr = new StringBuilder(
				"{'event': 'addChannel','channel': 'ok_futuresusd_cancel_order','parameters': ").append(params).append("}");
		sendMessage(tradeStr.toString());
	}

	public void cancelOrder(String symbol, Long orderId) {
		System.out.println("cancelOrder: " + orderId);
		Map<String, String> preMap = new HashMap<String, String>();
		preMap.put("api_key", OKCoinConstants.APIKEY);
		preMap.put("symbol", symbol);
		preMap.put("order_id", orderId.toString());
		String preStr = MD5Util.createLinkString(preMap);
		StringBuilder preBuilder = new StringBuilder(preStr);
		preBuilder.append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preBuilder.toString());
		preMap.put("sign", signStr);
		String params = MD5Util.getParams(preMap);
		String channel = "ok_spotcny_cancel_order";
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel', 'channel':'" + channel + "', 'parameters':").append(params).append("}");
		sendMessage(tradeStr.toString());
	}
	
	public void futureRealtrades() {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(OKCoinConstants.APIKEY).append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preStr.toString());
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'ok_usd_future_realtrades','parameters':{'api_key':'").append(OKCoinConstants.APIKEY).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		sendMessage(tradeStr.toString());
	}

	public void futureTrade(String symbol, String contractType, double price,
			int amount, int type, double matchPrice, int leverRate) {
		Map<String, String> preMap = new HashMap<String, String>();

		preMap.put("api_key", OKCoinConstants.APIKEY);
		preMap.put("symbol", symbol);
		preMap.put("contract_type", contractType);
		preMap.put("price", String.valueOf(price));
		preMap.put("amount", String.valueOf(amount));
		preMap.put("type", String.valueOf(type));
		preMap.put("match_price", String.valueOf(matchPrice));
		preMap.put("lever_rate", String.valueOf(leverRate));
		String preStr = MD5Util.createLinkString(preMap);
		preStr = preStr + "&secret_key=" + OKCoinConstants.SECRETKEY;
		String signStr = MD5Util.getMD5String(preStr);

		preMap.put("sign", signStr);
		String params = MD5Util.getParams(preMap);

		StringBuilder tradeStr = new StringBuilder("{'event': 'addChannel','channel':'ok_futuresusd_trade','parameters':").append(params).append("}");
		sendMessage(tradeStr.toString());
	}

	public void getUserInfo() {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(OKCoinConstants.APIKEY).append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preStr.toString());
		String channel = "ok_spotcny_userinfo";
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'").append(channel).append("','parameters':{'api_key':'").append(OKCoinConstants.APIKEY).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		sendMessage(tradeStr.toString());
	}
	
	public void getOrderInfo(String okCoinSymbol, long orderID) {
		Map<String, String> signPreMap = new HashMap<String, String>();
		signPreMap.put("api_key", OKCoinConstants.APIKEY);
		signPreMap.put("symbol", okCoinSymbol);
		signPreMap.put("order_id", new Long(orderID).toString());
		
		String preStr = MD5Util.createLinkString(signPreMap);
		StringBuilder preBuilder = new StringBuilder(preStr);
		preBuilder.append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preBuilder.toString());
		String channel = "ok_spotcny_order_info";
		StringBuilder message = new StringBuilder("{'event':'addChannel','channel':'" + channel + "','parameters':");
		signPreMap.put("sign", signStr);
		String params = MD5Util.getParams(signPreMap);
		message.append(params).append("}");
		sendMessage(message.toString());
	}

	public void getRealTrades() {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(OKCoinConstants.APIKEY).append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preStr.toString());
		String channel = "ok_cny_realtrades";
		StringBuilder tradeStr = new StringBuilder(
				"{'event':'addChannel','channel':'" + channel + "','parameters':{'api_key':'").append(OKCoinConstants.APIKEY).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		sendMessage(tradeStr.toString());
	}

	public void spotTrade(String symbol, String price, String amount, String type) {
		System.out.println("SpotTrade: " + type + " " + price + ", " + amount);
		Map<String, String> signPreMap = new HashMap<String, String>();
		signPreMap.put("api_key", OKCoinConstants.APIKEY);
		signPreMap.put("symbol", symbol);
		if (price != null) {
			signPreMap.put("price", price);
		}
		if (amount != null) {
			signPreMap.put("amount", amount);
		}
		signPreMap.put("type", type);
		String preStr = MD5Util.createLinkString(signPreMap);
		StringBuilder preBuilder = new StringBuilder(preStr);
		preBuilder.append("&secret_key=").append(OKCoinConstants.SECRETKEY);
		String signStr = MD5Util.getMD5String(preBuilder.toString());
		String channel = "ok_spotcny_trade";
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'" + channel + "','parameters':");
		signPreMap.put("sign", signStr);
		String params = MD5Util.getParams(signPreMap);
		tradeStr.append(params).append("}");
		sendMessage(tradeStr.toString());
	}
	
	/**
	 * Wrapping this in a check for isActive and isOpen seems to cause a lot of problems.
	 * @param message
	 */
	public void sendMessage(String message) {
		if (channel != null) {
//			group.execute(new Runnable() {
//				@Override
//				public void run() {
					channel.writeAndFlush(new TextWebSocketFrame(message));
//				}
//			});
			
		}
	}
	
	public void addChannel(String channel) {
		if (channel == null) {
			return;
		}
		String dataMsg = "{'event':'addChannel','channel':'" + channel + "','binary':'true'}";
		System.out.println(dataMsg);
		sendMessage(dataMsg);
		subscriptionChannels.add(channel);
	}
	
	public void removeChannel(String channel) {
		if (channel == null) {
			return;
		}
		String dataMsg = "{'event':'removeChannel','channel':'" + channel + "'}";
		sendMessage(dataMsg);
		subscriptionChannels.remove(channel);
	}

	public void removeAllChannels() {
		for (String channel : subscriptionChannels) {
			String dataMsg = "{'event':'removeChannel','channel':'" + channel + "'}";
			sendMessage(dataMsg);
		}
		subscriptionChannels.clear();
	}
	
	public void sendPing() {
		sendMessage("{'event':'ping'}");
	}

	public NIAClientHandler getHandler() {
		return handler;
	}
}