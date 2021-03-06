/*******************************************************************************
 * Copyright 2012 Michael Mimo Moratti
 * Modifications Copyright (c) 2018 eBlocker GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package ch.mimo.netty.handler.codec.icap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * This is the main Icap message implementation where
 * all common @see {@link DefaultIcapRequest} and @see {@link DefaultIcapResponse} member are present.
 *
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 */
public abstract class AbstractIcapMessage implements IcapMessage {

	private volatile int refCount = 1;

	private IcapHeaders icapHeader;
	private IcapVersion version;
	private Encapsulated encapsulated;
	
	private FullHttpRequest httpRequest;
	private FullHttpResponse httpResponse;
	
	private IcapMessageElementEnum body;
	
	public AbstractIcapMessage(IcapVersion version) {
		this.version = version;
		icapHeader = new IcapHeaders();
	}

	@Override
	public String getHeader(String name) {
		return icapHeader.getHeader(name);
	}

	@Override
	public Set<String> getHeaders(String name) {
		return icapHeader.getHeaders(name);
	}

	@Override
	public Set<Entry<String, String>> getHeaders() {
		return icapHeader.getHeaders();
	}

	@Override
	public boolean containsHeader(String name) {
		return icapHeader.containsHeader(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return icapHeader.getHeaderNames();
	}

	@Override
	public IcapMessage addHeader(String name, Object value) {
		icapHeader.addHeader(name,value);
		return this;
	}

	@Override
	public IcapMessage setHeader(String name, Object value) {
		icapHeader.setHeader(name,value);
		return this;
	}

	@Override
	public IcapMessage setHeader(String name, Iterable<?> values) {
		icapHeader.setHeader(name,values);
		return this;
	}

	@Override
	public IcapMessage removeHeader(String name) {
		icapHeader.removeHeader(name);
		return this;
	}
	
	@Override
	public int getPreviewAmount() {
		return icapHeader.getPreviewHeaderValue();
	}

	@Override
	public IcapMessage clearHeaders() {
		icapHeader.clearHeaders();
		return this;
	}

	@Override
	public IcapVersion getProtocolVersion() {
		return version;
	}

	@Override
	public IcapMessage setProtocolVersion(IcapVersion version) {
		this.version = version;
		return this;
	}

	@Override
	public boolean containsHttpRequest() {
		return httpRequest != null;
	}

	@Override
	public FullHttpRequest getHttpRequest() {
		return httpRequest;
	}
	
	@Override
	public IcapMessage setHttpRequest(FullHttpRequest httpRequest) {
		this.httpRequest = httpRequest;
		return this;
	}

	@Override
	public boolean containsHttpResponse() {
		return httpResponse != null;
	}

	@Override
	public FullHttpResponse getHttpResponse() {
		return httpResponse;
	}
	
	public IcapMessage setHttpResponse(FullHttpResponse response) {
		this.httpResponse = response;
		return this;
	}

	@Override
	public IcapMessage setEncapsulatedHeader(Encapsulated encapsulated) {
		this.encapsulated = encapsulated;
		return this;
	}

	@Override
	public Encapsulated getEncapsulatedHeader() {
		return encapsulated;
	}
	
	@Override
	public boolean isPreviewMessage() {
		return icapHeader.getPreviewHeaderValue() >= 0;
	}
	
	public IcapMessage setBody(IcapMessageElementEnum body) {
		this.body = body;
		return this;
	}

	public IcapMessageElementEnum getBodyType() {
		if(encapsulated != null) {
			return encapsulated.containsBodyEntry();
		} else {
			return body;
		}
	}
	
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append("(version: ");
        buf.append(getProtocolVersion().getText());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);

        if(httpRequest != null) {
        	buf.append("--- encapsulated HTTP Request ---").append(StringUtil.NEWLINE);
        	buf.append(httpRequest.toString());
        	if(httpRequest.content() != null && httpRequest.content().readableBytes() > 0) {
        		buf.append(StringUtil.NEWLINE).append("--> HTTP Request contains [" + httpRequest.content().readableBytes() + "] bytes of data").append(StringUtil.NEWLINE);
        	}
        }
        
        if(httpResponse != null) {
        	buf.append("--- encapsulated HTTP Response ---").append(StringUtil.NEWLINE);
        	buf.append(httpResponse.toString());
        	if(httpResponse.content() != null && httpResponse.content().readableBytes() > 0) {
        		buf.append(StringUtil.NEWLINE).append("--> HTTP Response contains [" + httpResponse.content().readableBytes() + "] bytes of data");
        	}
        }
        
        if(isPreviewMessage()) {
            buf.append(StringUtil.NEWLINE);
        	buf.append("--- Preview ---").append(StringUtil.NEWLINE);
        	buf.append("Preview size: " + icapHeader.getPreviewHeaderValue());
        }
        
        return buf.toString();
    }
    
    private void appendHeaders(StringBuilder buf) {
        for (Map.Entry<String, String> e: getHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

	@Override
	public int refCnt() {
		return refCount;
	}

	@Override
	public boolean release() {
		if (httpRequest != null) {
			httpRequest.release();
		}
		if (httpResponse != null) {
			httpResponse.release();
		}
		return --refCount == 0;
	}

	@Override
	public boolean release(int decrement) {
		if (httpRequest != null) {
			httpRequest.release(decrement);
		}
		if (httpResponse != null) {
			httpResponse.release(decrement);
		}
		refCount -= decrement;
		return refCount == 0;
	}

	@Override
	public AbstractIcapMessage retain() {
		++refCount;
		if (httpRequest != null) {
			httpRequest.retain();
		}
		if (httpResponse != null) {
			httpResponse.retain();
		}
		return this;
	}

	@Override
	public AbstractIcapMessage retain(int increment) {
		refCount += increment;
		if (httpRequest != null) {
			httpRequest.retain(increment);
		}
		if (httpResponse != null) {
			httpResponse.retain(increment);
		}
		return this;
	}

	@Override
	public AbstractIcapMessage touch() {
		if (httpRequest != null) {
			httpRequest.touch();
		}
		if (httpResponse != null) {
			httpResponse.touch();
		}
		return this;
	}

	@Override
	public AbstractIcapMessage touch(Object hint) {
		if (httpRequest != null) {
			httpRequest.touch(hint);
		}
		if (httpResponse != null) {
			httpResponse.touch(hint);
		}
		return this;
	}
}
