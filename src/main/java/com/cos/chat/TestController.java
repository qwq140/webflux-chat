package com.cos.chat;


import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;



@CrossOrigin
@RestController
public class TestController {
	// 프로세서 사용 : 중간에 데이터가 들어와도 진행
	
	Sinks.Many<String> sink;
	
	
	// multicast() 새로 들어온 데이터만 응답받음 hot 시퀀스 = 스트림
	// replay() 기존 데이터 + 새로운 데이터 응답 cold 시퀀스
	
	
	public TestController() {
		super();
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}

	
	@GetMapping("/send")
	public void send(Chat chat) {
		System.out.println(chat.getUsername()+":"+chat.getContent());
		sink.tryEmitNext(chat.getUsername()+":"+chat.getContent());
	}
	
	
	// data : 실제값 \n\n\
	@GetMapping(value = "/sse")
	public Flux<ServerSentEvent<String>> sse() { // ServcerSendEvent의 ContentType은 text event stream
		return sink.asFlux().map(e -> ServerSentEvent.builder(e).build()).doOnCancel(()->{
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast(); // response를 끊음.
		}); // 구독
	}
}

