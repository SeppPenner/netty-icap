package ch.mimo.netty.handler.codec.icap;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Before;
import org.junit.Test;

public class IcapChunkAggregatorTest extends AbstractIcapTest {

	private DecoderEmbedder<Object> embedder;
	
	@Before
	public void setUp() throws UnsupportedEncodingException {
		embedder = new DecoderEmbedder<Object>(new IcapChunkAggregator());
	}
	
	@Test
	public void offerUnknownObject() {
		embedder.offer("The ultimate answer is 42");
	}
	
	@Test
	public void aggregatorREQMODWithGetRequestWithoutChunks() throws UnsupportedEncodingException {
		embedder.offer(DataMockery.createREQMODWithGetRequestNoBodyAndEncapsulationHeaderIcapMessage());
		IcapMessage request = (IcapMessage)embedder.poll();
		DataMockery.assertCreateREQMODWithGetRequestNoBody(request);
	}
	
	@Test
	public void aggregatorREQMODWithGetRequestWithoutChunksAndNullBodySet() throws UnsupportedEncodingException {
		embedder.offer(DataMockery.createREQMODWithGetRequestNoBodyAndEncapsulationHeaderAndNullBodySetIcapMessage());
		IcapMessage request = (IcapMessage)embedder.poll();
		DataMockery.assertCreateREQMODWithGetRequestNoBody(request);
	}
	
	@Test
	public void aggregatorChunkOnlyTest() throws UnsupportedEncodingException {
		embedder.offer(DataMockery.createRESPMODWithGetRequestAndPreviewIcapChunk());
		IcapChunk chunk = (IcapChunk)embedder.poll();
		assertNotNull("no chunk received from pipeline",chunk);
		DataMockery.assertCreateRESPMODWithGetRequestAndPreviewChunk(chunk);
	}
	
	@Test
	public void aggregatorMessageWithoutBodyFollowedByBodyChunk() {
		embedder.offer(DataMockery.createREQMODWithGetRequestNoBodyAndEncapsulationHeaderIcapMessage());
		IcapMessage request = (IcapMessage)embedder.poll();
		DataMockery.assertCreateREQMODWithGetRequestNoBody(request);
		embedder.offer(DataMockery.createRESPMODWithGetRequestAndPreviewIcapChunk());
		IcapChunk chunk = (IcapChunk)embedder.poll();
		assertNotNull("no chunk received from pipeline",chunk);
		DataMockery.assertCreateRESPMODWithGetRequestAndPreviewChunk(chunk);
	}
	
	@Test
	public void aggregateREQMODRequestWithChunks() throws UnsupportedEncodingException {
		embedder.offer(DataMockery.createREQMODWithTwoChunkBodyAndEncapsulationHeaderIcapMessage());
		embedder.offer(DataMockery.createREQMODWithTwoChunkBodyIcapChunkOne());
		embedder.offer(DataMockery.createREQMODWithTwoChunkBodyIcapChunkTwo());
		embedder.offer(DataMockery.createREQMODWithTwoChunkBodyIcapChunkThree());
		IcapMessage request = (IcapMessage)embedder.poll();
		DataMockery.assertCreateREQMODWithTwoChunkBody(request);
		String body = request.getHttpRequest().getContent().toString(IcapCodecUtil.ASCII_CHARSET);
		StringBuilder builder = new StringBuilder();
		builder.append("This is data that was returned by an origin server.");
		builder.append("And this the second chunk which contains more information.");
		assertEquals("The body content was wrong",builder.toString(),body);
		Object object = embedder.peek();
		assertNull("still something there",object);
	}
	
	@Test
	public void aggregateRESPMODRequestWithPreviewChunks() throws UnsupportedEncodingException {
		embedder.offer(DataMockery.createRESPMODWithGetRequestAndPreviewIncludingEncapsulationHeaderIcapRequest());
		embedder.offer(DataMockery.createRESPMODWithGetRequestAndPreviewIcapChunk());
		embedder.offer(DataMockery.crateRESPMODWithGetRequestAndPreviewLastIcapChunk());
		IcapMessage request = (IcapMessage)embedder.poll();
		DataMockery.assertCreateRESPMODWithGetRequestAndPreview(request);
		String body = request.getHttpResponse().getContent().toString(IcapCodecUtil.ASCII_CHARSET);
		StringBuilder builder = new StringBuilder();
		builder.append("This is data that was returned by an origin server.");
		assertEquals("The body content was wrong",builder.toString(),body);
		Object object = embedder.peek();
		assertNull("still something there",object);
	}
}
