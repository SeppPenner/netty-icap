package ch.mimo.netty.handler.codec.icap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

public abstract class IcapMessageDecoder extends ReplayingDecoder<IcapMessageDecoder.State> {

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;
	private IcapMessage message;
	
	protected static enum State {
		SKIP_CONTROL_CHARS,
		READ_ICAP_INITIAL,
		READ_ICAP_HEADER
	}
	
    /**
     * Creates a new instance with the default
     * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}.
     */
    protected IcapMessageDecoder() {
        this(4096, 8192, 8192);
    }
    
    /**
     * Creates a new instance with the specified parameters.
     */
    protected IcapMessageDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(State.SKIP_CONTROL_CHARS,true);
        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException("maxInitialLineLength must be a positive integer: " + maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException("maxHeaderSize must be a positive integer: " + maxHeaderSize);
        }
        if (maxChunkSize < 0) {
            throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize);
        }
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
    }

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state) throws Exception {
		switch (state) {
		case SKIP_CONTROL_CHARS: {
            try {
                IcapDecoderUtil.skipControlCharacters(buffer);
                checkpoint(State.READ_ICAP_INITIAL);
            } finally {
                checkpoint();
            }
		}
		case READ_ICAP_INITIAL: {
			String[] initialLine = IcapDecoderUtil.splitInitialLine(IcapDecoderUtil.readLine(buffer,maxInitialLineLength));
            if (initialLine.length < 3) {
                // Invalid initial line - ignore.
                checkpoint(State.SKIP_CONTROL_CHARS);
                return null;
            }
            
            message = createMessage(initialLine);
            checkpoint(State.READ_ICAP_HEADER);
		}
		case READ_ICAP_HEADER: {
			
		}
		default:
			break;
		}
		
		
		// TODO parse and store icap headers with the HttpHeaders class.
		// The IcapHeaders class will provide the missing headers "Preview" and "Encapsulation"
		// plus the parsing functionality for the Encapsulation header!
		return message;
	}
	
	public abstract boolean isDecodingRequest();
	
	protected abstract IcapMessage createMessage(String[] initialLine) throws Exception;
}
