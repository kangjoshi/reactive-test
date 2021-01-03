package com.example.reactivetest;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

import java.time.Duration;

public class StepVerifierTests {

    @Test
    public void create_test_1() {
        StepVerifier
                .create(Flux.just("foo", "bar"))    // 생성
                .expectSubscription()               // 구독 이벤트인지
                .expectNext("foo")                  // 첫번째 원소 값이 foo인지 확인
                .expectNext("bar")                  // 두번째 원소 값이 bar인지 확인
                .expectComplete()                   // 종료 시그널이 존재하는지
                .verify();                          // 검증 실행
    }

    @Test
    public void create_test_2() {
        StepVerifier
                .create(Flux.range(0, 100))
                .expectSubscription()
                .expectNext(0)
                .expectNextCount(98)    // 첫번째 원소 이 후 98개의 원소가 정상 생성되었는지 확인
                .expectNext(99)         // 99번째 원소의 값이 99인지
                .expectComplete()
                .verify();
    }

    @Test
    public void create_test_3() {
        StepVerifier
                .create(Flux.just("alpha-foo", "beta-bar"))
                .expectSubscription()
                .expectNextMatches(e -> e.startsWith("alpha"))  // predicate
                .thenCancel()           // 구독 취소
                .verify();
    }

    @Test
    public void create_test_4() {
        StepVerifier
                .create(Flux.error(new RuntimeException("Error")))
                .expectError(RuntimeException.class)  // 에러가 RuntimeException 클래스 타입의 발생했는지 확인
                .verify();
    }

    @Test
    public void create_test_5() {
        StepVerifier
                .create(Flux.range(0, 100), 2)  // 두번째 인자로 초기 구독자가 요청하는 데이터 지정, 지정하지 않으면 Long.MAX_VALUE가 지정된다.
                .expectSubscription()
                .expectNext(0)
                .expectNext(1)
                .thenRequest(3) // 구독자의 요청 수량 제어(배압)
                .expectNext(2, 3, 4)
                .thenCancel()
                .verify();      // 검증 실행
    }

    @Test
    public void create_test_6() {
        /*
        * TestPublisher 테스트 목적으로 onNext(), onComplete(), onError() 이벤트를 직접 기동할 수 있다.
        * */
        TestPublisher<String> idsPublisher = TestPublisher.create();

        StepVerifier
                .create(idsPublisher.flux())
                .expectSubscription()
                .then(() -> idsPublisher.next("1")) // next 발생
                .expectNext("1")
                .then(idsPublisher::complete)       // complete 발생
                .expectComplete()
                .verify();

    }

    private Flux<String> sendWithInterval() {
        return Flux.interval(Duration.ofMinutes(1))
                .zipWith(Flux.just("a", "b", "c"))
                .map(Tuple2::toString);
    }

    @Test
    public void create_test_7() {
        StepVerifier
                .withVirtualTime(() -> sendWithInterval())
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(3))   // 시간 제어, 지정된 시간 만큼 이전에 실행해야 하거나 실행하도록 예약된 모든 작업을 기동. 인자를 지정하지 않으면 모든 작업 기동
                .expectNext("[0,a]", "[1,b]", "[2,c]")
                .expectComplete()
                .verify();
    }

    @Test
    public void create_test_8() {
        StepVerifier
                .withVirtualTime(() -> sendWithInterval())
                .expectSubscription()
                .expectNoEvent(Duration.ofMinutes(1))   // 지정된 대기 시간 동안 이벤트 없음을 확인
                .expectNext("[0,a]")
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNext("[1,b]")
                .thenCancel()
                .verify();
    }




}
