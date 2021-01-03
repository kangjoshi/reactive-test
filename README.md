### 리액티브 테스트
리액티브 프로그래밍의 코드는 비동기식이므로 반환된 값이 올바른지 확인하기 위해서는 Publisher 인터페이스를 사용해 스트림을 게시하고 
Subscritber 인터페이스를 구현하는등 간단하게 테스트할 수 있는 방법이 없다. 

#### StepVerifier 
테스트 목적으로 제공되는 reactor-test 모듈에 포함. StepVerifier가 제공하는 API를 이용하면 어떤 종류의 Publisher라도 스트림 검증을 위한 플로를 만들 수 있다.

```java
StepVerifier
    .create(Flux.just("foo", "bar"))    // 원소 생성
    .expectSubscription()               // 구독 이벤트인지
    .expectNext("foo")                  // 첫번째 원소 값이 foo인지 확인
    .expectNext("bar")                  // 두번째 원소 값이 bar인지 확인
    .expectComplete()                   // 종료 시그널이 존재하는지
    .verify();                          // 검증 실행    
```
expectNext(T var1)는 next의 값이 인자값과 동일한지 확인한다. expectComplete()는 원소가 모두 소비되어 종료 시그널이 발생했는지 검증한다.
verify()는 블로킹 호출이므로 검증 플로가 완료될 때 까지 실행이 차단된다.

```java
StepVerifier
    .create(Flux.just("alpha-foo", "beta-bar"))
    .expectSubscription()
    .expectNextMatches(e -> e.startsWith("alpha"))  // 조건을 확인하여 검증
    .thenCancel()                                   // 구독 취소
    .verify();
```
expectNextMatches(Predicate<? super T> var1)는 Predicate를 인자로 받아 좀더 유연한 검증을 할 수 있게한다. thenCancel()를 이용하여 모든 원소가 소비되지 않아도 구독 취소를 할 수 있다.

```java
StepVerifier
    .create(Flux.error(new RuntimeException("Error")))
    .expectError(RuntimeException.class)  // 에러가 RuntimeException 클래스 타입의 발생했는지 확인
    .verify();
```
expectError(Class<? extends Throwable> var1)로 인자와 동일한 타입의 예외 발생 여부를 확인할 수 있다. 인자를 넘기지 않는다면 예외 발생 여부를 확인한다.

```java
StepVerifier
    .create(Flux.range(0, 100), 2)  // 두번째 인자로 초기 구독자가 요청하는 데이터 지정, 지정하지 않으면 Long.MAX_VALUE가 지정된다.
    .expectSubscription()
    .expectNext(0, 1)
    .thenRequest(3)                 // 구독자의 요청 수량 제어(배압)
    .expectNext(2, 3, 4)
    .thenCancel()
    .verify();
```
thenRequest(long var1)로 요청 수량 제어를 할 수 있다.

```java
TestPublisher<String> idsPublisher = TestPublisher.create();

StepVerifier
        .create(idsPublisher.flux())
        .expectSubscription()
        .then(() -> idsPublisher.next("1")) // next 발생
        .expectNext("1")
        .then(idsPublisher::complete)       // complete 발생
        .expectComplete()
        .verify();
```
TestPublisher 테스트 목적으로 onNext(), onComplete(), onError() 이벤트를 직접 기동할 수 있다.

```java
private Flux<String> sendWithInterval() {
    return Flux.interval(Duration.ofMinutes(1))
            .zipWith(Flux.just("a", "b", "c"))
            .map(Tuple2::toString);
}

StepVerifier
        .withVirtualTime(() -> sendWithInterval())
        .expectSubscription()
        .thenAwait(Duration.ofMinutes(3))   // 시간 제어, 인자로 지정한 시간 이전에 실행하도록 예약된 모든 작업을 기동. 인자를 지정하지 않으면 실행해야 할 모든 작업 기동
        .expectNext("[0,a]", "[1,b]", "[2,c]")
        .expectComplete()
        .verify();

StepVerifier
        .withVirtualTime(() -> sendWithInterval())
        .expectSubscription()
        .expectNoEvent(Duration.ofMinutes(1))   // 지정된 대기 시간 동안 이벤트 없음을 확인
        .expectNext("[0,a]")
        .expectNoEvent(Duration.ofMinutes(1))
        .expectNext("[1,b]")
        .thenCancel()
        .verify();
```
withVirtualTime(Supplier<? extends Publisher<? extends T>> scenarioSupplier)을 사용하면 VirtualTimeScheduler를 사용하여 리액터의 모든 스케쥴러를 가상으로 대체하여 시간을 제어하여 테스트를 수행할 수 있다.

##### WebTestClient와 통합
```java
WebTestClient
    .bindToController(paymentController)
    .build()
    .get()
    .uri("/payments/")
    .exchange()
    .expectStatus().isOk()
    .returnResult(Payment.class)
    .getResponseBody()
    .as(StepVerifier::create)                   // webTestClient를 이용해 반환된 결과를 StepVerifier로 받아 테스트를 이어 나갈수 있다.
    .expectNextMatches(e -> e.getId() == 1)
    .expectNextMatches(e -> e.getId() == 2)
    .expectComplete()
    .verify();
```
