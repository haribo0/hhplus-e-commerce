# 서비스 분리 및 트랜잭션 처리 설계

## 1. MSA로의 서비스 분리와 트랜잭션 처리 문제

### 1.1. MSA 환경에서의 트랜잭션 처리 한계

모놀리식 아키텍처에서는 단일 데이터베이스를 사용하여 하나의 트랜잭션 내에서 모든 변경 사항을 처리할 수 있었다. 그러나 MSA로 전환되면서 각 서비스가 독립적인 데이터베이스를 가지게 되고, 이를 통해 하나의 요청이 여러 개의 서비스와 데이터베이스를 거치며 처리될 수밖에 없다. 이로 인해 다음과 같은 문제가 발생할 수 있다.

#### 주요 문제점

- **분산 트랜잭션 관리의 어려움**: 기존의 RDBMS에서 제공하는 ACID 트랜잭션을 여러 서비스에 걸쳐 보장하기 어려우며, 이를 위해 별도의 분산 트랜잭션 관리 기법이 필요하다.
- **데이터 정합성 문제**: 주문과 결제가 개별 서비스에서 실행되면, 결제는 완료되었지만 주문이 실패하는 경우와 같은 데이터 불일치가 발생할 수 있다.
- **보상 트랜잭션 필요성**: 일부 서비스에서 변경 사항이 반영되었으나 다른 서비스에서 실패하는 경우, 이를 되돌리기 위한 보상 트랜잭션을 고려해야 한다.
- **비동기 이벤트 처리의 복잡성**: 이벤트 기반으로 시스템을 구성할 경우, 장애 발생 시 트랜잭션의 일관성을 유지하는 메커니즘이 필요하다.
- **네트워크 장애 및 가용성 문제**: 한 서비스가 실패하면 연쇄적으로 다른 서비스에도 영향을 미칠 수 있어 가용성을 고려한 설계가 필요하다.

### 1.2. 해결 방안

이러한 문제를 해결하기 위해 다음과 같은 접근 방식을 적용할 수 있다.

| 해결 방안                                  | 설명                                                             |
| -------------------------------------- | -------------------------------------------------------------- |
| **Saga 패턴**                            | 트랜잭션을 여러 개의 로컬 트랜잭션으로 나누고, 보상 트랜잭션을 통해 롤백을 관리                  |
| **이벤트 기반 아키텍처**                        | Kafka, Redis Stream과 같은 메시지 브로커를 활용해 서비스 간 의존도를 낮추고 비동기 처리를 수행 |
| **보상 트랜잭션 (Compensation Transaction)** | 작업 실패 시 보정 작업을 실행하여 데이터 정합성을 유지                                |
| **Idempotency (멱등성) 보장**               | 중복 실행에도 동일한 결과를 보장하여 네트워크 장애 등으로 인한 오류를 방지                     |

---

## 2. 실시간 주문 데이터 전달 기능 추가

### 2.1. 기존 로직 개선 (Kafka 기반 비동기 이벤트 처리)

기존 시스템에서는 주문 완료 후 `dataPlatform.publish()`를 직접 호출하는 방식이었다. 이 방식은 주문 서비스와 데이터 플랫폼 간의 강한 결합을 유발하며, 네트워크 장애 발생 시 데이터 불일치 문제가 발생할 수 있다. 이를 해결하기 위해 Kafka를 활용한 비동기 이벤트 기반 구조로 개선한다.

### 2.2. 개선된 이벤트 기반 설계

#### 주문 완료 후 Kafka 이벤트 발행

```java
@Transactional
public OrderInfo order(OrderCommand.Create command) {
    Order order = orderService.create(command);
    kafkaTemplate.send("order.completed", new OrderCompletedEvent(order.getId()));
    return new OrderInfo(order.getId());
}
```

#### 데이터 플랫폼 서비스에서 Kafka 이벤트 구독

```java
@KafkaListener(topics = "order.completed", groupId = "dataplatform-group")
public void handleOrderCompleted(OrderCompletedEvent event) {
    dataPlatform.publish(event.getOrderId());
}
```

**개선된 점**

- 주문 서비스와 데이터 플랫폼 서비스 간 **의존성을 제거**
- 네트워크 장애 발생 시 **재시도 로직 적용 가능**
- 확장성이 뛰어난 **비동기 이벤트 기반 설계**

---

## 3. 트랜잭션 처리 방식 비교: Facade vs. 이벤트 기반 Saga

### 3.1. Facade 기반 트랜잭션 관리

Facade를 활용하면 트랜잭션을 도메인 단위로 분리하여 관리할 수 있지만, 분산 환경에서는 여전히 일관성을 보장하기 어렵다. 이를 보완하기 위해 **보상 트랜잭션**을 적용할 수 있다.

#### 개선된 Facade 방식 (보상 트랜잭션 적용)

```java
@Transactional
public OrderInfo order(OrderCommand.Create command) {
    try {
        Order order = orderService.create(command);
        Payment payment = paymentService.create(order);
        stockService.deduct(order);
        kafkaTemplate.send("order.completed", new OrderCompletedEvent(order.getId()));
        return new OrderInfo(order.getId());
    } catch (Exception e) {
        paymentService.refund(order.getId());
        stockService.restore(order.getId());
        throw e;
    }
}
```

**장점**: 트랜잭션을 도메인 단위로 분리하여 처리할 수 있으며, 보상 트랜잭션을 활용해 롤백이 가능하다.

**단점**: 서비스 간 결합이 여전히 존재하며, 보상 트랜잭션 설계가 복잡할 수 있다.

---

### 3.2. 이벤트 기반 Saga 패턴 적용

서비스 간 의존성을 완전히 제거하기 위해 **Saga 패턴**을 적용한다. 각 서비스는 이벤트를 통해 상태를 관리하며, 개별 트랜잭션이 독립적으로 실행된다.

#### 주문 서비스 (Saga 시작)

```java
@Transactional
public OrderInfo createOrder(OrderCommand.Create command) {
    Order order = orderService.create(command);
    kafkaTemplate.send("order.created", new OrderCreatedEvent(order.getId()));
    return new OrderInfo(order.getId());
}
```

#### 결제 서비스 (Saga Step)

```java
@KafkaListener(topics = "order.created", groupId = "payment-group")
public void handleOrderCreated(OrderCreatedEvent event) {
    Payment payment = paymentService.create(event.getOrderId());
    kafkaTemplate.send("payment.success", new PaymentCompletedEvent(event.getOrderId()));
}
```

#### 재고 서비스 (Saga Step)

```java
@KafkaListener(topics = "payment.success", groupId = "stock-group")
public void handlePaymentSuccess(PaymentCompletedEvent event) {
    boolean success = stockService.deduct(event.getOrderId());
    if (success) {
        kafkaTemplate.send("order.completed", new OrderCompletedEvent(event.getOrderId()));
    } else {
        kafkaTemplate.send("payment.rollback", new PaymentRollbackEvent(event.getOrderId()));
    }
}
```

**장점**: 서비스 간 의존성을 완전히 제거하며 장애에 대한 복원력이 높다.

**단점**: 이벤트 기반 설계가 필요하며, 트랜잭션 처리가 복잡해질 수 있다.

---

## 4. 결론

MSA로 분리할 경우 기존의 Facade 기반 트랜잭션 관리 방식은 분산 환경에서 한계를 가지게 된다. 이를 대체하기 위해 **이벤트 기반 Saga 패턴**을 적용하면 서비스 간 결합도를 줄이고, 장애 발생 시 복원력을 높일 수 있다.

이벤트 기반 아키텍처를 활용하면 개별 서비스가 독립적인 트랜잭션을 유지하면서도, Kafka와 같은 메시지 브로커를 통해 데이터 일관성을 보장할 수 있다. 또한, 보상 트랜잭션을 함께 적용하면 데이터 불일치를 최소화할 수 있다.

따라서, MSA 환경에서 트랜잭션 일관성을 유지하기 위해 Facade 기반 방식 대신 이벤트 기반 트랜잭션 관리 방식(Saga 패턴, 보상 트랜잭션 등)을 고려하는 것이 적절하다고 생각한다.

