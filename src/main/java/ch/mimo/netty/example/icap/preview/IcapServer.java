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
package ch.mimo.netty.example.icap.preview;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * An ICAP Server that prints the preview request asks for the rest via
 * 100 contine prints that and responds with 204 Non Content.
 * 
 * 
 * @author Michael Mimo Moratti (mimo@mimo.ch)
 *
 */
public class IcapServer {

	public static void main(String[] args) {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .localAddress(8099)
            .childHandler(new IcapServerChannelPipeline())
            .bind();
	}
}
